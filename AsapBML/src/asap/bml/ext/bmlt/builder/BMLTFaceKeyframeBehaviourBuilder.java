/*******************************************************************************
 *******************************************************************************/
package asap.bml.ext.bmlt.builder;

import saiba.bml.builder.BehaviourBuilder;
import saiba.bml.core.Behaviour;
import asap.bml.ext.bmlt.BMLTBehaviour;
import asap.bml.ext.bmlt.BMLTFaceKeyframeBehaviour;
import asap.bml.ext.bmlt.BMLTFaceKeyframeBehaviour.Type;

/**
 * Builder for the BMLTFaceKeyframeBehaviour
 * @author herwinvw
 *
 */
public class BMLTFaceKeyframeBehaviourBuilder
{
    private final BehaviourBuilder builder;
    
    public BMLTFaceKeyframeBehaviourBuilder(String bmlId, String id)
    {
        builder = new BehaviourBuilder(BMLTFaceKeyframeBehaviour.xmlTag(), bmlId, id);
        builder.namespace(BMLTBehaviour.BMLTNAMESPACE);
    }
    
    public BMLTFaceKeyframeBehaviourBuilder name(String name)
    {
        builder.param("name", name);
        return this;
    }
    
    public BMLTFaceKeyframeBehaviourBuilder type(Type type)
    {
        builder.param("type", type.toString());
        return this;
    }
    
    public BMLTFaceKeyframeBehaviourBuilder content(String content)
    {
        builder.content(content);
        return this;
    }
    
    public Behaviour build()
    {
        return builder.build();
    }
}
