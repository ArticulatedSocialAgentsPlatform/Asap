package asap.ipaacaembodiments;

import hmi.animation.Hanim;
import hmi.animation.VJoint;
import hmi.environment.bodyandfaceembodiments.BodyAndFaceEmbodiment;
import hmi.faceanimation.FaceController;
import hmi.faceembodiments.EyelidMorpherEmbodiment;
import hmi.math.Quat4f;

import java.util.ArrayList;

import lombok.Setter;

/**
 * Steers a body and a face through an ipaaca renderer
 * @author hvanwelbergen
 * 
 */
public class IpaacaFaceAndBodyEmbodiment implements BodyAndFaceEmbodiment
{
    private final IpaacaFaceEmbodiment faceEmbodiment;
    private final IpaacaBodyEmbodiment bodyEmbodiment;
    private final IpaacaEmbodiment ipaacaEmbodiment;
    private final String id;

    @Setter
    private EyelidMorpherEmbodiment eyelidMorpher = new EyelidMorpherEmbodiment(new ArrayList<String>());

    public IpaacaFaceAndBodyEmbodiment(String id, IpaacaEmbodiment ipaacaEmbodiment, IpaacaFaceEmbodiment faceEmbodiment,
            IpaacaBodyEmbodiment bodyEmbodiment)
    {
        this.id = id;
        this.faceEmbodiment = faceEmbodiment;
        this.bodyEmbodiment = bodyEmbodiment;
        this.ipaacaEmbodiment = ipaacaEmbodiment;
    }

    @Override
    public void copy()
    {
        VJoint vjRightEye = getAnimationVJoint().getPartBySid(Hanim.r_eyeball_joint);
        VJoint vjLeftEye = getAnimationVJoint().getPartBySid(Hanim.l_eyeball_joint);
        if (vjRightEye != null && vjLeftEye != null)
        {
            float qRight[] = Quat4f.getQuat4f();
            float qLeft[] = Quat4f.getQuat4f();
            vjRightEye.getRotation(qRight);
            vjLeftEye.getRotation(qLeft);
            eyelidMorpher.setEyeLidMorph(qLeft, qRight, faceEmbodiment.getFaceController());
        }
        ipaacaEmbodiment.setJointData(bodyEmbodiment.getJointMatrices(), faceEmbodiment.getDesiredMorphTargets());
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public FaceController getFaceController()
    {
        return faceEmbodiment.getFaceController();
    }

    @Override
    public VJoint getAnimationVJoint()
    {
        return bodyEmbodiment.getAnimationVJoint();
    }
}
