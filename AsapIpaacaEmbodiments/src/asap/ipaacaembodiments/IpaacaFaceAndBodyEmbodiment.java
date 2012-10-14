package asap.ipaacaembodiments;

import hmi.animation.VJoint;
import hmi.animationembodiments.SkeletonEmbodiment;
import hmi.faceanimation.FaceController;
import hmi.faceembodiments.FaceEmbodiment;

/**
 * Steers a body and a face through an ipaaca renderer
 * @author hvanwelbergen
 * 
 */
public class IpaacaFaceAndBodyEmbodiment implements SkeletonEmbodiment, FaceEmbodiment
{
    private final IpaacaFaceEmbodiment faceEmbodiment;
    private final IpaacaBodyEmbodiment bodyEmbodiment;
    private final IpaacaEmbodiment ipaacaEmbodiment;
    private final String id;

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
