/*******************************************************************************
 *******************************************************************************/
package asap.animationengine;

import hmi.animation.Hanim;
import hmi.animation.Skeleton;
import hmi.animation.SkeletonPose;
import hmi.animation.VJoint;
import hmi.animation.VObjectTransformCopier;
import hmi.mixedanimationenvironment.MixedAnimationPlayer;
import hmi.physics.PhysicalHumanoid;
import hmi.physics.PhysicalSegment;
import hmi.physics.controller.PhysicalController;
import hmi.physics.mixed.MixedSystem;
import hmi.util.PhysicsSync;
import hmi.worldobjectenvironment.WorldObjectManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import net.jcip.annotations.GuardedBy;
import asap.animationengine.mixed.MixedPlayer;
import asap.animationengine.mixed.MixedSystemGenerator;
import asap.animationengine.restpose.RestPose;
import asap.realizer.Player;
import asap.realizer.planunit.TimedPlanUnitState;

/**
 * Manages the execution of a plan containing TimedMotionUnits. The AnimationPlayer is different from the DefaultPlayer in that the TimedMotionUnits
 * act on a separate body rather than directly on the virtual human, and that their results are combined and applied to the virtual human at the end
 * of each play.
 * 
 * @author Herwin
 */
@Slf4j
public class AnimationPlayer2 implements Player, MixedAnimationPlayer
{
    private VJoint vPrev;
    private VJoint vCurr;
    private VJoint vNext;
    private PhysicalHumanoid pHuman;

    private VObjectTransformCopier votcCurrToPrev;
    private VObjectTransformCopier votcNextToCurr;
    private boolean prevValid;
    
    @GuardedBy("this")
    private List<PhysicalController> controllers = new ArrayList<PhysicalController>();
    
    private final AnimationPlanPlayer app;    

    private MixedPlayer mPlayer;
    private float stepTime;

    private Skeleton prevSkel, curSkel, nextSkel;
    private SkeletonPose vPrevStartPose;
    private SkeletonPose vCurrStartPose;
    private SkeletonPose vNextStartPose;
    private boolean prevValidOld = false;
    protected WorldObjectManager woManager;
    private MixedSystemGenerator mixedSystemGenerator;    
    private final float gravity[]={0,-9.8f,0}; 
    public RestPose getRestPose()
    {
        return app.getRestPose();
    }
    
    public AnimationPlayer2(VJoint vP, VJoint vC, VJoint vN, PhysicalHumanoid ph, float h, AnimationPlanPlayer planPlayer)
    {
        this(vP, vC, vN, ph, h, null, planPlayer);
    }

    public AnimationPlayer2(VJoint vP, VJoint vC, VJoint vN, PhysicalHumanoid ph, float h, WorldObjectManager wom,
            AnimationPlanPlayer planPlayer)
    {
        vPrev = vP;
        vCurr = vC;
        vNext = vN;
        prevSkel = new Skeleton("prevSkel", vPrev);
        curSkel = new Skeleton("curSkel", vCurr);
        nextSkel = new Skeleton("nextSkel", vNext);

        woManager = wom;
        //VJoint[] emptyArray = new VJoint[0];
        vPrevStartPose = new SkeletonPose("prev", prevSkel, "TR");
        vCurrStartPose = new SkeletonPose("cur",  curSkel, "TR");
        vNextStartPose = new SkeletonPose("next", nextSkel, "TR");
        vPrevStartPose.setFromTarget();
        vCurrStartPose.setFromTarget();
        vNextStartPose.setFromTarget();

        pHuman = ph;
        pHuman.setEnabled(true);

        votcNextToCurr = VObjectTransformCopier.newInstanceFromVJointTree(vNext, vCurr, "T1R");
        votcCurrToPrev = VObjectTransformCopier.newInstanceFromVJointTree(vCurr, vPrev, "T1R");

        mixedSystemGenerator = new MixedSystemGenerator(ph,gravity); 
        MixedSystem m = mixedSystemGenerator.generateMixedSystem("phDefault", new HashSet<String>(), new HashSet<String>(), vCurr);
        mPlayer = new MixedPlayer(m, vPrev, vCurr, vNext);
        
        mPlayer.setH(h);
        stepTime = h;
        app = planPlayer;
        prevValid = false;
    }

    @Override
    public synchronized void stopBehaviour(String bmlId, String id, double globalTime)
    {
        app.stopPlanUnit(bmlId, id, globalTime);        
    }
    
    @Override
    public synchronized void interruptBehaviour(String bmlId, String id, double globalTime)
    {
        app.interruptPlanUnit(bmlId, id, globalTime);        
    }

    public WorldObjectManager getWoManager()
    {
        return woManager;
    }

    /**
     * Resets all controllers, mixed systems (that is: restores their start physical humanoids and sets connector velocity to 0) and sets vPrev, vNext
     * and vCurr to their pose at initialization
     * 
     * Stops all timed motion units and removes them from the plan
     */
    @Override
    public synchronized void reset(double time)
    {
        prevValid = false;
        vPrevStartPose.setToTarget();
        vCurrStartPose.setToTarget();
        vNextStartPose.setToTarget();
        synchronized (PhysicsSync.getSync())
        {
            for (PhysicalController p : controllers)
            {
                p.reset();
            }
            /*
            for (MixedSystem ms : mSystems)
            {
                ms.reset(vNext);
            }
            */
        }
        app.reset(time);        
    }

    /**
     * Sets the reset pose to the current vNext. The reset pose gets set to all the mixed systems whenever the the reset function is called. Typical
     * workflow:<br>
     * 1. player = new AnimationPlayer(...<br>
     * 2. vNext = player.getVNext(); <br>
     * 3. set pose in vNext <br>
     * 4. vNext.calculateMatrices();<br>
     * 5. player.setResetPose();<br>
     */
    public synchronized void setResetPose()
    {
        vNextStartPose.setFromTarget();
        /*
        synchronized (PhysicsSync.getSync())
        {
            for (MixedSystem ms : mSystems)
            {
                ms.setResetPose(vNext);
                ms.reset(vNext);
            }
        }
        */
    }
    
    @Override
    public void play(double time){}
    
    @Override
    public synchronized void playStep(double prevTime)
    {
        log.debug("time {}",prevTime);
        
        controllers.clear();
        prevValidOld = prevValid;

        // play kinematics
        if (prevValid)
        {
            votcCurrToPrev.copyConfig();
            votcNextToCurr.copyConfig();
            app.play(prevTime);
        }
        else
        {
            app.play(prevTime);
            votcNextToCurr.copyConfig();
            votcCurrToPrev.copyConfig();
            prevValid = true;
        }

        ArrayList<String> controlledJoints = new ArrayList<String>();
        for (PhysicalController p : controllers)
        {
            for (String jid : p.getRequiredJointIDs())
            {
                if (!controlledJoints.contains(jid))
                {
                    controlledJoints.add(jid);
                }
            }
        }

        ArrayList<String> desiredJoints = new ArrayList<String>();
        for (PhysicalController p : controllers)
        {
            for (String jid : p.getDesiredJointIDs())
            {
                if (!desiredJoints.contains(jid) && !controlledJoints.contains(jid))
                {
                    desiredJoints.add(jid);
                }
            }
        }

        // The best matching mSystem is the one that has (in order of importance):
        // 1. All required joints for all controllers
        // 2. The most matching desired joints
        // 3. The least total number of physical joints
        // Note that desired physical joints, when selected, still override the kinematic motion on the same joint
        MixedSystem bestMatch = mPlayer.getSystem();
        //int jointsInPH = Integer.MAX_VALUE;
        //int nrOfDesiredJoints = -1;
        boolean foundMatch = false;
        bestMatch = mixedSystemGenerator.generateMixedSystem("ms", controlledJoints, desiredJoints, vCurr);
        
//        for (MixedSystem m : mSystems)
//        {
//            PhysicalHumanoid ph = m.getPHuman();
//            boolean foundAll = true;
//            for (String jid : controlledJoints)
//            {
//                boolean found = false;
//                if (ph.getRootSegment() != null && ph.getRootSegment().getSid().equals(jid))
//                {
//                    found = true;
//                    break;
//                }
//                for (PhysicalJoint pj : ph.getJoints())
//                {
//                    if (pj.getName().equals(jid))
//                    {
//                        found = true;
//                        break;
//                    }
//                }
//                if (!found)
//                {
//                    foundAll = false;
//                    break;
//                }
//            }
//            if (foundAll)
//            {
//                // all required joints found, check for most desired joints
//                int dJoints = 0;
//                for (String jid : desiredJoints)
//                {
//                    if (ph.getRootSegment() != null && ph.getRootSegment().getSid().equals(jid))
//                    {
//                        dJoints++;
//                        continue;
//                    }
//                    for (PhysicalJoint pj : ph.getJoints())
//                    {
//                        if (pj.getName().equals(jid))
//                        {
//                            dJoints++;
//                            break;
//                        }
//                    }
//                }
//                if (dJoints > nrOfDesiredJoints)
//                {
//                    nrOfDesiredJoints = dJoints;
//                    bestMatch = m;
//                    jointsInPH = ph.getJoints().size();
//                    foundMatch = true;
//                }
//                else if (dJoints == nrOfDesiredJoints)
//                {
//                    if (ph.getJoints().size() < jointsInPH)
//                    {
//                        bestMatch = m;
//                        jointsInPH = ph.getJoints().size();
//                        foundMatch = true;
//                    }
//                }
//            }
//        }
        // play physics
        synchronized (PhysicsSync.getSync())
        {
            if (!foundMatch)
            {
                log.warn("Could not find a mixed system that contains joints: {} ", controlledJoints);
            }

            if (bestMatch != mPlayer.getSystem())
            {
                log.debug("Switching system {}", prevTime);
                mPlayer.switchSystem(bestMatch, stepTime, vPrev, vCurr, vNext, prevValidOld);
                pHuman.setEnabled(false);
                pHuman = mPlayer.getSystem().getPHuman();
                pHuman.setEnabled(true);
                for (PhysicalController p : controllers)
                {
                    p.setPhysicalHumanoid(pHuman);
                }
            }
            mPlayer.play(stepTime);
            pHuman.updateCOM(stepTime);

            for (PhysicalController p : controllers)
            {
                p.setPhysicalHumanoid(pHuman); // Could be done more elegantly,
                                               // not needed to do this every
                                               // frame?
                // But at least ensures that the controller always runs on the
                // correct physical humanoid now.
                p.update(stepTime);
            }
        }
    }

    public synchronized void copyPhysics()
    {
        synchronized (PhysicsSync.getSync())
        {
            pHuman.copy();

            // copy configuration of physically steered joints on current to next, so we can use them in
            // world-IK with almost correct body postures on proc animation in the next animation step
            float q[] = new float[4];
            float tr[] = new float[3];
            for (PhysicalSegment ps : pHuman.getSegments())
            {
                VJoint vC = vCurr.getPart(ps.getSid());
                VJoint vN = vNext.getPart(ps.getSid());
                vC.getRotation(q);
                vN.setRotation(q);
                if (ps.getSid().equals(Hanim.HumanoidRoot))
                {
                    vC.getTranslation(tr);
                    vN.setTranslation(tr);
                }
            }
        }
    }

    public synchronized void addController(PhysicalController controller)
    {
        if (!controllers.contains(controller))
        {
            controllers.add(controller);
        }
    }
    
    /**
     * Get the set of joints that is to be animated
     * 
     * @return the vNext
     */
    public VJoint getVNext()
    {
        return vNext;
    }

    /**
     * Get the prev set of joints that is to be animated
     * 
     * @return the vPrev
     */
    public VJoint getVPrev()
    {
        return vPrev;
    }

    /**
     * Get the current set of joints that is to be animated
     * 
     * @return the vCurr
     */
    public VJoint getVCurr()
    {
        return vCurr;
    }

    /**
     * @return the pHuman
     */
    public synchronized PhysicalHumanoid getPHuman()
    {
        return pHuman;
    }

    /**
     * @return the stepTime
     */
    public synchronized float getStepTime()
    {
        return stepTime;
    }

    @Override
    public void setBMLBlockState(String bmlId, TimedPlanUnitState state)
    {
        app.setBMLBlockState(bmlId, state);        
    }

    @Override
    public void stopBehaviourBlock(String bmlId, double time)
    {
        app.stopBehaviourBlock(bmlId, time);        
    }
    
    @Override
    public void interruptBehaviourBlock(String bmlId, double time)
    {
        app.interruptBehaviourBlock(bmlId, time);        
    }

    @Override
    public void shutdown()
    {

    }
    
    public void verifyTime(double time)
    {
    }
    
    @Override
    public void updateTiming(String bmlId)
    {
        
    }
}
