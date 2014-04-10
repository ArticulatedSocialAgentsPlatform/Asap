package asap.bml.ext.bmla;

import hmi.xml.XMLNameSpace;

import java.util.List;
import java.util.Set;

import saiba.bml.core.BMLBehaviorAttributeExtension;
import saiba.bml.core.BehaviourBlock;

import com.google.common.collect.ImmutableList;

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
    
    private boolean findPrefix(String prefix, List<XMLNameSpace> xmlNamespaceList)
    {
        for(XMLNameSpace ns:xmlNamespaceList)
        {
            if(ns.getPrefix().equals(prefix))
            {
                return true;
            }
        }
        return false;
    } 
    
    private boolean findNameSpace(String nameSpace, List<XMLNameSpace> xmlNamespaceList)
    {
        for(XMLNameSpace ns:xmlNamespaceList)
        {
            if(ns.getNamespace().equals(nameSpace))
            {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public String toBMLString(List<XMLNameSpace> xmlNamespaceList)
    {
        if(!findPrefix("bmla", xmlNamespaceList) && !findNameSpace(BMLAInfo.BMLA_NAMESPACE, xmlNamespaceList))
        {
            xmlNamespaceList = new ImmutableList.Builder<XMLNameSpace>().addAll(xmlNamespaceList).add(new XMLNameSpace("bmla",BMLAInfo.BMLA_NAMESPACE)).build();            
        }
        return super.toBMLString(xmlNamespaceList);
    }    
}
