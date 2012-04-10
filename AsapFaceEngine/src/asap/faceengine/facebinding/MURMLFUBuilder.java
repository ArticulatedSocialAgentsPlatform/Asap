package asap.faceengine.facebinding;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import asap.faceengine.faceunit.FaceUnit;
import asap.faceengine.faceunit.KeyframeMorphFU;
import asap.motionunit.keyframe.CubicSplineFloatInterpolator;
import asap.motionunit.keyframe.Interpolator;
import asap.motionunit.keyframe.KeyFrame;
import asap.motionunit.keyframe.LinearFloatInterpolator;
import asap.murml.Definition;
import asap.murml.Frame;
import asap.murml.JointValue;
import asap.murml.Phase;
import asap.murml.Posture;
import asap.timemanipulator.EaseInEaseOutManipulator;

/**
 * Creates a facial unit from a MURML description
 * @author hvanwelbergen
 * 
 */
@Slf4j
public final class MURMLFUBuilder
{
    private MURMLFUBuilder()
    {
    }

    public static FaceUnit setup(Definition murmlDefinition)
    {
        if (murmlDefinition.getKeyframing() != null)
        {
            // XXX for now just generates a MU for the first phase
            Phase ph = murmlDefinition.getKeyframing().getPhases().get(0);
            if (ph.getFrames().size() > 0)
            {
                Posture p0 = ph.getFrames().get(0).getPosture();

                List<String> targets = new ArrayList<String>();
                List<KeyFrame> keyFrames = new ArrayList<KeyFrame>();

                int nrOfDofs = 0;
                // XXX assumes that all frames have the same interpolation targets
                for (JointValue jv : p0.getJointValues())
                {
                    targets.add(jv.jointSid);
                    nrOfDofs++; // only one dof per 'joint' in face units
                }

                for (Frame f : ph.getFrames())
                {
                    int size = 0;
                    for (JointValue jv : f.getPosture().getJointValues())
                    {
                        size += jv.getDofs().length;
                    }
                    float dofs[] = new float[size];

                    int i = 0;
                    for (JointValue jv : f.getPosture().getJointValues())
                    {
                        for (float fl : jv.getDofs())
                        {
                            dofs[i] = fl / 100f;
                            i++;
                            break; // ignore all 'joint' values beyond the first
                        }
                    }
                    keyFrames.add(new KeyFrame(f.getFtime(), dofs));
                }
                
                Interpolator interp;
                switch (murmlDefinition.getKeyframing().getMode())
                {
                case SPLINE:
                    interp = new CubicSplineFloatInterpolator();
                    break;
                case QUATERNION:
                    log.warn("Invalid mode QUATERNION in MURML description of faceunit");
                    return null;                    
                case LINEAR:
                case RAW:
                default:
                    interp = new LinearFloatInterpolator();
                }

                double scale = murmlDefinition.getKeyframing().getEasescale();
                double p = murmlDefinition.getKeyframing().getEaseturningpoint();
                return new KeyframeMorphFU(targets, interp, new EaseInEaseOutManipulator(scale, p), keyFrames, nrOfDofs, murmlDefinition
                        .getKeyframing().isInsertStartframe());
            }
        }
        return null;
    }

    public static FaceUnit setup(String murml)
    {
        Definition def = new Definition();
        def.readXML(murml);
        return setup(def);
    }
}
