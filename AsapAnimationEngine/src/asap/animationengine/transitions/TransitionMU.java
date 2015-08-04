/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.transitions;

import hmi.animation.VJoint;

import java.util.ArrayList;
import java.util.List;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.KeyPosition;
import asap.realizer.planunit.KeyPositionManager;
import asap.realizer.planunit.KeyPositionManagerImpl;
import asap.realizer.planunit.ParameterNotFoundException;


/**
 * Transition MotionUnits are used to create transitions between other MotionUnit
 * types. They interpolate between the final state (could be position and velocity) of one MotionUnit
 * and the predicted initial state of another motion unit. Transition Motion-
 * Units are specified solely by their start and end time and the set of joints they act
 * upon. At animation time, the start pose is taken from the current joint configuration
 * of the virtual human at the moment that the transition MotionUnit starts. The
 * end pose is determined by an Animation Predictor. The Animation Predictor uses a
 * copy of the motor plan containing only the predictable MotionUnits of the original
 * plan. Predictable MotionUnits are those MotionUnits that deterministically define
 * the pose they set at any given time (for now, only procedural MotionUnits).
 * @author welberge
 */
public abstract class TransitionMU implements AnimationUnit
{
    protected List<VJoint> joints;
    private double prefDuration = 2;
    private KeyPositionManager keyPositionManager = new KeyPositionManagerImpl();
    protected AnimationPlayer aniPlayer;
    
    public void addKeyPosition(KeyPosition kp)
    {
        keyPositionManager.addKeyPosition(kp);
    }

    public List<KeyPosition> getKeyPositions()
    {
        return keyPositionManager.getKeyPositions();
    }

    public void setKeyPositions(List<KeyPosition> p)
    {
        keyPositionManager.setKeyPositions(p);
    }

    public KeyPosition getKeyPosition(String id)
    {
        return keyPositionManager.getKeyPosition(id);
    }

    public void removeKeyPosition(String id)
    {
        keyPositionManager.removeKeyPosition(id);
    }

    @Override
    public abstract TransitionMU copy(AnimationPlayer player);

    public TransitionMU()
    {
        super();
        addKeyPosition(new KeyPosition("start",0,1));
        addKeyPosition(new KeyPosition("end",1,1));
    }
    
    @Override
    public TimedAnimationMotionUnit createTMU(FeedbackManager bfm, BMLBlockPeg bbPeg, String bmlId, String id, PegBoard pb)
    {
        return new TimedAnimationMotionUnit(bfm, bbPeg, bmlId, id, this, pb, aniPlayer);
    }

    @Override
    public double getPreferedDuration()
    {
        return prefDuration;
    }

    @Override
    public abstract void play(double t);

    public abstract void setStartPose();
    
    @Override
    public void setFloatParameterValue(String name, float value) throws ParameterNotFoundException
    {
        throw new ParameterNotFoundException(name);
    }

    @Override
    public void setParameterValue(String name, String value) throws ParameterNotFoundException
    {
        if (name.equals("joints"))filterJoints(value);
        else throw new ParameterNotFoundException(name);
    }
    
    @Override
    public float getFloatParameterValue(String name) throws ParameterNotFoundException
    {
        throw new ParameterNotFoundException(name);
    }
    
    @Override
    public String getParameterValue(String name) throws ParameterNotFoundException
    {
        StringBuffer str = new StringBuffer();
        if (name.equals("joints"))
        {
            for (VJoint vj : joints)
            {
                if (!str.toString().equals(""))
                {
                    str.append(" ");                    
                }
                str.append(vj.getSid());                
            }
            return str.toString();
        }
        throw new ParameterNotFoundException(name);
    }
    
    protected void filterJoints(String value)
    {
        String names = value.replace(",", " ");
        String[] jointNames = names.split("\\s");
        ArrayList<VJoint> newJoints = new ArrayList<VJoint>();
        for (VJoint vj : joints)
        {
            for (String jn : jointNames)
            {
                if (vj.getSid() != null)
                {
                    if (vj.getSid().equals(jn))
                    {
                        newJoints.add(vj);
                    }
                }
            }
        }
        joints.clear();
        joints.addAll(newJoints);
    }    
}
