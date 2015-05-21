/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.mixed;

import hmi.animation.VJoint;
import hmi.math.NumMath;
import hmi.math.Quat4f;
import hmi.math.Vec3f;
import hmi.math.Vec4f;
import hmi.physics.PhysicalHumanoid;
import hmi.physics.PhysicalJoint;
import hmi.physics.inversedynamics.IDSegment;
import hmi.physics.mixed.Branch;
import hmi.physics.mixed.MixedSystem;
import hmi.util.PhysicsSync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Manages the playback of motion on a mixedsystem
 * @author Herwin
 *
 */
public class MixedPlayer
{
    private Logger logger = LoggerFactory.getLogger(MixedPlayer.class.getName());
    
    private VJoint[] hPrevS;
    private VJoint[] hCurrS;
    private VJoint[] hNextS;

    private float qPrev[];
    private float qCurr[];
    private float qNext[];

    private float qDiff[];
    private float qDiff2[];
    private float w[];
    private float wDiff[];
    private float q[] = new float[4];
    private float q1[] = new float[4];
    private float q2[] = new float[4];
    private float v[] = new float[3];
    private float v1[] = new float[3];
    private float v2[] = new float[3];

    private MixedSystem system;
    private int kinJoints = 0;
    private float h;

    // public static final float H = 10f/1000f;
    public static final float DEFAULTH = 1f / 1000f;

    public MixedPlayer(MixedSystem sys, VJoint hPrev, VJoint hCurr, VJoint hNext)
    {
        setSystem(sys, hPrev, hCurr, hNext);
        h = DEFAULTH;
    }

    private void setPrevCurrNextFromPhysicalHuman(PhysicalHumanoid ph,
            VJoint vPrev, VJoint vCurr, VJoint vNext)
    {
        float[] w = new float[3];
        // float[] vel = new float[3];
        for (PhysicalJoint pj : ph.getJoints())
        {
            if (system.getPHuman().getJoint(pj.getName()) == null)
            {
                // Not in a physical joint of the new system, set pos and vel in
                // vNext, vCurr, vNext
                synchronized (PhysicsSync.getSync())
                {
                    pj.getRotation(q);
                    pj.getAngularVelocity(w);
                }
                vNext.getPart(pj.getName()).setRotation(q);

                logger.debug("Setting rotation of {} {}", pj.getName(), Quat4f.toString(q));

                // extrapolate previous positions using the angular velocity
                float theta = Vec3f.length(w) * h;
                if (theta > 0)
                {
                    Vec3f.normalize(w);
                    Vec3f.scale((float) -Math.sin(theta * 0.5), w);
                    Quat4f.set(q1, (float) Math.cos(theta * 0.5), w[0], w[1],
                            w[2]);
                    Quat4f.mul(q2, q, q1);
                    vCurr.getPart(pj.getName()).setRotation(q2);
                    Quat4f.mul(q2, q1);
                    vPrev.getPart(pj.getName()).setRotation(q2);
                } else
                {
                    vCurr.getPart(pj.getName()).setRotation(q);
                    vPrev.getPart(pj.getName()).setRotation(q);
                }
            }
        }
        if (ph.getRootSegment() != null)
        {
            if (system.getPHuman().getSegment(ph.getRootSegment().getSid()) == null)
            {
                synchronized (PhysicsSync.getSync())
                {
                    ph.getRootSegment().box.getRotation(q);
                    ph.getRootSegment().getAngularVelocity(w);
                    ph.getRootSegment().box.getTranslation(v2);
                }
                vNext.getPart(ph.getRootSegment().getSid()).setRotation(q);

                float theta = Vec3f.length(w) * h;
                if (theta > 0)
                {
                    Vec3f.normalize(w);
                    Vec3f.scale((float) -Math.sin(theta * 0.5), w);
                    Quat4f.set(q1, (float) Math.cos(theta * 0.5), w[0], w[1],
                            w[2]);
                    Quat4f.mul(q2, q, q1);
                    vCurr.getPart(ph.getRootSegment().getSid()).setRotation(q2);
                    Quat4f.mul(q2, q1);
                    vPrev.getPart(ph.getRootSegment().getSid()).setRotation(q2);
                } else
                {
                    vCurr.getPart(ph.getRootSegment().getSid()).setRotation(q);
                    vPrev.getPart(ph.getRootSegment().getSid()).setRotation(q);
                }

                // TODO: set velocity in prev curr next
                Vec3f.set(v1, ph.getRootSegment().startJointOffset);
                Quat4f.transformVec3f(q, v1);
                Vec3f.add(v, v1, v2);
                vPrev.getPart(ph.getRootSegment().getSid()).setTranslation(v);
                vCurr.getPart(ph.getRootSegment().getSid()).setTranslation(v);
                vNext.getPart(ph.getRootSegment().getSid()).setTranslation(v);
                // System.out.println("Setting rotation and translation to "+ph.rootSegment.getName());
            }
        }
        /*
         * int i=0; for(Branch b:system.getBranches()) { for(IDSegment
         * seg:b.idBranch.getSegments()) { PhysicalJoint pj =
         * ph.getJoint(seg.name); if(pj!=null) {
         * synchronized(PhysicsSync.getSync()) { pj.getRotation(q);
         * pj.getAngularVelocity(w); } hNextS[i].setRotation(q);
         * 
         * 
         * System.out.println("Setting rotation of "+seg.name+" "+Quat4f.toString
         * (q));
         * 
         * //extrapolate previous positions using the angular velocity float
         * theta = Vec3f.length(w)*h; if(theta>0) { Vec3f.normalize(w);
         * Vec3f.scale((float)-Math.sin(theta*0.5),w); Quat4f.set(q1,
         * (float)Math.cos(theta*0.5), w[0], w[1], w[2]); Quat4f.mul(q2, q, q1);
         * hCurrS[i].setRotation(q2);
         * //System.out.println("curr: "+Quat4f.toString(q2));
         * Quat4f.mul(q2,q1); hPrevS[i].setRotation(q2);
         * //System.out.println("prev: "+Quat4f.toString(q2)); } else {
         * hCurrS[i].setRotation(q); hPrevS[i].setRotation(q); } }
         * PhysicalSegment ps = ph.getSegment(seg.name); if(ps!=null) { if(ps ==
         * ph.rootSegment) { synchronized(PhysicsSync.getSync()) {
         * ps.box.getRotation(q); ps.box.getTranslation(v2); }
         * hPrevS[i].setRotation(q); hCurrS[i].setRotation(q);
         * hNextS[i].setRotation(q);
         * 
         * Vec3f.set(v1, ps.startJointOffset); Quat4f.transformVec3f(q, v1);
         * Vec3f.add(v,v1,v2); hPrevS[i].setTranslation(v);
         * hCurrS[i].setTranslation(v); hNextS[i].setTranslation(v);
         * System.out.println("Setting rotation and translation to "+seg.name);
         * } } i++; } }
         */
    }

    /**
     * Sets up the system, based on a new system
     * 
     * @param sys
     *            new system
     * @param hPrev
     *            joint trees to find kinematically steered joints in
     * @param hCurr
     * @param hNext
     */
    public void setSystem(MixedSystem sys, VJoint hPrev, VJoint hCurr,
            VJoint hNext)
    {
        system = sys;
        int i = 0;

        // find the VObjects that are kinematically steered
        for (Branch b : system.getBranches())
        {
            i += b.idBranch.getSegments().length;
        }
        kinJoints = i;
        hPrevS = new VJoint[kinJoints];
        hCurrS = new VJoint[kinJoints];
        hNextS = new VJoint[kinJoints];

        i = 0;
        for (Branch b : system.getBranches())
        {
            for (IDSegment seg : b.idBranch.getSegments())
            {
                // System.out.println(seg.name);
                // System.out.println(hPrev);

                hPrevS[i] = hPrev.getPart(seg.name);
                hCurrS[i] = hCurr.getPart(seg.name);
                hNextS[i] = hNext.getPart(seg.name);
                i++;
            }
        }

        qPrev = new float[4 * kinJoints];
        qCurr = new float[4 * kinJoints];
        qNext = new float[4 * kinJoints];
        qDiff = new float[4 * kinJoints];
        qDiff2 = new float[4 * kinJoints];
        w = new float[3 * kinJoints];
        wDiff = new float[3 * kinJoints];
    }

    /**
     * Get the simulation rate
     */
    public float getH()
    {
        return h;
    }

    /**
     * Set the simulation rate
     */
    public void setH(float hNew)
    {
        h = hNew;
    }

    /**
     * Sets the massoffset of the physical humanoid to the one defined by its
     * current joints
     */
    public void setMassOffset()
    {
        for (int i = 0; i < kinJoints; i++)
        {
            hCurrS[i].getRotation(qCurr, i * 4);
        }
        system.setMassOffset(qCurr);
    }

    public void reset(VJoint vj)
    {
        system.reset(vj);
    }

    /**
     * Calculates joint rotations, joint angular velocity and joint angular
     * acceleration from the prev, current, next joint configuration
     */
    private void calculateKinematics()
    {
        for (int i = 0; i < kinJoints; i++)
        {
            hPrevS[i].getRotation(qPrev, i * 4);
            hCurrS[i].getRotation(qCurr, i * 4);
            if (Vec4f.dot(qPrev, i * 4, qCurr, i * 4) < 0)
            {
                Vec4f.scale(-1, qCurr, i * 4);
                logger.debug("Flipping cur quat");
            }
            hNextS[i].getRotation(qNext, i * 4);
            if (Vec4f.dot(qCurr, i * 4, qNext, i * 4) < 0)
            {
                Vec4f.scale(-1, qNext, i * 4);
                logger.debug("Flipping next quat");
            }
        }

        for (int i = 0; i < kinJoints * 4; i++)
        {
            qDiff[i] = NumMath.diff(qPrev[i], qNext[i], h);
            qDiff2[i] = NumMath.diff2(qPrev[i], qCurr[i], qNext[i], h);
        }
        for (int i = 0; i < kinJoints; i++)
        {
            Quat4f.setAngularVelocityFromQuat4f(w, i * 3, qCurr, i * 4, qDiff,
                    i * 4);
            Quat4f.setAngularAccelerationFromQuat4f(wDiff, i * 3, qCurr, i * 4,
                    qDiff2, i * 4);
            /*
             * System.out.println("qPrev("+i+"): "+Quat4f.toString(qPrev,i*4)+", "
             * +Vec4f.length(qPrev));
             * System.out.println("qCurr("+i+"): "+Quat4f.
             * toString(qCurr,i*4)+", "+Vec4f.length(qCurr));
             * System.out.println(
             * "qNext("+i+"): "+Quat4f.toString(qNext,i*4)+", "
             * +Vec4f.length(qNext));
             * System.out.println("qDiff("+i+"): "+Quat4f.toString(qDiff,i*4));
             * System
             * .out.println("qDiff2("+i+"): "+Quat4f.toString(qDiff2,i*4));
             * System.out.println("aacc("+i+"): "+Vec3f.length(wDiff,i*3));
             * System.out.println("avel("+i+"): "+Vec3f.length(w,i*3));
             */
        }
    }

    /**
     * Switches the current mixed system to sys
     * 
     * @param sys
     *            system to switch to
     * @param timeDiff
     *            time delta since last update
     */
    public void switchSystem(MixedSystem sys, float timeDiff, VJoint prev,
            VJoint curr, VJoint next, boolean switchPrevCurrNext)
    {
        PhysicalHumanoid ph = system.getPHuman();
        calculateKinematics();
        sys.set(system, qCurr, w);
        setSystem(sys, prev, curr, next);
        if (switchPrevCurrNext)
        {
            setPrevCurrNextFromPhysicalHuman(ph, prev, curr, next);
        }
    }

    public void play(float timeDiff)
    {
        calculateKinematics();
        system.time(timeDiff, qCurr, w, wDiff);
    }

    /**
     * @return the system
     */
    public MixedSystem getSystem()
    {
        return system;
    }
}
