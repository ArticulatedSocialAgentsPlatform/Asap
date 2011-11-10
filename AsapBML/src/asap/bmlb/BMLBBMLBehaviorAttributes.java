package asap.bmlb;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import hmi.bml.core.BMLBehaviorAttributeExtension;
import hmi.bml.core.BehaviourBlock;
import hmi.bml.core.CoreSchedulingMechanism;
import hmi.bml.core.SchedulingMechanism;
import hmi.util.StringUtil;
import hmi.xml.XMLScanException;
import hmi.xml.XMLTokenizer;

/**
 * Attributes added to the &ltbml&gt tag by bmlb
 * @author hvanwelbergen
 */
public class BMLBBMLBehaviorAttributes implements BMLBehaviorAttributeExtension
{
    private Set<String> chunkAfterList = new HashSet<String>();
    
    /**
     * Gets an unmodifiable view of the chunkAfterList, that is the list of bml blocks after which this
     * block is to be chunked
     */          
    public Set<String> getChunkAfterList()
    {
        return Collections.unmodifiableSet(chunkAfterList);
    }

    private void getParameterList(String str, Set<String> parameterList)
    {
        String params[] = str.split("\\(");
        if (params.length != 2 || !params[1].trim().endsWith(")")) throw new XMLScanException("Error scanning scheduling attribute " + str);
        String parameterStr = params[1].trim();
        parameterStr = parameterStr.substring(0, parameterStr.length() - 1);
        StringUtil.splitToCollection(parameterStr, ",", parameterList);
    }
    
    @Override
    public void decodeAttributes(BehaviourBlock behavior, HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        
    }

    @Override
    public SchedulingMechanism handleSchedulingMechanism(String sm)
    {
        if (sm.startsWith("chunk-after"))
        {
            getParameterList(sm, chunkAfterList);
            return BMLBSchedulingMechanism.CHUNK_AFTER;
        }        
        return CoreSchedulingMechanism.UNKNOWN;
    }   
}
