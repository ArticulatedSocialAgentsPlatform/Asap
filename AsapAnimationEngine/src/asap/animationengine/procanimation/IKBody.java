/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.procanimation;

import hmi.animation.AnalyticalIKSolver;
import hmi.animation.Hanim;
import hmi.animation.VJoint;
import hmi.math.Mat3f;
import hmi.math.Quat4f;
import hmi.math.Vec3f;

/**
 * IK manipulator for a Humanoid body Uses analytical IK to position the feet
 * and hands.
 * 
 * @author welberge
 */
public class IKBody
{
    private VJoint human; // /humanoid coupled to this body
    private VJoint root;
    private VJoint lhip;
    private VJoint lknee;
    private VJoint lankle;
    private VJoint rhip;
    private VJoint rknee;
    private VJoint rankle;
    private VJoint lshoulder;
    private VJoint lelbow;
    private VJoint lwrist;
    private VJoint rshoulder;
    private VJoint relbow;
    private VJoint rwrist;

    private AnalyticalIKSolver solverRFeet;
    private AnalyticalIKSolver solverLFeet;
    private AnalyticalIKSolver solverLHand;
    private AnalyticalIKSolver solverRHand;

    private float[] leftFoot = new float[3];
    private float[] rightFoot = new float[3];

    // temp vars
    private float temp[] = new float[3];
    private float q[] = new float[4];
    private float qW[] = new float[4];
    private float qWDes[] = new float[9];
    private float mW[] = new float[9];
    private float mWDes[] = new float[9];
    private float zAxis[] = new float[3];
    private float yAxis[] = new float[3];
    private float xAxis[] = new float[3];

    /**
     * Constructor
     * 
     * @param h
     *            humanoid
     */
    public IKBody(VJoint h)
    {
        human = h;
        lhip = human.getPart(Hanim.l_hip);
        lknee = human.getPart(Hanim.l_knee);
        lankle = human.getPart(Hanim.l_ankle);
        rhip = human.getPart(Hanim.r_hip);
        rknee = human.getPart(Hanim.r_knee);
        rankle = human.getPart(Hanim.r_ankle);
        lshoulder = human.getPart(Hanim.l_shoulder);
        lelbow = human.getPart(Hanim.l_elbow);
        lwrist = human.getPart(Hanim.l_wrist);
        rshoulder = human.getPart(Hanim.r_shoulder);
        relbow = human.getPart(Hanim.r_elbow);
        rwrist = human.getPart(Hanim.r_wrist);
        root = human.getPart(Hanim.HumanoidRoot);

        float sv[] = new float[3];
        float tv[] = new float[3];
        // setup left foot solver
        lknee.getPathTranslation(lhip, tv);
        lankle.getPathTranslation(lknee, sv);
        solverLFeet = new AnalyticalIKSolver(sv, tv, AnalyticalIKSolver.LimbPosition.LEG, (Vec3f.length(sv) + Vec3f.length(tv)) * 0.999f);

        // setup right foot solver
        rknee.getPathTranslation(rhip, tv);
        rankle.getPathTranslation(rknee, sv);
        solverRFeet = new AnalyticalIKSolver(sv, tv, AnalyticalIKSolver.LimbPosition.LEG, (Vec3f.length(sv) + Vec3f.length(tv)) * 0.999f);

        // left hand solver
        lelbow.getPathTranslation(lshoulder, tv);
        lwrist.getPathTranslation(lelbow, sv);
        solverLHand = new AnalyticalIKSolver(sv, tv, AnalyticalIKSolver.LimbPosition.ARM, (Vec3f.length(sv) + Vec3f.length(tv)) * 0.999f);

        // right hand solver
        relbow.getPathTranslation(rshoulder, tv);
        rwrist.getPathTranslation(relbow, sv);
        solverRHand = new AnalyticalIKSolver(sv, tv, AnalyticalIKSolver.LimbPosition.ARM, (Vec3f.length(sv) + Vec3f.length(tv)) * 0.999f);
    }

    /**
     * Set the translation of the HumanoidRoot
     * 
     * @param rootPos
     *            the translation of the HumanoidRoot
     */
    public void setRoot(float[] rootPos)
    {
        root.setTranslation(rootPos);
    }

    /**
     * Places the feet onto their previous position
     */
    public void placeFeet()
    {
        setFeet(leftFoot, rightFoot, true);
    }

    private void setFoot(float pos[], boolean keepFlat, VJoint hip, VJoint knee, VJoint ankle, AnalyticalIKSolver solver)
    {
        float qSho[] = new float[4];
        float qElb[] = new float[4];
        AnalyticalIKSolver.translateToLocalSystem(null, hip, pos, temp);
        solver.solve(temp, qSho, qElb);
        hip.setRotation(qSho);
        knee.setRotation(qElb);

        if (keepFlat)
        {
            knee.getPathRotation(null, qW);
            Mat3f.setFromQuatScale(mW, qW, 1f);
            Mat3f.getRow(mW, 0, xAxis);
            Mat3f.getRow(mW, 1, yAxis);
            Mat3f.getRow(mW, 2, zAxis);
            Vec3f.set(xAxis, xAxis[0], 0, xAxis[2]);
            if (Vec3f.lengthSq(xAxis) < 0.01)
            {
                Vec3f.set(xAxis, 1, 0, 0);
            }
            else
            {
                Vec3f.normalize(xAxis);
            }
            Vec3f.set(zAxis, zAxis[0], 0, zAxis[2]);
            if (Vec3f.lengthSq(xAxis) < 0.01)
            {
                Vec3f.set(zAxis, 0, 0, 1);
            }
            else
            {
                Vec3f.normalize(zAxis);
            }
            Vec3f.cross(yAxis, zAxis, xAxis);
            Mat3f.setRow(mWDes, 0, xAxis);
            Mat3f.setRow(mWDes, 1, yAxis);
            Mat3f.setRow(mWDes, 2, zAxis);
            Quat4f.set(qWDes, mWDes);
            Quat4f.inverse(qW);
            Quat4f.mul(q, qW, qWDes);
            ankle.setRotation(q);
        }
    }

    /**
     * Set the hip and knee rotation so that a certain left foot position is
     * satisfied.
     * 
     * @param l
     *            the left foot position, in world coordinates
     * @param keepFlat
     *            set if the foot should be kept flat on a horizontal floor (on
     *            the global X-Z plane). Modifies ankle rotation.
     */
    public void setLeftFoot(float l[], boolean keepFlat)
    {
        if (l != null)
        {
            Vec3f.set(leftFoot, l);
        }
        setFoot(l, keepFlat, lhip, lknee, lankle, solverLFeet);
    }

    /**
     * Set the hip and knee rotation so that a certain right foot position is
     * satisfied.
     * 
     * @param r
     *            the right foot position, in world coordinates
     * @param keepFlat
     *            set if the foot should be kept flat on the floor. Modifies
     *            ankle rotation.
     */
    public void setRightFoot(float r[], boolean keepFlat)
    {
        if (r != null)
        {
            Vec3f.set(rightFoot, r);
        }
        setFoot(r, keepFlat, rhip, rknee, rankle, solverRFeet);
    }

    /**
     * Sets the hip, knee and ankle rotations that satisfy a foot position
     * 
     * @param l
     *            left foot position, null for current pos
     * @param r
     *            right foot position, null for current pos
     * @param keepFlat
     *            , set to keep heels flat
     */
    public void setFeet(float[] l, float[] r, boolean keepFlat)
    {
        setLeftFoot(l, keepFlat);
        setRightFoot(r, keepFlat);
    }

    /**
     * Sets the shoulder and elbow rotations that satisfy the left hand position
     * in world coordinates
     * 
     * @param l
     *            left hand position
     */
    public void setLeftHand(float l[])
    {
        float left[] = new float[3];
        AnalyticalIKSolver.translateToLocalSystem(null, lshoulder, l, left);
        setLocalLeftHand(left);
    }

    /**
     * Sets the shoulder and elbow rotations that satisfy the right hand
     * position in world coordinates
     * 
     * @param l
     *            right hand position
     */
    public void setRightHand(float r[])
    {
        float[] right = new float[3];
        AnalyticalIKSolver.translateToLocalSystem(null, rshoulder, r, right);
        setLocalRightHand(right);
    }

    /**
     * Sets the shoulder and elbow rotations that satisfy the right hand
     * position in shoulder coordinates
     * 
     * @param r
     *            right hand position
     */
    public void setLocalRightHand(float r[])
    {
        float qSho[] = new float[4];
        float qElb[] = new float[4];
        solverRHand.solve(r, qSho, qElb);
        rshoulder.setRotation(qSho);
        relbow.setRotation(qElb);
    }

    /**
     * Sets the right hand swivel angle (see Tolani)
     * 
     * @param sw
     *            the swivel rotation
     */
    public void setSwivelRightHand(double sw)
    {
        solverRHand.setSwivel(sw);
    }

    /**
     * Set the swivel rotation of the left hand (see Tolani)
     * 
     * @param sw
     *            the swivel rotation of the left hand
     */
    public void setSwivelLeftHand(double sw)
    {
        solverLHand.setSwivel(sw);
    }

    /**
     * Set the swivel rotation of the right foot (see Tolani)
     * 
     * @param sw
     *            the swivel rotation of th right foot
     */
    public void setSwivelRightFoot(double sw)
    {
        solverRFeet.setSwivel(sw);
    }

    /**
     * Set the swivel rotation of the left foot (see Tolani)
     * 
     * @param sw
     *            the swivel rotation of the left foot
     */
    public void setSwivelLeftFoot(double sw)
    {
        solverLFeet.setSwivel(sw);
    }

    /**
     * Get the swivel of the right foot, given the current humanoid setup
     * 
     * @return the swivel of the right foot
     */
    public double getSwivelRightFoot()
    {
        float e[] = new float[3];
        float g[] = new float[3];

        float rAnkle[] = new float[3];
        float rKnee[] = new float[3];
        rankle.getPathTranslation(null, rAnkle);
        AnalyticalIKSolver.translateToLocalSystem(null, rhip, rAnkle, g);

        rknee.getPathTranslation(null, rKnee);
        AnalyticalIKSolver.translateToLocalSystem(null, rhip, rKnee, e);

        // System.out.println("e: "+e);
        // System.out.println("g: "+g);
        return -solverRFeet.getSwivel(e, g);
    }

    /**
     * Get the swivel of the left foot, given the current humanoid setup
     * 
     * @return the swivel of the left foot
     */
    public double getSwivelLeftFoot()
    {
        float e[] = new float[3];
        float g[] = new float[3];
        float lAnkle[] = new float[3];
        float lKnee[] = new float[3];
        lankle.getPathTranslation(null, lAnkle);
        AnalyticalIKSolver.translateToLocalSystem(null, lhip, lAnkle, g);

        lknee.getPathTranslation(null, lKnee);
        AnalyticalIKSolver.translateToLocalSystem(null, lhip, lKnee, e);

        // System.out.println("e: "+e);
        // System.out.println("g: "+g);
        return -solverLFeet.getSwivel(e, g);
    }

    /**
     * Get the swivel of the left foot, given the current humanoid setup
     * 
     * @return the swivel of the left foot
     */
    public double getSwivelLeftArm()
    {
        // float e[] = new float[3];
        float g[] = new float[3];
        float lWrist[] = new float[3];

        lwrist.getPathTranslation(null, lWrist);
        AnalyticalIKSolver.translateToLocalSystem(null, lshoulder, lWrist, g);

        float qSho[] = new float[4];
        lshoulder.getRotation(qSho);
        float qElb[] = new float[4];
        lelbow.getRotation(qElb);

        float lElbow[] = new float[3];
        float e[] = new float[3];
        lelbow.getPathTranslation(null, lElbow);
        AnalyticalIKSolver.translateToLocalSystem(null, lshoulder, lElbow, e);

        return solverLHand.getSwivelFromShoulderElbowAndGoal(qSho, qElb, g);
    }

    /**
     * Get the swivel of the right arm, given the current humanoid setup
     * 
     * @return the swivel of the right arm
     */
    public double getSwivelRightArm()
    {
        // float e[] = new float[3];
        float g[] = new float[3];
        float rWrist[] = new float[3];
        // float rElbow[] = new float[3];
        rwrist.getPathTranslation(null, rWrist);
        AnalyticalIKSolver.translateToLocalSystem(null, rshoulder, rWrist, g);
        float qSho[] = new float[4];
        rshoulder.getRotation(qSho);
        float qElb[] = new float[4];
        relbow.getRotation(qElb);
        /*
         * relbow.getPathTranslation(null, rElbow);
         * AnalyticalIKSolver.translateToLocalSystem(null, rshoulder, rElbow, e);
         * return solverRHand.getSwivel(e, g);
         */
        float rElbow[] = new float[3];
        float e[] = new float[3];
        relbow.getPathTranslation(null, rElbow);
        AnalyticalIKSolver.translateToLocalSystem(null, rshoulder, rElbow, e);
        
        return solverRHand.getSwivelFromShoulderElbowAndGoal(qSho, qElb, g);
    }

    /**
     * Calculates the rotations of hip and knee for given swivel and left foot
     * position
     * 
     * @param swivel
     *            swivel angle
     * @param lf
     *            left foot position in world coordinates
     * @param qh
     *            output: left hip rotation
     * @param qk
     *            output: left knee rotation
     */
    public void getLeftFootRotation(double swivel, float lf[], float qh[], float qk[])
    {
        float left[] = new float[3];
        AnalyticalIKSolver.translateToLocalSystem(null, lhip, lf, left);
        solverLFeet.setSwivel(swivel);
        solverLFeet.solve(left, qh, qk);
    }

    /**
     * Get the translation of the root
     * 
     * @param root
     *            vector to store the translation in, null to create it
     * @return the root translation
     */
    public void getRootPosition(float root[])
    {
        VJoint hRoot = human.getPart(Hanim.HumanoidRoot);
        hRoot.getPathTranslation(null, root);
    }

    /**
     * Calculates the rotations of hip and knee for given swivel and right foot
     * position
     * 
     * @param swivel
     *            swivel angle
     * @param lf
     *            right foot position in world coordinates
     * @param qh
     *            output: right hip rotation
     * @param qk
     *            output: right knee rotation
     */
    public void getRightFootRotation(double swivel, float rf[], float qh[], float qk[])
    {
        float right[] = new float[3];
        AnalyticalIKSolver.translateToLocalSystem(null, rhip, rf, right);
        solverRFeet.setSwivel(swivel);
        solverRFeet.solve(right, qh, qk);
    }

    /**
     * Sets the shoulder and elbow rotations that satisfy the left hand position
     * in shoulder coordinates
     * 
     * @param r
     *            right hand position
     */
    public void setLocalLeftHand(float l[])
    {
        float qSho[] = new float[4];
        float qElb[] = new float[4];
        solverLHand.solve(l, qSho, qElb);
        lshoulder.setRotation(qSho);
        lelbow.setRotation(qElb);
    }

    /**
     * @return Returns the human.
     */
    public VJoint getHuman()
    {
        return human;
    }
}
