/*******************************************************************************
 *******************************************************************************/
package asap.bml.ext.bmla.builder;

import saiba.bml.builder.BehaviourBuilder;
import saiba.bml.core.Behaviour;
import asap.bml.ext.bmla.BMLAInfo;
import asap.bml.ext.bmla.BMLAParameterValueChangeBehaviour;

/**
 * Builds a BMLAParameterValueChangeBehaviour
 * @author Herwin
 *
 */
public class BMLAParameterValueChangeBehaviourBuilder
{
    private final BehaviourBuilder builder;
    public BMLAParameterValueChangeBehaviourBuilder(String bmlId, String id, String target, String paramId)
    {
        builder = new BehaviourBuilder(BMLAParameterValueChangeBehaviour.xmlTag(), bmlId, id);
        builder.namespace(BMLAInfo.BMLA_NAMESPACE);
        builder.param("target", target);
        builder.param("paramId", paramId);        
    }
    
    public BMLAParameterValueChangeBehaviourBuilder trajectory(String type, String targetValue)
    {
        builder.content(new BMLATrajectoryBuilder(type, targetValue).build().toXMLString());
        return this;
    }
    
    public BMLAParameterValueChangeBehaviourBuilder trajectory(String type, String initialValue, String targetValue)
    {
        builder.content(new BMLATrajectoryBuilder(type, targetValue).initialValue(initialValue).build().toXMLString());
        return this;
    }
    
    public Behaviour build()
    {
        return builder.build();
    }
}
