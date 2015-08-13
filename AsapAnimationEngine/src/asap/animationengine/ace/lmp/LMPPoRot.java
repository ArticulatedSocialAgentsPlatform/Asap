/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.ace.lmp;

import hmi.animation.Hanim;
import hmi.animation.VJoint;
import hmi.math.Vecf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.ace.GStrokePhaseID;
import asap.animationengine.ace.PoConstraint;
import asap.math.splines.TCBSplineN;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedPlanUnitPlayException;

import com.google.common.collect.ImmutableSet;

//XXX some code here is very similar to that in LMPWristRot, generalize this
/**
 * LMP for local wrist rotation
 * @author hvanwelbergen
 * 
 */
@Slf4j
public class LMPPoRot extends LMP
{
    private final AnimationPlayer aniPlayer;
    private ImmutableSet<String> kinematicJoints;
    private final String joint;
    private double qS, qDotS; // start angles and angular velocity (for scope joints only!!!)
    private Map<PoConstraint, TimePeg> constraintMap = new HashMap<>();
    private TCBSplineN traj;

    private List<float[]> pointVec;
    private List<Double> timeVec;
    private VJoint vjWristAdditive; 
    private VJoint vAdditive;
    private static final double TRANSITION_TIME = 0.4; // TODO: use getPODurationFromAmplitude instead?
    private static final double DEFAULT_STROKEPHASE_DURATION = 0;

    @Setter
    private List<PoConstraint> poVec;

    public LMPPoRot(String scope, List<PoConstraint> poVec, FeedbackManager fbm, BMLBlockPeg bmlPeg, String bmlId, String behId,
            PegBoard pegBoard, AnimationPlayer aniPlayer)
    {
        super(fbm, bmlPeg, bmlId, behId, pegBoard);
        this.aniPlayer = aniPlayer;
        this.poVec = poVec;
        
        

        // TODO: implement proper scope selection when no scope is provided.
        if (scope == null)
        {
            scope = "left_arm";
        }

        if (scope.equals("left_arm"))
        {
            kinematicJoints = ImmutableSet.of(Hanim.l_wrist);
            joint = Hanim.l_wrist;
        }
        else if (scope.equals("right_arm"))
        {
            kinematicJoints = ImmutableSet.of(Hanim.r_wrist);
            joint = Hanim.r_wrist;
        }
        else
        {
            joint = null;
            log.warn("Invalid scope {}" + scope);
        }
        createMissingTimePegs();
    }

    @Override
    public Set<String> getKinematicJoints()
    {
        return kinematicJoints;
    }

    @Override
    public Set<String> getPhysicalJoints()
    {
        return ImmutableSet.of();
    }

    // from LMP_JointAngle::setAngleVec
    public void setPoConstraint(List<PoConstraint> vv)
    {
        if (!vv.isEmpty())
        {
            // cout << "SET ANGLE VEC:" << endl;
            // for (int i=0; i<vv.size(); i++)
            // cout << vv[i] << "->" << endl;

            poVec = vv;
            qS = 0;
            qDotS = 0;
            //segments = vv.size() - 1;
            // if (segments >= timeVec.size())
            // cerr << "Warning: " << segments << " segments "
            // << "for " << timeVec.size() << " time points!" << endl;
        }
    }

    @Override
    public List<String> getAvailableSyncs()
    {
        List<String> syncs = super.getAvailableSyncs();
        for (PoConstraint oc : poVec)
        {
            if (!syncs.contains(oc.getId()))
            {
                syncs.add(oc.getId());
            }
        }
        return syncs;
    }

    private double getPODurationFromAmplitude(double amp)
    {
        return (Math.abs(amp) / 140.0);
    }

    private void createMissingTimePegs()
    {
        for (PoConstraint oc : poVec)
        {
            if (constraintMap.get(oc) == null)
            {
                TimePeg tp = new TimePeg(getBMLBlockPeg());
                constraintMap.put(oc, tp);
                setTimePeg(oc.getId(), tp);
            }
        }        
    }

    protected void setInternalStrokeTiming(double earliestStart)
    {
        List<PoConstraint> tpSet = new ArrayList<>();
        for (PoConstraint oc : poVec)
        {
            TimePeg tp = constraintMap.get(oc);
            if (tp != null)
            {
                if (tp.getGlobalValue() != TimePeg.VALUE_UNKNOWN)
                {
                    tpSet.add(oc);
                }
            }
        }

        // set inner
        for (int i = 0; i < tpSet.size() - 1; i++)
        {
            PoConstraint ocLeft = tpSet.get(i);
            PoConstraint ocRight = tpSet.get(i + 1);
            TimePeg tpLeft = constraintMap.get(ocLeft);
            TimePeg tpRight = constraintMap.get(ocRight);
            double avgDur = (tpRight.getGlobalValue() - tpLeft.getGlobalValue()) / (poVec.indexOf(ocRight) - poVec.indexOf(ocLeft));
            double time = tpLeft.getGlobalValue();
            for (int j = poVec.indexOf(ocLeft) + 1; j < poVec.indexOf(ocRight); j++)
            {
                time += avgDur;
                constraintMap.get(poVec.get(j)).setGlobalValue(time);
            }
        }

        // find average duration to use for outer
        int i = 0;
        double totalDur = 0;
        int segments = 0;

        TimePeg tpPrev = null;
        for (PoConstraint oc : poVec)
        {
            i++;
            TimePeg tp = constraintMap.get(oc);
            if (tp != null)
            {
                if (tp.getGlobalValue() != TimePeg.VALUE_UNKNOWN)
                {
                    if (tpPrev != null)
                    {
                        segments++;
                        totalDur += (tp.getGlobalValue() - tpPrev.getGlobalValue()) / (double) i;
                        i = 0;
                    }
                    tpPrev = tp;
                }
            }
        }
        double avgDur = DEFAULT_STROKEPHASE_DURATION / (poVec.size() - 1);
        if (segments > 0)
        {
            avgDur = totalDur / segments;
        }

        // set from right to end
        double time = constraintMap.get(tpSet.get(tpSet.size() - 1)).getGlobalValue();
        for (int j = poVec.indexOf(tpSet.get(tpSet.size() - 1)) + 1; j < poVec.size(); j++)
        {
            time += avgDur;
            constraintMap.get(poVec.get(j)).setGlobalValue(time);
        }

        // set from left to start
        time = constraintMap.get(tpSet.get(0)).getGlobalValue();
        int nrOfSegments = poVec.indexOf(tpSet.get(0));
        if (time - (nrOfSegments * avgDur) < TRANSITION_TIME + earliestStart)
        {
            avgDur = (time - TRANSITION_TIME) / nrOfSegments;
        }

        for (int j = poVec.indexOf(tpSet.get(0)) - 1; j >= 0; j--)
        {
            time -= avgDur;
            constraintMap.get(poVec.get(j)).setGlobalValue(time);
        }
    }

    

    private PoConstraint findOrientConstraint(String syncId)
    {
        for (PoConstraint oc : poVec)
        {
            if (oc.getId().equals(syncId))
            {
                return oc;
            }
        }
        return null;
    }

    @Override
    public void setTimePeg(String syncId, TimePeg peg)
    {
        if (findOrientConstraint(syncId) != null)
        {
            constraintMap.put(findOrientConstraint(syncId), peg);
        }
        super.setTimePeg(syncId, peg);
    }

    @Override
    public boolean hasValidTiming()
    {
        TimePeg tpPrev = null;
        for (PoConstraint oc : poVec)
        {
            TimePeg tp = constraintMap.get(oc);
            if (tp != null)
            {
                if (tp.getGlobalValue() != TimePeg.VALUE_UNKNOWN)
                {
                    if (tpPrev != null)
                    {
                        if (tpPrev.getGlobalValue() > tp.getGlobalValue()) return false;
                    }
                    tpPrev = tp;
                }
            }
        }
        return true;
    }

    private List<Double> toTimeVec()
    {
        List<Double> v = new ArrayList<>();
        for (PoConstraint po : poVec)
        {
            v.add(constraintMap.get(po).getGlobalValue());
        }
        return v;
    }

    private List<float[]> toPointVec()
    {
        List<float[]> v = new ArrayList<>();
        for (PoConstraint po : poVec)
        {
            float vv[] = new float[1];
            vv[0] = (float) po.getPo();
            v.add(vv);
        }
        return v;
    }

    private void refine()
    {
        if (!pointVec.isEmpty())
        {

            // create trajectory (MgcNaturalSplineN::BT_CLAMPED)
            int segments = pointVec.size() - 1;
            //float qDotE[] = Vecf.getVecf(1);
            List<Double> t = new ArrayList<>();
            List<Double> b = new ArrayList<>();
            List<Double> c = new ArrayList<>();
            for (int i = 0; i <= segments; ++i)
            {
                t.add(0d);
                b.add(0d);
                c.add(0d);
            }
            traj = new TCBSplineN(segments, timeVec, pointVec, t, c, b);
        }
    }

    private void startFrom(float q, double qDot, double time)
    {
        timeVec.add(0, time);
        float v[] = Vecf.getVecf(1);
        v[0] = q;
        pointVec.add(0, v);
        timeVec.add(0, time);

        // TODO(?)
        // // -- FIX-ME?: this may better be dealt with in the motor control module...??
        // if ( !phaseVec.empty() )
        // {
        // // if the following state is finished, then it should be retracting phase
        // if ( phaseVec.front() == GuidingStroke::STP_FINISH )
        // phaseVec.push_front( GuidingStroke::STP_RETRACT );
        // else
        // // otherwise it is preparation
        // phaseVec.push_front( GuidingStroke::STP_PREP );
        // }
        // else
        // // if no phases defined at all, let the program be finished immediately
        // phaseVec.push_front( GuidingStroke::STP_FINISH );

        qDotS = qDot;
    }

    @Override
    protected void startUnit(double time) throws TimedPlanUnitPlayException
    {
        vAdditive = aniPlayer.constructAdditiveBody(ImmutableSet.of(joint));
        vjWristAdditive = vAdditive.getPartBySid(joint);
        
        resolveTimePegs(time);
        timeVec = toTimeVec();
        pointVec = toPointVec();

        // time to start now
        startFrom(0, 0, time);

        // FIX-ME??? ---
        // for static PO constraints, there are only three control points defined,
        // which may give too little segments! This loop is meant to fill up the
        // vectors with "meaningless" control points just to meet the minimum number
        // of spline parameters.
        while (pointVec.size() < 4)
        {
            pointVec.add(0, Vecf.getVecf(1));
            timeVec.add(0, time);
        }
        refine();
        feedback("start", time);
    }

    private double getConfiguration(double fTime)
    {
        if (traj != null)
        {
            // updateState(fTime);
            return traj.GetPosition(fTime)[0];
        }
        else log.warn("LMP_JAngleTCB::getPosition : no trajectory!");
        return 0;
    }

    @Override
    protected void playUnit(double time) throws TimedPlanUnitPlayException
    {
        double conf = getConfiguration(time);
        vjWristAdditive.setAxisAngle(0, 0, 1, (float) Math.toRadians(conf));
    }

    @Override
    protected void stopUnit(double time) throws TimedPlanUnitPlayException
    {
        feedback("end", time);
        aniPlayer.removeAdditiveBody(vAdditive);
    }

    
    @Override
    public double getPreparationDuration()
    {
        return TRANSITION_TIME;
    }

    @Override
    public double getRetractionDuration()
    {
        return TRANSITION_TIME;
    }

    @Override
    public double getStrokeDuration()
    {
        double strokeDuration = 0;        
        for (PoConstraint oc : poVec)
        {
            if (oc.getPhase() == GStrokePhaseID.STP_STROKE)
            {
                strokeDuration += TRANSITION_TIME;
            }
        }
        return strokeDuration;
    }
}
