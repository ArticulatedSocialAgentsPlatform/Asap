package asap.ipaacaembodiments;

import hmi.animation.VJoint;
import hmi.environmentbase.CopyEmbodiment;

/**
 * Sends joint rotations from its animation joint to a renderer through Ipaaca. 
 * Assumes that the animation joint is not changed during the copy(). That is: assumes that there is only one thread accessing animationJoint.
 * @author hvanwelbergen
 *
 */
public class IpaacaBodyEmbodiment implements CopyEmbodiment
{
    private final String id;
    private final VJoint animationJoint;    
    private IpaacaEmbodiment ipaacaEmbodiment;
    
    public IpaacaBodyEmbodiment(String id, VJoint animationJoint, IpaacaEmbodiment ipaacaEmbodiment)
    {
        this.id = id;
        this.animationJoint = animationJoint;      
        this.ipaacaEmbodiment = ipaacaEmbodiment;
    }
    
    public void init()
    {
        ipaacaEmbodiment.getAvailableJoints();
    }
    
    @Override
    public void copy()
    {
        animationJoint.calculateMatrices();   
        
    }

    @Override
    public String getId()
    {
        return id;
    }    
}
