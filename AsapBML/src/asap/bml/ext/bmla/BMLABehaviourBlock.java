/*******************************************************************************
 *******************************************************************************/
package asap.bml.ext.bmla;

import hmi.xml.XMLNameSpace;

import java.util.List;
import java.util.Set;

import saiba.bml.core.BMLBehaviorAttributeExtension;
import saiba.bml.core.BehaviourBlock;

/**
 * Enhances BehaviourBlock with some boilerplate code to parse/create BML blocks that contain BMLA elements.
 * @author hvanwelbergen
 *
 */
public class BMLABehaviourBlock extends BehaviourBlock
{
    final BMLABMLBehaviorAttributes bbmlbExt; 
    
    public boolean isPrePlanned()
    {
        return bbmlbExt.isPrePlanned();
    }

    public List<String> getOnStartList()
    {
        return bbmlbExt.getOnStartList();
    }

    public Set<String> getAppendAfterList()
    {
        return bbmlbExt.getAppendAfterList();
    }

    public Set<String> getChunkAfterList()
    {
        return bbmlbExt.getChunkAfterList();
    }

    public Set<String> getPrependBeforeList()
    {
        return bbmlbExt.getPrependBeforeList();
    }

    public Set<String> getChunkBeforeList()
    {
        return bbmlbExt.getChunkBeforeList();
    }

    public List<String> getInterruptList()
    {
        return bbmlbExt.getInterruptList();
    }

    public Set<String> getOtherBlockDependencies()
    {
        return bbmlbExt.getOtherBlockDependencies();
    }

    public BMLABehaviourBlock(BMLBehaviorAttributeExtension... bmlBehaviorAttributeExtensions)
    {
        super(bmlBehaviorAttributeExtensions);
        BMLABMLBehaviorAttributes ext = getBMLAAttribute(bmlBehaviorAttributeExtensions);
        if (ext == null)
        {
            bbmlbExt = new BMLABMLBehaviorAttributes();
            addBMLBehaviorAttributeExtension(bbmlbExt);
        }
        else
        {
            bbmlbExt = ext;
        }        
    }
    
    private BMLABMLBehaviorAttributes getBMLAAttribute(BMLBehaviorAttributeExtension... bmlBehaviorAttributeExtensions)
    {
        for (BMLBehaviorAttributeExtension ext:bmlBehaviorAttributeExtensions)
        {
            if(ext instanceof BMLABMLBehaviorAttributes)
            {
                return (BMLABMLBehaviorAttributes)ext;
            }
        }
        return null;
    }
    
    
    
    @Override
    public String toBMLString(List<XMLNameSpace> xmlNamespaceList)
    {
        return super.toBMLString(BMLAPrefix.insertBMLANamespacePrefix(xmlNamespaceList));
    }    
}
