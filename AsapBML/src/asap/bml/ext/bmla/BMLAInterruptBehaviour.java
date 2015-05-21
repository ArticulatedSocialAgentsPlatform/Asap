/*******************************************************************************
 *******************************************************************************/
package asap.bml.ext.bmla;

import hmi.xml.XMLFormatting;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import saiba.bml.core.Behaviour;
import saiba.bml.parser.SyncPoint;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * BMLA Interrupt behavior. Specifies the interruption of a target BML block
 * @author welberge
 */
public class BMLAInterruptBehaviour extends Behaviour
{
    private String target = "";
    private Set<String>include = new HashSet<String>();
    private Set<String>exclude = new HashSet<String>();
    
    private static final List<String> DEFAULT_SYNCS = ImmutableList.of("start","end");
    
    public static List<String> getDefaultSyncPoints()
    {
        return DEFAULT_SYNCS;
    }
    
    @Override
    public String getNamespace()
    {
        return BMLAInfo.BMLA_NAMESPACE;
    }

    public String getTarget()
    {
        return target;
    }
    
    @Override
    public void addDefaultSyncPoints()
    {
        for(String s:getDefaultSyncPoints())
        {
            addSyncPoint(new SyncPoint(bmlId, id, s));
        }        
    }

    public ImmutableSet<String> getInclude()
    {
        return ImmutableSet.copyOf(include);
    }
    
    public ImmutableSet<String> getExclude()
    {
        return ImmutableSet.copyOf(exclude);
    }
    
    public BMLAInterruptBehaviour(String bmlId,XMLTokenizer tokenizer) throws IOException
    {
        super(bmlId);
        readXML(tokenizer);
    }

    @Override
    public StringBuilder appendAttributeString(StringBuilder buf, XMLFormatting fmt)
    {
        appendAttribute(buf, "target", target);
        String includeString = Joiner.on(",").join(include);
        String excludeString = Joiner.on(",").join(exclude);
        
        if(!includeString.equals(""))
        {
            appendAttribute(buf, "include", includeString);
        }
        if(!excludeString.equals(""))
        {
            appendAttribute(buf, "exclude", excludeString);
        }
        return super.appendAttributeString(buf,fmt);
    }

    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        target = getRequiredAttribute("target", attrMap, tokenizer);
        String includes = getOptionalAttribute("include",attrMap,"");
        if(includes.length()>0)include.addAll(Sets.newHashSet(includes.split(",")));
        String excludes = getOptionalAttribute("exclude",attrMap,"");
        if(excludes.length()>0)exclude.addAll(Sets.newHashSet(excludes.split(",")));
        super.decodeAttributes(attrMap, tokenizer);
    }

    /*
     * The XML Stag for XML encoding
     */
    private static final String XMLTAG = "interrupt";

    /**
     * The XML Stag for XML encoding -- use this static method when you want to see if a given
     * String equals the xml tag for this class
     */
    public static String xmlTag()
    {
        return XMLTAG;
    }

    /**
     * The XML Stag for XML encoding -- use this method to find out the run-time xml tag of an
     * object
     */
    @Override
    public String getXMLTag()
    {
        return XMLTAG;
    }

    @Override
    public float getFloatParameterValue(String name)
    {
        return super.getFloatParameterValue(name);
    }

    @Override
    public String getStringParameterValue(String name)
    {
        if (name.equals("target")) return target;
        return super.getStringParameterValue(name);
    }

    @Override
    public boolean specifiesParameter(String name)
    {
        if (name.equals("target")) return true;
        return super.specifiesParameter(name);
    }
}
