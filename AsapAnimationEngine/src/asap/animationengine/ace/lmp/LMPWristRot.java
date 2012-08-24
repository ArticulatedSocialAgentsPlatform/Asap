package asap.animationengine.ace.lmp;

import hmi.animation.Hanim;
import hmi.math.Mat3f;
import hmi.math.Quat4f;
import hmi.math.Vec3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import saiba.bml.feedback.BMLSyncPointProgressFeedback;

import lombok.extern.slf4j.Slf4j;
import asap.animationengine.ace.OrientConstraint;
import asap.motionunit.TMUPlayException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedPlanUnitPlayException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * Local motor program for absolute wrist rotations
 * @author hvanwelbergen
 * @author Stefan Kopp (original C++ version)
 */
@Slf4j
public class LMPWristRot extends LMPPos
{
    private ImmutableSet<String> kinematicJoints;
    private ImmutableList<OrientConstraint> ocVec;
    private Map<OrientConstraint, TimePeg> constraintMap = new HashMap<OrientConstraint, TimePeg>();
    private final PegBoard pegBoard;

    private static final float PRECISION = 0.001f;
    private static final double TRANSITION_TIME = 0.4;
    private static final double DEFAULT_STROKEPHASE_DURATION = 5;

    public LMPWristRot(String scope, List<OrientConstraint> ocVec, FeedbackManager bbf, BMLBlockPeg bmlBlockPeg, String bmlId, String id,
            PegBoard pegBoard)
    {
        super(bbf, bmlBlockPeg, bmlId, id);
        if (scope.equals("left_arm"))
        {
            kinematicJoints = ImmutableSet.of(Hanim.l_wrist);
        }
        else if (scope.equals("right_arm"))
        {
            kinematicJoints = ImmutableSet.of(Hanim.r_wrist);
        }
        this.ocVec = ImmutableList.copyOf(ocVec);
        this.pegBoard = pegBoard;
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

    // m is previous rotation
    private float[] getNextWristRot(float[] m, OrientConstraint oc)
    {
        float dOld[] = Vec3f.getVec3f();
        float pOld[] = Vec3f.getVec3f();

        Mat3f.getRow(m, 1, pOld);
        Mat3f.getRow(m, 2, dOld);

        if (Vec3f.epsilonEquals(oc.getD(), Vec3f.getZero(), PRECISION) && Vec3f.epsilonEquals(oc.getP(), Vec3f.getZero(), PRECISION))
        // -- efo dir and palm orientation given
        {
            // get target orientation
            float[] d = Vec3f.getVec3f(oc.getD());
            Vec3f.normalize(d);
            float[] p = Vec3f.getVec3f(oc.getP());
            Vec3f.normalize(p);
            float dot = Vec3f.dot(d, p);
            if (dot > 0)
            {
                log.warn("WristRot : Ext. finger orient. and palm orient. not orthogonal!!!!");
                float a[] = Vec3f.getVec3f();
                Vec3f.cross(a, d, p);
                Vec3f.cross(p, a, d);

                Vec3f.normalize(p);
                log.info("-> corrected palm normal: {}", Vec3f.toString(p));
            }

            if (getKinematicJoints().iterator().next().equals(Hanim.r_wrist))
            {
                Vec3f.scale(-1, p);
            }
            float v[] = Vec3f.getVec3f();
            Vec3f.cross(v, p, d);
            Vec3f.normalize(v);
            float m1[] = Mat3f.getMat3f();
            Mat3f.setRow(m1, 0, v);
            Mat3f.setRow(m1, 1, p);
            Mat3f.setRow(m1, 2, d);
            return m1;
        }

        else if (Vec3f.epsilonEquals(oc.getD(), Vec3f.getZero(), PRECISION))
        // -- only efo given
        {
            // get transformation dOld -> d
            float d[] = Vec3f.getVec3f(oc.getD());
            Vec3f.normalize(d);
            Vec3f.normalize(dOld);

            float m1[] = Mat3f.getMat3f();
            float q[] = Quat4f.getQuat4f();
            Quat4f.setFromVectors(q, dOld, d);
            Mat3f.setFromQuatScale(m1, q, 1);
            Mat3f.mul(m1, m, m1);
            Mat3f.mul(m, m1);
            return m1;
        }

        else if (Vec3f.epsilonEquals(oc.getP(), Vec3f.getZero(), PRECISION))
        // -- only palm orientation given
        {
            // get transformation pOld -> p
            float p[] = Vec3f.getVec3f(oc.getP());
            if (getKinematicJoints().iterator().next().equals(Hanim.r_wrist))
            {
                Vec3f.scale(-1, p);
            }
            Vec3f.normalize(p);
            Vec3f.normalize(pOld);
            float m1[] = Mat3f.getMat3f();
            // M.makeRotate(pOld,p);
            float q[] = Quat4f.getQuat4f();
            Quat4f.setFromVectors(q, pOld, p);
            Mat3f.setFromQuatScale(m1, q, 1);
            Mat3f.mul(m1, m, m1);
            return m1;
        }
        return Mat3f.getIdentity();
    }

    private void createdPegWhenMissingOnPegBoard(String syncId)
    {
        if (pegBoard.getTimePeg(getBMLId(), getId(), syncId) == null)
        {
            TimePeg tp = new TimePeg(getBMLBlockPeg());
            pegBoard.addTimePeg(getBMLId(), getId(), syncId, tp);
        }

    }

    private void createMissingTimePegs()
    {
        for (OrientConstraint oc : ocVec)
        {
            if (constraintMap.get(oc) == null)
            {
                TimePeg tp = new TimePeg(getBMLBlockPeg());
                constraintMap.put(oc, tp);
                pegBoard.addTimePeg(getBMLId(), getId(), oc.getId(), tp);
            }
        }
        createdPegWhenMissingOnPegBoard("start");
        createdPegWhenMissingOnPegBoard("end");
    }

    private void uniformlyDistributeStrokeConstraints()
    {
        List<OrientConstraint> tpSet = new ArrayList<>();
        for (OrientConstraint oc : ocVec)
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
        
        //set inner
        for(int i=0;i<tpSet.size()-1;i++)
        {
            OrientConstraint ocLeft = tpSet.get(i);
            OrientConstraint ocRight = tpSet.get(i+1);
            TimePeg tpLeft = constraintMap.get(ocLeft);
            TimePeg tpRight = constraintMap.get(ocRight);
            double avgDur = (tpLeft.getGlobalValue()-tpRight.getGlobalValue())/(ocVec.indexOf(ocRight)-ocVec.indexOf(ocLeft));
            double time = tpLeft.getGlobalValue();
            for(int j=ocVec.indexOf(ocRight)+1;j<ocVec.indexOf(ocLeft);j++)
            {
                time+=avgDur;
                constraintMap.get(ocVec.get(j)).setGlobalValue(time);
            }
        }

        
        //find average duration to use for outer
        int i = 0;       
        double totalDur = 0;
        int segments = 0;
        
        TimePeg tpPrev = null;
        for (OrientConstraint oc : ocVec)
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
        double avgDur = DEFAULT_STROKEPHASE_DURATION/(ocVec.size()-1);
        if(segments>0)
        {
            avgDur = totalDur/segments;
        }        
        
        //set right
        double time =  constraintMap.get(tpSet.get(tpSet.size()-1)).getGlobalValue();
        for(int j=ocVec.indexOf(tpSet.get(tpSet.size()-1))+1;j<ocVec.size();j++)
        {
            time+=avgDur;
            constraintMap.get(ocVec.get(j)).setGlobalValue(time);
        }
        
        //set left        
        time =  constraintMap.get(0).getGlobalValue();
        for(int j=ocVec.indexOf(tpSet.get(0))-1;j>=0;j--)
        {
            time-=avgDur;
            constraintMap.get(ocVec.get(j)).setGlobalValue(time);
        }
    }

    @Override
    public void updateTiming(double time) throws TMUPlayException
    {
        createMissingTimePegs();
        
        //TODO: handle cases in which constraints that are not on the 'border' of the LMP are set in a better manner. 
        
        // resolve start
        if (getStartTime() == TimePeg.VALUE_UNKNOWN)
        {
            if (getTimePeg("stroke_start").getGlobalValue() != TimePeg.VALUE_UNKNOWN)
            {
                pegBoard.setPegTime(getBMLId(), getId(), "start", getTimePeg("stroke_start").getGlobalValue() - TRANSITION_TIME);
            }
        }
        else if (getTimePeg("stroke_start").getGlobalValue() == TimePeg.VALUE_UNKNOWN)
        {
            pegBoard.setPegTime(getBMLId(), getId(), "stroke_start", getStartTime() + TRANSITION_TIME);            
        }
        else
        {
            pegBoard.getTimePeg(getBMLId(),getId(),"start").setValue(0, getBMLBlockPeg());
            pegBoard.getTimePeg(getBMLId(),getId(),"stroke_start").setValue(TRANSITION_TIME, getBMLBlockPeg());
        }

        // resolve end
        if (getEndTime() == TimePeg.VALUE_UNKNOWN)
        {
            if (getTimePeg("stroke_end").getGlobalValue() != TimePeg.VALUE_UNKNOWN)
            {
                pegBoard.setPegTime(getBMLId(), getId(), "end", getTimePeg("stroke_end").getGlobalValue() + TRANSITION_TIME);
            }
        }
        else
        {
            if (getTimePeg("stroke_end").getGlobalValue() == TimePeg.VALUE_UNKNOWN)
            {
                pegBoard.setPegTime(getBMLId(), getId(), "stroke_end", getStartTime() - TRANSITION_TIME);
            }
        }

        uniformlyDistributeStrokeConstraints();

        if (getEndTime() == TimePeg.VALUE_UNKNOWN)
        {
            pegBoard.setPegTime(getBMLId(), getId(), "end", getTimePeg("stroke_end").getGlobalValue() + TRANSITION_TIME);
        }

    }

    @Override
    public double getStartTime()
    {
        return pegBoard.getPegTime(getBMLId(),getId(),"start");
    }

    @Override
    public double getEndTime()
    {
        return pegBoard.getPegTime(getBMLId(),getId(),"end");
    }

    @Override
    public double getRelaxTime()
    {
        if (pegBoard.getPegTime(getBMLId(),getId(),"relax")!=TimePeg.VALUE_UNKNOWN)
        {
            return pegBoard.getPegTime(getBMLId(),getId(),"relax");
        }
        else if (pegBoard.getPegTime(getBMLId(),getId(),"stroke_end")!=TimePeg.VALUE_UNKNOWN)
        {
            return pegBoard.getPegTime(getBMLId(),getId(),"stroke_end");
        }        
        return getEndTime();
    }

    @Override
    public TimePeg getTimePeg(String syncId)
    {
        return pegBoard.getTimePeg(getBMLId(),getId(), syncId);
    }

    private OrientConstraint findOrientConstraint(String syncId)
    {
        for (OrientConstraint oc : ocVec)
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
        pegBoard.addTimePeg(getBMLId(), getId(), syncId, peg);
    }

    @Override
    public boolean hasValidTiming()
    {
        TimePeg tpPrev = null;
        for (OrientConstraint oc : ocVec)
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
    protected void playUnit(double time) throws TimedPlanUnitPlayException
    {
        // TODO Auto-generated method stub

    }

    @Override
    protected void startUnit(double time) throws TimedPlanUnitPlayException
    {
        // //TODO
        // // get tfm for stroke onset
        // cQuat.FromRotationMatrix( (MgcMatrix3) c );
        // startRot = getNextWristRot( c, ocVec.front() );
        // startQuat.FromRotationMatrix((MgcMatrix3) startRot);
        //
        // // complete and start
        // refine();
        
        double bmlBlockTime = time - bmlBlockPeg.getValue();
        feedback(new BMLSyncPointProgressFeedback(getBMLId(), getId(), "start", bmlBlockTime, time));
    }

    @Override
    protected void stopUnit(double time) throws TimedPlanUnitPlayException
    {
        // TODO Auto-generated method stub

    }

}
