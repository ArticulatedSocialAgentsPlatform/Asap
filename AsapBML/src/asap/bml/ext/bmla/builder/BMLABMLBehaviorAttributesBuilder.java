/*******************************************************************************
 *******************************************************************************/
package asap.bml.ext.bmla.builder;

import asap.bml.ext.bmla.BMLABMLBehaviorAttributes;

/**
 * Builder for BMLABMLBehaviorAttributes
 * @author Herwin
 *
 */
public class BMLABMLBehaviorAttributesBuilder
{
    BMLABMLBehaviorAttributes behAttr = new BMLABMLBehaviorAttributes();
    
    public BMLABMLBehaviorAttributesBuilder setPreplanned(boolean preplanned)
    {
        behAttr.setPrePlan(preplanned);
        return this;
    }
    
    public BMLABMLBehaviorAttributesBuilder addToChunkAfter(String ...bmlIds)
    {
        behAttr.addToChunkAfter(bmlIds);
        return this;
    }
    
    public BMLABMLBehaviorAttributesBuilder addToAppendAfter(String ...bmlIds)
    {
        behAttr.addToAppendAfter(bmlIds);
        return this;
    }
    
    public BMLABMLBehaviorAttributesBuilder addToChunkBefore(String ...bmlIds)
    {
        behAttr.addToChunkBefore(bmlIds);
        return this;
    }
    
    public BMLABMLBehaviorAttributesBuilder addToPrependBefore(String ...bmlIds)
    {
        behAttr.addToPrependBefore(bmlIds);
        return this;
    }
    
    public BMLABMLBehaviorAttributesBuilder addToInterrupt(String ...bmlIds)
    {
        behAttr.addToInterrupt(bmlIds);
        return this;
    }
    
    public BMLABMLBehaviorAttributesBuilder addToOnStart(String ...bmlIds)
    {
        behAttr.addToOnStart(bmlIds);
        return this;
    }
    
    public BMLABMLBehaviorAttributes build()
    {
        return behAttr;
    }
}
