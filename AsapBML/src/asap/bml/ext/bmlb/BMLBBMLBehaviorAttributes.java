package asap.bml.ext.bmlb;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import saiba.bml.core.BMLBehaviorAttributeExtension;
import saiba.bml.core.BMLBlockComposition;
import saiba.bml.core.BehaviourBlock;
import saiba.bml.core.CoreComposition;
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
    private Set<String> prependBeforeList = new HashSet<String>();
    private Set<String> chunkBeforeList = new HashSet<String>();

    /**
     * Gets an unmodifiable view of the chunkAfterList, that is the list of bml blocks after which this
     * block is to be chunked
     */
    public Set<String> getChunkAfterList()
    {
        return Collections.unmodifiableSet(chunkAfterList);
    }

    /**
     * Gets an unmodifiable view of the prependBeforeList, that is the list of bml blocks before which this
     * block is to be prepended
     */
    public Set<String> getPrependBeforeList()
    {
        return Collections.unmodifiableSet(prependBeforeList);
    }
    
    
    /**
     * Gets an unmodifiable view of the chunkBeforeList, that is the list of bml blocks before which this
     * block is to be chunked
     */
    public Set<String> getChunkBeforeList()
    {
        return Collections.unmodifiableSet(chunkBeforeList);
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
    public BMLBlockComposition handleComposition(String sm)
    {
        if (sm.startsWith("CHUNK-AFTER"))
        {
            getParameterList(sm, chunkAfterList);
            return BMLBComposition.CHUNK_AFTER;
        }
        else if (sm.equals("PREPEND"))
        {
            return BMLBComposition.PREPEND;
        }
        else if (sm.startsWith("PREPEND-BEFORE"))
        {
            getParameterList(sm, prependBeforeList);
            return BMLBComposition.PREPEND_BEFORE;
        }
        else if (sm.startsWith("CHUNK-BEFORE"))
        {
            getParameterList(sm, chunkBeforeList);
            return BMLBComposition.CHUNK_BEFORE;
        }
        return CoreComposition.UNKNOWN;
    }
}
