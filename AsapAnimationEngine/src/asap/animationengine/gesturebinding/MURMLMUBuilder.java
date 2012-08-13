package asap.animationengine.gesturebinding;

import hmi.math.Quat4f;

import java.util.ArrayList;
import java.util.List;

import asap.animationengine.keyframe.MURMLKeyframeMU;
import asap.animationengine.motionunit.AnimationUnit;
import asap.motionunit.keyframe.KeyFrame;
import asap.motionunit.keyframe.LinearQuatFloatInterpolator;
import asap.murml.Dynamic;
import asap.murml.Frame;
import asap.murml.JointValue;
import asap.murml.Keyframing;
import asap.murml.MURMLDescription;
import asap.murml.Phase;
import asap.murml.Posture;
import asap.timemanipulator.EaseInEaseOutManipulator;

/**
 * Creates an animation unit from a MURML description
 * @author hvanwelbergen
 */
public final class MURMLMUBuilder
{
    private MURMLMUBuilder()
    {
    }
    
    public static AnimationUnit setup(String murml)
    {
        MURMLDescription def = new MURMLDescription();
        def.readXML(murml);
        return setup(def);
    }
    
    public static AnimationUnit getKeyFramingMU(Keyframing kf)
    {

        // XXX for now just generates a MU for the first phase
        Phase ph = kf.getPhases().get(0);
        
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
                
                int i=0;
                for(JointValue jv : f.getPosture().getJointValues())
                {
                    float q[]=new float[4];
                    Quat4f.setFromRollPitchYawDegrees(q, jv.getDofs()[0], jv.getDofs()[1], jv.getDofs()[2]);
                    Quat4f.set(dofs, i*4, q,0);
                    i++;
                }
                keyFrames.add(new KeyFrame(f.getFtime(), dofs));
            }
            
            LinearQuatFloatInterpolator interp = new LinearQuatFloatInterpolator(); 
            interp.setKeyFrames(keyFrames, nrOfDofs);
            double scale = kf.getEasescale();
            double p = kf.getEaseturningpoint();
            return new MURMLKeyframeMU(targets, interp, new EaseInEaseOutManipulator(scale,p), keyFrames, nrOfDofs,
                    kf.isInsertStartframe());
        }
        return null;
    }
    
    public static AnimationUnit setup(MURMLDescription murmlDescription)
    {
        if(murmlDescription.getDynamic()!=null)
        {
            Dynamic dyn = murmlDescription.getDynamic();
            if (dyn.getKeyframing()!=null)
            {
                return getKeyFramingMU(dyn.getKeyframing());
            }
        }
        return null;
    }
}
