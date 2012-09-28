package asap.ipaacaembodiments;

import hmi.animation.VJoint;
import hmi.animation.VJointUtils;
import hmi.animationembodiments.SkeletonEmbodiment;

/**
 * Obtains the VJoint embodiment from an IpaacaEmbodiment 
 * @author hvanwelbergen
 */
public class IpaacaSkeletonEmbodiment implements SkeletonEmbodiment
{
    private VJoint animationVJoint;
    private final String id;
    private final IpaacaEmbodiment ipaaca;
    
    public IpaacaSkeletonEmbodiment(String id, IpaacaEmbodiment ipaaca)
    {
        this.id = id;
        this.ipaaca = ipaaca;
    }
    
    public void initialize()
    {
        ipaaca.waitForAvailableJoints();
        animationVJoint = ipaaca.getRootJoint();
        
        //apply renaming
        
        //construct hanim version
        VJointUtils.setHAnimPose(animationVJoint);
        
        //construct hanim-null rotation version   
    }
    
    @Override
    public void copy()
    {
        animationVJoint.calculateMatrices();        
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public VJoint getAnimationVJoint()
    {
        return animationVJoint;
    }

}
