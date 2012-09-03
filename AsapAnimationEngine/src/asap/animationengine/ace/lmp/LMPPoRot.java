package asap.animationengine.ace.lmp;

import hmi.animation.Hanim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import saiba.bml.core.Behaviour;
import asap.animationengine.ace.OrientConstraint;
import asap.animationengine.ace.PoConstraint;
import asap.motionunit.TMUPlayException;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.scheduler.TimePegAndConstraint;

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
    private ImmutableSet<String> kinematicJoints;
    private final String joint;
    private double qS, qDotS; // start angles and angular velocity (for scope joints only!!!)
    private int segments;
    private Map<PoConstraint, TimePeg> constraintMap = new HashMap<>();

    private static final double TRANSITION_TIME = 0.4;
    private static final double DEFAULT_STROKEPHASE_DURATION = 5;

    @Setter
    private List<PoConstraint> poVec;

    public LMPPoRot(String scope, List<PoConstraint> poVec, FeedbackManager fbm, BMLBlockPeg bmlPeg, String bmlId, String behId,
            PegBoard pegBoard)
    {
        super(fbm, bmlPeg, bmlId, behId, pegBoard);
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
            segments = vv.size() - 1;
            // if (segments >= timeVec.size())
            // cerr << "Warning: " << segments << " segments "
            // << "for " << timeVec.size() << " time points!" << endl;
        }
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
                pegBoard.addTimePeg(getBMLId(), getId(), oc.getId(), tp);
            }
        }
        createPegWhenMissingOnPegBoard("start");
        createPegWhenMissingOnPegBoard("end");
    }

    private void uniformlyDistributeStrokeConstraints(double earliestStart)
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

    private boolean noPegsSet()
    {
        for (TimePeg tp : pegBoard.getTimePegs(getBMLId(), getId()))
        {
            if (tp.getGlobalValue() != TimePeg.VALUE_UNKNOWN)
            {
                return false;
            }
        }
        return true;
    }

    private void resolveTimePegs(double time)
    {
        createMissingTimePegs();

        // TODO: handle cases in which constraints that are not on the 'border' of the LMP are set in a better manner.

        // resolve start
        if (getStartTime() == TimePeg.VALUE_UNKNOWN && getTimePeg("strokeStart").getGlobalValue() != TimePeg.VALUE_UNKNOWN)
        {
            pegBoard.setPegTime(getBMLId(), getId(), "start", getTimePeg("strokeStart").getGlobalValue() - TRANSITION_TIME);
        }
        else if (getTimePeg("strokeStart").getGlobalValue() == TimePeg.VALUE_UNKNOWN && getStartTime() != TimePeg.VALUE_UNKNOWN)
        {
            pegBoard.setPegTime(getBMLId(), getId(), "strokeStart", getStartTime() + TRANSITION_TIME);
        }
        else if (noPegsSet())
        {
            pegBoard.getTimePeg(getBMLId(), getId(), "start").setValue(0, getBMLBlockPeg());
            pegBoard.getTimePeg(getBMLId(), getId(), "strokeStart").setValue(TRANSITION_TIME, getBMLBlockPeg());
        }

        // resolve end
        if (getEndTime() == TimePeg.VALUE_UNKNOWN)
        {
            if (getTimePeg("strokeEnd").getGlobalValue() != TimePeg.VALUE_UNKNOWN)
            {
                pegBoard.setPegTime(getBMLId(), getId(), "end", getTimePeg("strokeEnd").getGlobalValue() + TRANSITION_TIME);
            }
        }
        else
        {
            if (getTimePeg("strokeEnd").getGlobalValue() == TimePeg.VALUE_UNKNOWN)
            {
                pegBoard.setPegTime(getBMLId(), getId(), "strokeEnd", getEndTime() - TRANSITION_TIME);
            }
        }

        uniformlyDistributeStrokeConstraints(time);
        if (getStartTime() == TimePeg.VALUE_UNKNOWN)
        {
            pegBoard.setPegTime(getBMLId(), getId(), "start", getTimePeg("strokeStart").getGlobalValue() - TRANSITION_TIME);
        }
        if (getEndTime() == TimePeg.VALUE_UNKNOWN)
        {
            pegBoard.setPegTime(getBMLId(), getId(), "end", getTimePeg("strokeEnd").getGlobalValue() + TRANSITION_TIME);
        }
    }

    @Override
    public void updateTiming(double time) throws TMUPlayException
    {
        if (!isLurking()) return;
        resolveTimePegs(time);
    }

    @Override
    public void resolveSynchs(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sac) throws BehaviourPlanningException
    {
        // TODO Auto-generated method stub

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

    @Override
    protected void startUnit(double time)
    {
        feedback("start", time);
    }

    @Override
    protected void playUnit(double time) throws TimedPlanUnitPlayException
    {
        // TODO Auto-generated method stub

    }

    @Override
    protected void stopUnit(double time) throws TimedPlanUnitPlayException
    {
        // TODO Auto-generated method stub

    }

}
