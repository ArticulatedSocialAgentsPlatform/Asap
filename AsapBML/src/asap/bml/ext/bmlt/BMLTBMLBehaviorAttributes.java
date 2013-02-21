package asap.bml.ext.bmlt;

import saiba.bml.core.BMLBehaviorAttributeExtension;
import saiba.bml.core.BMLBlockComposition;
import saiba.bml.core.BehaviourBlock;
import saiba.bml.core.CoreComposition;
import hmi.util.StringUtil;
import hmi.xml.XMLScanException;
import hmi.xml.XMLTokenizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Attributes added to the &ltbml&gt tag by bmlt
 * @author hvanwelbergen
 * 
 */
public class BMLTBMLBehaviorAttributes implements BMLBehaviorAttributeExtension
{
    private List<String> interruptList = new ArrayList<String>();

    private List<String> onStartList = new ArrayList<String>();

    private boolean prePlan;

    private boolean allowExternalRefs = false;

    private Set<String> appendList = new HashSet<String>();

    /**
     * @return the an unmodifiable view of the appendList
     */
    public Set<String> getAppendList()
    {
        return Collections.unmodifiableSet(appendList);
    }

    /**
     * @return an unmodifiable view of the onStartList, that is the list of bml blocks that this
     *         block should activate
     */
    public List<String> getOnStartList()
    {
        return Collections.unmodifiableList(onStartList);
    }

    public boolean allowExternalRefs()
    {
        return allowExternalRefs;
    }

    public boolean isPrePlanned()
    {
        return prePlan;
    }

    private void getParameterList(String str, Set<String> parameterList)
    {
        String params[] = str.split("\\(");
        if (params.length != 2 || !params[1].trim().endsWith(")")) throw new XMLScanException("Error scanning scheduling attribute " + str);
        String parameterStr = params[1].trim();
        parameterStr = parameterStr.substring(0, parameterStr.length() - 1);
        StringUtil.splitToCollection(parameterStr, ",", parameterList);
    }

    /**
     * @return the an unmodifiable view of the interruptList
     */
    public List<String> getInterruptList()
    {
        return Collections.unmodifiableList(interruptList);
    }

    public void decodeAttributes(BehaviourBlock bb, HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        String interrupt = bb.getOptionalAttribute("http://hmi.ewi.utwente.nl/bmlt:interrupt", attrMap, null);
        if (interrupt != null)
        {
            StringUtil.splitToCollection(interrupt, ",", interruptList);
        }
        prePlan = bb.getOptionalBooleanAttribute("http://hmi.ewi.utwente.nl/bmlt:preplan", attrMap, false);
        StringUtil.splitToCollection(bb.getOptionalAttribute("http://hmi.ewi.utwente.nl/bmlt:onStart", attrMap, ""), ",", onStartList);
    }

    @Override
    public BMLBlockComposition handleComposition(String sm)
    {
        if (sm.startsWith("APPEND-AFTER"))
        {
            getParameterList(sm, appendList);
            return BMLTSchedulingMechanism.APPEND_AFTER;
        }
        return CoreComposition.UNKNOWN;
    }
}
