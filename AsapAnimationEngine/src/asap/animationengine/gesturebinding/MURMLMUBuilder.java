package asap.animationengine.gesturebinding;

import java.util.ArrayList;
import java.util.List;

import asap.animationengine.motionunit.AnimationUnit;
import asap.motionunit.keyframe.KeyFrame;
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
                
                
                for(Frame f:ph.getFrames())
                {
                    int size = 0;                    
                    for(JointValue jv : f.getPosture().getJointValues())
                    {
                        size+=jv.getDofs().length;
                    }                    
                    float dofs[] = new float [size];
                    
                    int i=0;
                    for(JointValue jv : f.getPosture().getJointValues())
                    {
                        for(float fl: jv.getDofs())
                        {
                            dofs[i] = fl/100f;
                            i++;                                  
                        }                        
                    }
                    keyFrames.add(new KeyFrame(f.getFtime(), dofs));
                }
                
                //TODO: select interpolator
                //CubicSplineFloatInterpolator interp = new CubicSplineFloatInterpolator();
                //return new Keyframe(targets, interp, keyFrames, nrOfDofs);
                
                //TODO: generate motionunit
            }
        }
        return null;
    }
}
