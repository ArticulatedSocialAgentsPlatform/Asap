package asap.animationengine.gaze;
/*******************************************************************************
 *******************************************************************************/
import hmi.animation.Hanim;
import hmi.math.Quat4f;
import hmi.math.Vec3f;
import hmi.neurophysics.EyeSaturation;
import hmi.neurophysics.ListingsLaw;
import hmi.neurophysics.Saccade;

import java.util.Set;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.motionunit.MUPlayException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;

import com.google.common.collect.ImmutableSet;

/**
 * Constant velocity saccade to target.
 * Gaze is on target at ready, moves back to rest position at relax.
 * @author Herwin van Welbergen
 */
public class EyeGazeMU extends TweedGazeMU
{
    private float qEyeLeft[] = Quat4f.getQuat4f();
    private float qEyeRight[] = Quat4f.getQuat4f();
    
    @Override    
    public EyeGazeMU copy(AnimationPlayer p) throws MUSetupException
    {
        EyeGazeMU gmu = new EyeGazeMU();
        gmu.lEye = p.getVNextPartBySid(Hanim.l_eyeball_joint);
        gmu.rEye = p.getVNextPartBySid(Hanim.r_eyeball_joint);
        gmu.lEyeCurr = p.getVCurrPartBySid(Hanim.l_eyeball_joint);
        gmu.rEyeCurr = p.getVCurrPartBySid(Hanim.r_eyeball_joint);
        if(gmu.lEye == null || gmu.rEye==null)
        {
            throw new MUSetupException("Eyegaze MU requested, but no eyeball joint in skeleton.",this);
        }
        gmu.player = p;
        gmu.woManager = p.getWoManager();
        gmu.target = target;
        gmu.offsetAngle = offsetAngle;
        gmu.offsetDirection = offsetDirection;
        return gmu;
    }

    @Override
    public TimedAnimationMotionUnit createTMU(FeedbackManager bfm, BMLBlockPeg bmlBlockPeg,String bmlId, String id, PegBoard pb)
    {
        return new GazeTMU(bfm,bmlBlockPeg,bmlId, id, this,pb, player);
    }
    
    @Override
    public void setEndRotation(float[] gazeDir)
    {
        woTarget.getTranslation2(gazeDir, rEye);
        Quat4f.transformVec3f(getOffsetRotation(), gazeDir);
        Vec3f.normalize(gazeDir);
        float q[]=Quat4f.getQuat4f();
        ListingsLaw.listingsEye(gazeDir, q);
        EyeSaturation.sat(q, Quat4f.getIdentity(), qEyeRight);
        
        woTarget.getTranslation2(gazeDir, lEye);
        Quat4f.transformVec3f(getOffsetRotation(), gazeDir);
        Vec3f.normalize(gazeDir);        
        ListingsLaw.listingsEye(gazeDir, q);
        
        EyeSaturation.sat(q, Quat4f.getIdentity(), qEyeLeft);
    }
    
    public double getPreferedReadyDuration()
    {
        float q[]=Quat4f.getQuat4f();
        Quat4f.mulConjugateRight(q, qStartLeftEye, qEyeLeft);
        float angle = Quat4f.getAngle(q);
        if(angle<0)angle = -angle;
        if(angle>Math.PI) angle-=Math.PI;
        return Saccade.getSaccadeDuration(angle);
    }
    
    
    
    @Override
    public void play(double t) throws MUPlayException
    {
        setEndEyeRotation(lEyeCurr, qEyeLeft);
        setEndEyeRotation(rEyeCurr, qEyeRight);
        
        float qLeft[]=Quat4f.getQuat4f();
        float qRight[]=Quat4f.getQuat4f();
        
        
        if (t < RELATIVE_READY_TIME)
        {
            float relT = (float)t/(float)RELATIVE_READY_TIME;
            Quat4f.interpolate(qLeft, qStartLeftEye, qEyeLeft, relT);            
            Quat4f.interpolate(qRight, qStartRightEye, qEyeRight, relT);
        }
        else
        {
            Quat4f.set(qLeft,qEyeLeft);
            Quat4f.set(qRight,qEyeRight);
        }
        rEye.setRotation(qRight);        
        lEye.setRotation(qLeft);                
    }        
    
    private static final Set<String>KINJOINTS = ImmutableSet.of(Hanim.l_eyeball_joint, Hanim.r_eyeball_joint);    
    
    @Override
    public Set<String> getKinematicJoints()
    {
        return KINJOINTS;        
    }  
}
