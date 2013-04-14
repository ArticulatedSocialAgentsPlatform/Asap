package asap.bml.ext.bmla;

import hmi.util.StringUtil;
import hmi.xml.XMLScanException;
import hmi.xml.XMLTokenizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import saiba.bml.core.BMLBehaviorAttributeExtension;
import saiba.bml.core.BMLBlockComposition;
import saiba.bml.core.BehaviourBlock;
import saiba.bml.core.CoreComposition;

/**
 * Attributes added to the &ltbml&gt tag by bmlb
 * @author hvanwelbergen
 */
public class BMLABMLBehaviorAttributes implements BMLBehaviorAttributeExtension
{
    private Set<String> chunkAfterList = new HashSet<String>();
    private Set<String> appendAfterList = new HashSet<String>();
    private Set<String> prependBeforeList = new HashSet<String>();
    private Set<String> chunkBeforeList = new HashSet<String>();
    private List<String> interruptList = new ArrayList<String>();
    private List<String> onStartList = new ArrayList<String>();
    private boolean prePlan;

    /**
     * @return an unmodifiable view of the onStartList, that is the list of bml blocks that this
     *         block should activate
     */
    public List<String> getOnStartList()
    {
        return Collections.unmodifiableList(onStartList);
    }

    public boolean isPrePlanned()
    {
        return prePlan;
    }

    /**
     * Gets an unmodifiable view of the appendAfterList, that is the list of bml blocks after which this
     * block is to be concatenated
     */
    public Set<String> getAppendAfterList()
    {
        return Collections.unmodifiableSet(appendAfterList);
    }

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

    @Override
    public void decodeAttributes(BehaviourBlock bb, HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        StringUtil.splitToCollection(bb.getOptionalAttribute("http://www.asap-project.org/bmla:chunkAfter", attrMap, ""), ",",
                chunkAfterList);
        StringUtil.splitToCollection(bb.getOptionalAttribute("http://www.asap-project.org/bmla:chunkBefore", attrMap, ""), ",",
                chunkBeforeList);
        StringUtil.splitToCollection(bb.getOptionalAttribute("http://www.asap-project.org/bmla:appendAfter", attrMap, ""), ",",
                appendAfterList);
        StringUtil.splitToCollection(bb.getOptionalAttribute("http://www.asap-project.org/bmla:prependBefore", attrMap, ""), ",",
                prependBeforeList);

        String interrupt = bb.getOptionalAttribute("http://www.asap-project.org/bmla:interrupt", attrMap, null);
        if (interrupt != null)
        {
            StringUtil.splitToCollection(interrupt, ",", interruptList);
        }
        prePlan = bb.getOptionalBooleanAttribute("http://www.asap-project.org/bmla:preplan", attrMap, false);
        StringUtil.splitToCollection(bb.getOptionalAttribute("http://www.asap-project.org/bmla:onStart", attrMap, ""), ",", onStartList);
    }

    /**
     * @return the an unmodifiable view of the interruptList
     */
    public List<String> getInterruptList()
    {
        return Collections.unmodifiableList(interruptList);
    }

    private void appendParameterList(String str, Set<String> parameterList)
    {
        String params[] = str.split("\\(");
        if (params.length != 2 || !params[1].trim().endsWith(")")) throw new XMLScanException("Error scanning scheduling attribute " + str);
        String parameterStr = params[1].trim();
        parameterStr = parameterStr.substring(0, parameterStr.length() - 1);
        StringUtil.splitToCollection(parameterStr, ",", parameterList);
    }

    @Override
    public BMLBlockComposition handleComposition(String sm)
    {
        if (sm.startsWith("APPEND-AFTER"))
        {
            appendParameterList(sm, appendAfterList);
            return BMLASchedulingMechanism.APPEND_AFTER;
        }
        return CoreComposition.UNKNOWN;
    }

    @Override
    public Set<String> getOtherBlockDependencies()
    {
        return new ImmutableSet.Builder<String>().addAll(chunkAfterList).addAll(appendAfterList).addAll(prependBeforeList)
                .addAll(chunkBeforeList).addAll(interruptList).addAll(onStartList).build();
    }
}
