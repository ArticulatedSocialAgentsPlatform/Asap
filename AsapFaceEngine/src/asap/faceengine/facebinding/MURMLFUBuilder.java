package asap.faceengine.facebinding;

import java.util.ArrayList;
import java.util.List;

import asap.faceengine.faceunit.FaceUnit;
import asap.faceengine.faceunit.KeyframeMorphFU;
import asap.motionunit.keyframe.CubicSplineFloatInterpolator;
import asap.motionunit.keyframe.KeyFrame;
import asap.murml.Definition;
import asap.murml.Frame;
import asap.murml.JointValue;
import asap.murml.Phase;
import asap.murml.Posture;

/**
 * Creates a facial unit from a MURML description
 * @author hvanwelbergen
 *
 */
public final class MURMLFUBuilder
{
    private MURMLFUBuilder(){}
    
    public static FaceUnit setup(String murml)
    {
        Definition def = new Definition();
        def.readXML(murml);
        if(def.getKeyframing()!=null)
        {
            //XXX for now just generates a MU for the first phase
            Phase ph = def.getKeyframing().getPhases().get(0);
            if(ph.getFrames().size()>0)
            {
                Posture p0 = ph.getFrames().get(0).getPosture();
                
                List<String> targets = new ArrayList<String>();
                List<KeyFrame> keyFrames = new ArrayList<KeyFrame>();
                
                //XXX assumes that all frames have the same interpolation targets
                for(JointValue jv:p0.getJointValues())
                {                    
                    targets.add(jv.jointSid);                    
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
                            dofs[i] = fl;
                            i++;
                        }                        
                    }
                    keyFrames.add(new KeyFrame(f.getFtime(), dofs));
                }
                
                //TODO: select interpolator
                CubicSplineFloatInterpolator interp = new CubicSplineFloatInterpolator();
                interp.setKeyFrames(keyFrames,targets.size());
                return new KeyframeMorphFU(targets, interp);
            }
        }
        return null;
    }
}
