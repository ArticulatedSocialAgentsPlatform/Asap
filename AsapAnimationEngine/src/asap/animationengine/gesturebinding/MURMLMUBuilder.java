package asap.animationengine.gesturebinding;

import hmi.math.Quat4f;

import java.util.ArrayList;
import java.util.List;

import asap.animationengine.keyframe.MURMLKeyframeMU;
import asap.animationengine.motionunit.AnimationUnit;
import asap.motionunit.keyframe.KeyFrame;
import asap.motionunit.keyframe.LinearQuatFloatInterpolator;
import asap.murml.Definition;
import asap.murml.Frame;
import asap.murml.JointValue;
import asap.murml.Phase;
import asap.murml.Posture;

/**
 * Creates an animation unit from a MURML description
 * @author hvanwelbergen
 */
public final class MURMLMUBuilder
{
    private MURMLMUBuilder()
    {
    }

    public static AnimationUnit setup(Definition murmlDefinition)
    {
        if (murmlDefinition.getKeyframing() != null)
        {
            // XXX for now just generates a MU for the first phase
            Phase ph = murmlDefinition.getKeyframing().getPhases().get(0);
            
            if(ph.getFrames().size()>0)
            {
                Posture p0 = ph.getFrames().get(0).getPosture();
                
                List<String> targets = new ArrayList<String>();
                List<KeyFrame> keyFrames = new ArrayList<KeyFrame>();
                
                int nrOfDofs = 0;
                //XXX assumes that all frames have the same interpolation targets
                for(JointValue jv:p0.getJointValues())
                {                    
                    targets.add(jv.jointSid);
                    nrOfDofs+=jv.getDofs().length;     
                }          
                
                nrOfDofs = (nrOfDofs*4)/3;
                for(Frame f:ph.getFrames())
                {
                    int size = 0;                    
                    for(JointValue jv : f.getPosture().getJointValues())
                    {
                        size+=jv.getDofs().length;
                    }
                    float dofs[] = new float [(size*4)/3];
                    
                    for(JointValue jv : f.getPosture().getJointValues())
                    {
                        for(int i=0;i<size/3;i++)
                        {
                            float q[]=new float[4];
                            Quat4f.setFromRollPitchYawDegrees(q, jv.getDofs()[i*3], jv.getDofs()[i*3+1], jv.getDofs()[i*3+2]);
                            Quat4f.set(dofs, i*4, q,0);
                        }
                    }
                    keyFrames.add(new KeyFrame(f.getFtime(), dofs));
                }
                
                LinearQuatFloatInterpolator interp = new LinearQuatFloatInterpolator(); 
                interp.setKeyFrames(keyFrames, nrOfDofs);
                return new MURMLKeyframeMU(targets, interp, keyFrames, nrOfDofs);
            }
        }
        return null;
    }
}
