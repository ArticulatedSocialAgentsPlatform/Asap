/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.ace.lmp;

import hmi.animation.Hanim;
import hmi.animation.VJoint;
import hmi.math.Mat3f;
import hmi.math.Quat4f;
import hmi.math.Vec3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.ace.GStrokePhaseID;
import asap.animationengine.ace.OrientConstraint;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedPlanUnitPlayException;

import com.google.common.collect.ImmutableSet;

/**
 * Local motor program for absolute wrist rotations
 * @author hvanwelbergen
 * @author Stefan Kopp (original C++ version)
 */
@Slf4j
public class LMPWristRot extends LMP
{
    private ImmutableSet<String> kinematicJoints;
    private final List<OrientConstraint> ocVec;
    private Map<OrientConstraint, TimePeg> constraintMap = new HashMap<>();
    private List<OrientPos> orientVec = new ArrayList<>();

    private final AnimationPlayer aniPlayer;
    private final String joint;

    private static final float PRECISION = 0.001f;
    public static final double TRANSITION_TIME = 0.4;

    @Data
    private static class OrientPos
    {
        private final TimePeg tp;
        private final float q[];
    }

    public LMPWristRot(String scope, List<OrientConstraint> ocVec, FeedbackManager bbf, BMLBlockPeg bmlBlockPeg, String bmlId, String id,
            PegBoard localPegBoard, AnimationPlayer aniPlayer)
    {
        super(bbf, bmlBlockPeg, bmlId, id, localPegBoard);
        this.aniPlayer = aniPlayer;

        System.out.println("ocVec size: " + ocVec.size());

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
        this.ocVec = new ArrayList<>(ocVec);
        createOcVecTimePegs();
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

    private static final int PALM_COLUMN = 0;
    private static final int EXT_FINGER_COLUMN = 1;
    private static final int THIRD_COLUMN = 2;

    // m is previous rotation
    private float[] getNextWristRot(float[] m, OrientConstraint oc)
    {
        float dOld[] = Vec3f.getVec3f();
        float pOld[] = Vec3f.getVec3f();

        Mat3f.getColumn(m, PALM_COLUMN, pOld);
        Mat3f.getColumn(m, EXT_FINGER_COLUMN, dOld);

        if (!Vec3f.epsilonEquals(oc.getD(), Vec3f.getZero(), PRECISION) && !Vec3f.epsilonEquals(oc.getP(), Vec3f.getZero(), PRECISION))
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

            if (joint.equals(Hanim.l_wrist))
            {
                Vec3f.scale(-1, p);
            }
            Vec3f.scale(-1, d);

            float v[] = Vec3f.getVec3f();
            Vec3f.cross(v, p, d);
            Vec3f.normalize(v);
            float m1[] = Mat3f.getMat3f();
            Mat3f.setColumn(m1, THIRD_COLUMN, v);
            Mat3f.setColumn(m1, PALM_COLUMN, p);
            Mat3f.setColumn(m1, EXT_FINGER_COLUMN, d);
            return m1;
        }

        else if (!Vec3f.epsilonEquals(oc.getD(), Vec3f.getZero(), PRECISION))
        // -- only efo given
        {
            // get transformation dOld -> d
            float d[] = Vec3f.getVec3f(oc.getD());
            Vec3f.scale(-1, d);
            Vec3f.normalize(d);
            Vec3f.normalize(dOld);

            float m1[] = Mat3f.getMat3f();
            float q[] = Quat4f.getQuat4f();
            Quat4f.setFromVectors(q, dOld, d);
            Mat3f.setFromQuatScale(m1, q, 1);
            Mat3f.mul(m1, m);
            return m1;
        }

        else if (!Vec3f.epsilonEquals(oc.getP(), Vec3f.getZero(), PRECISION))
        // -- only palm orientation given
        {

            // get transformation pOld -> p
            float p[] = Vec3f.getVec3f(oc.getP());
            if (joint.equals(Hanim.l_wrist))
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
            Mat3f.mul(m1, m);
            return m1;
        }
        return Mat3f.getIdentity();
    }

    @Override
    public List<String> getAvailableSyncs()
    {
        List<String> syncs = super.getAvailableSyncs();
        for (OrientConstraint oc : ocVec)
        {
            if (!syncs.contains(oc.getId()))
            {
                syncs.add(oc.getId());
            }
        }
        return syncs;
    }

    private void createOcVecTimePegs()
    {
        for (OrientConstraint oc : ocVec)
        {
            if (constraintMap.get(oc) == null)
            {
                TimePeg tp = new TimePeg(getBMLBlockPeg());
                constraintMap.put(oc, tp);
                setTimePeg(oc.getId(), tp);
            }
        }
    }

    @Override
    protected void setInternalStrokeTiming(double earliestStart)
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

        // set inner
        for (int i = 0; i < tpSet.size() - 1; i++)
        {
            OrientConstraint ocLeft = tpSet.get(i);
            OrientConstraint ocRight = tpSet.get(i + 1);
            TimePeg tpLeft = constraintMap.get(ocLeft);
            TimePeg tpRight = constraintMap.get(ocRight);
            double avgDur = (tpRight.getGlobalValue() - tpLeft.getGlobalValue()) / (ocVec.indexOf(ocRight) - ocVec.indexOf(ocLeft));
            double time = tpLeft.getGlobalValue();
            for (int j = ocVec.indexOf(ocLeft) + 1; j < ocVec.indexOf(ocRight); j++)
            {
                time += avgDur;
                constraintMap.get(ocVec.get(j)).setGlobalValue(time);
            }
        }

        // find average duration to use for outer
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
        double avgDur = getStrokeDuration() / (ocVec.size() - 1);
        if (segments > 0)
        {
            avgDur = totalDur / segments;
        }

        // set from right to end
        double time = constraintMap.get(tpSet.get(tpSet.size() - 1)).getGlobalValue();
        for (int j = ocVec.indexOf(tpSet.get(tpSet.size() - 1)) + 1; j < ocVec.size(); j++)
        {
            time += avgDur;
            constraintMap.get(ocVec.get(j)).setGlobalValue(time);
        }

        // set from left to start
        time = constraintMap.get(tpSet.get(0)).getGlobalValue();
        int nrOfSegments = ocVec.indexOf(tpSet.get(0));
        if (time - (nrOfSegments * avgDur) < TRANSITION_TIME + earliestStart)
        {
            avgDur = (time - TRANSITION_TIME) / nrOfSegments;
        }

        for (int j = ocVec.indexOf(tpSet.get(0)) - 1; j >= 0; j--)
        {
            time -= avgDur;
            constraintMap.get(ocVec.get(j)).setGlobalValue(time);
        }
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
        super.setTimePeg(syncId, peg);
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

    private static class OrientBound
    {
        int index;
        double time;
    }

    // from LMP_Orient::getLowerBoundOrient
    private OrientBound getLowerBoundOrient(double fTime)
    {
        OrientBound b = new OrientBound();
        int index = -1;
        b.time = orientVec.get(0).getTp().getGlobalValue();

        for (OrientPos p : orientVec)
        {
            if (p.getTp().getGlobalValue() > fTime)
            {
                break;
            }
            b.time = p.tp.getGlobalValue();
            index++;
        }
        b.index = index;
        return b;
    }

    // from LMP_Orient::getUpperBoundOrient
    private OrientBound getUpperBoundOrient(double fTime)
    {
        int index = orientVec.size();
        OrientBound upperBound = new OrientBound();
        upperBound.time = orientVec.get(orientVec.size() - 1).tp.getGlobalValue();

        ListIterator<OrientPos> pIter = orientVec.listIterator(orientVec.size());
        while (pIter.hasPrevious())
        {
            OrientPos p = pIter.previous();
            if (p.getTp().getGlobalValue() < fTime && p.getTp().getGlobalValue() != TimePeg.VALUE_UNKNOWN)
            {
                break;
            }
            upperBound.time = p.tp.getGlobalValue();
            index--;
        }
        upperBound.index = index;
        return upperBound;
    }

    // From LMP_Orient::getOrient
    private float[] getOrient(double time)
    {
        if (orientVec.isEmpty())
        {
            return Quat4f.getIdentity();
        }
        OrientBound uBound = getUpperBoundOrient(time);
        OrientBound lBound = getLowerBoundOrient(time);
        int i = lBound.index;
        int j = uBound.index;
        double fT = (time - lBound.time) / (uBound.time - lBound.time);

        // System.out.println("lower bound: "+ i+","+lBound.time +" upper bound: "+j+","+uBound.time +" fT="+fT);

        float[] rkTarget = Quat4f.getQuat4f();
        int point_numb = orientVec.size();
        if (j > point_numb)
        {
            rkTarget = orientVec.get(orientVec.size() - 1).q;

            return rkTarget; // fTime > iBound
        }
        if (i == point_numb - 1)
        {
            rkTarget = orientVec.get(i).getQ();
            return rkTarget; // fTime > iBound
        }

        // determine border quaternions
        // (rkO=i-1, rkP=i, rkQ=i+1, rkR=i+2)
        float[] rkO, rkP, rkQ, rkR, rkA, rkA1, rkB, rkB1, rkRes;
        rkP = orientVec.get(i).getQ();

        /* unused??
        if (i == 0) rkO = orientVec.get(i).getQ();
        else rkO = orientVec.get(i - 1).getQ();
        */
        
        if (i + 1 >= point_numb) rkQ = orientVec.get(point_numb - 1).getQ();
        else rkQ = orientVec.get(i + 1).getQ();
        
        /* unused??
        if (i + 2 >= point_numb) rkR = orientVec.get(point_numb - 1).getQ();
        else rkR = orientVec.get(i + 2).getQ();
        */
        
        rkTarget = rkQ;

        // interpolate between rkP and rkQ
        // cout << "interpolate between " << rkP << " and " << rkQ << endl;

        // -- slerp:
        float q[] = Quat4f.getQuat4f();
        Quat4f.interpolate(q, rkP, rkQ, (float) fT);
        return q;
    }

    @Override
    protected void playUnit(double time) throws TimedPlanUnitPlayException
    {
        float q[] = getOrient(time);
        VJoint vjRoot = aniPlayer.getVCurrPartBySid(Hanim.HumanoidRoot);
        VJoint vjWristCurr = aniPlayer.getVCurrPartBySid(joint);

        float qw[] = Quat4f.getQuat4f();
        float qp[] = Quat4f.getQuat4f();
        float q2[] = Quat4f.getQuat4f();
        Quat4f.set(qw, q);
        VJoint par = vjWristCurr.getParent();
        par.getPathRotation(vjRoot, qp);
        Quat4f.inverse(qp);
        Quat4f.mul(q2, qp, qw);
        VJoint vjWrist = aniPlayer.getVNextPartBySid(joint);
        vjWrist.setRotation(q2);
    }

    // Prepares a sequence of quaternions for interpolating
    // the assigned set of extended finger orientations.
    private void refine(float[] cQuat, float[] c)
    {
        float[] startRot = getNextWristRot(c, ocVec.get(0));
        float[] startQuat = Quat4f.getQuat4f();
        Quat4f.setFromMat3f(startQuat, startRot);

        // clear overall quaternion control points
        orientVec.clear();

        // start and first stroke quaternions already determined by 'activate()'
        orientVec.add(new OrientPos(getStartPeg(), cQuat));
        orientVec.add(new OrientPos(getStrokeStartPeg(), startQuat));

        // subsequent stroke orientations
        float rot[] = Mat3f.getMat3f();
        Mat3f.set(rot, startRot);

        for (int i = 1; i < ocVec.size(); i++)
        {
            // append shortest transformation that rotates dir onto ocVec.dir
            rot = getNextWristRot(rot, ocVec.get(i));
            float[] q = Quat4f.getQuat4f();
            Quat4f.setFromMat3f(q, rot);
            orientVec.add(new OrientPos(constraintMap.get(ocVec.get(i)), q));

        }

        // add start phase transition
        float d[] = Vec3f.getVec3f();
        float p[] = Vec3f.getVec3f();
        Mat3f.getColumn(c, PALM_COLUMN, p);
        Mat3f.getColumn(c, EXT_FINGER_COLUMN, d);
        OrientConstraint ocNew = new OrientConstraint("start", GStrokePhaseID.STP_PREP, d, p);
        ocVec.add(0, ocNew);
        constraintMap.put(ocNew, getStartPeg());
    }

    @Override
    protected void startUnit(double time) throws TimedPlanUnitPlayException
    {
        getStartPeg().setAbsoluteTime(true); // don't mess with start anymore!
        resolveTimePegs(time);
        float cQuat[] = Quat4f.getQuat4f();
        VJoint root = aniPlayer.getVCurrPartBySid(Hanim.HumanoidRoot);
        aniPlayer.getVCurrPartBySid(joint).getPathRotation(root, cQuat);

        float[] c = Mat3f.getMat3f();
        Mat3f.setFromQuatScale(c, cQuat, 1);
        refine(cQuat, c);

        feedback("start", time);
        super.startUnit(time);
    }

    @Override
    protected void stopUnit(double time) throws TimedPlanUnitPlayException
    {

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
        for (OrientConstraint oc : ocVec)
        {
            if (oc.getPhase() == GStrokePhaseID.STP_STROKE)
            {
                strokeDuration += TRANSITION_TIME;
            }
        }
        return strokeDuration;
    }

}
