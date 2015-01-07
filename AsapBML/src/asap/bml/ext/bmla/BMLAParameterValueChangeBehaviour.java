/*******************************************************************************
 *******************************************************************************/
package asap.bml.ext.bmla;

import hmi.xml.XMLFormatting;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import saiba.bml.core.Behaviour;
import saiba.bml.parser.SyncPoint;

import com.google.common.collect.ImmutableList;

/**
 * BMLT behavior that provides changes in parameters of ongoing other behaviors. 
 * @author welberge
 */
public class BMLAParameterValueChangeBehaviour extends Behaviour
{
    @Override
    public String getNamespace()
    {
        return BMLAInfo.BMLA_NAMESPACE;
    }
    
    private static final List<String> DEFAULT_SYNCS = ImmutableList.of("start","end");    
    public static List<String> getDefaultSyncPoints()
    {
        return DEFAULT_SYNCS;
    }

    public String target;
    public String paramId;
    private BMLATrajectory trajectory = new BMLATrajectory();
    
    @Override
    public float getFloatParameterValue(String name)
    {
        if(trajectory.specifiesParameter(name))
        {
            return trajectory.getFloatParameterValue(name);
        }
        return super.getFloatParameterValue(name);
    }
    
    @Override
    public void addDefaultSyncPoints()
    {
        for(String s:getDefaultSyncPoints())
        {
            addSyncPoint(new SyncPoint(bmlId, id, s));
        }        
    }

    @Override
    public String getStringParameterValue(String name)
    {
        if (name.equals("target")) return target;
        else if (name.equals("paramId")) return paramId;
        if(trajectory.specifiesParameter(name))
        {
            return trajectory.getStringParameterValue(name);
        }
        return super.getStringParameterValue(name);
    }

    @Override
    public boolean specifiesParameter(String name)
    {
        if (name.equals("target")) return true;
        if (name.equals("paramId")) return true;
        if (trajectory.specifiesParameter(name)) return true;
        return super.specifiesParameter(name);
    }
    
    @Override
    public boolean satisfiesConstraint(String n, String value)
    {
        if (n.equals("target") && value.equals(target)) return true;
        if (n.equals("paramId") && value.equals(paramId)) return true;
        if (trajectory.satisfiesConstraint(n,value)) return true;
        return super.satisfiesConstraint(n, value);
    }

    public BMLAParameterValueChangeBehaviour(String bmlId,XMLTokenizer tokenizer) throws IOException
    {
        super(bmlId);
        readXML(tokenizer);
    }

    public BMLAParameterValueChangeBehaviour(String bmlId)
    {
        super(bmlId);
    }
    
    @Override
    public boolean hasContent()
    {
        return true;
    }
    
    @Override
    public StringBuilder appendAttributeString(StringBuilder buf, XMLFormatting fmt)
    {
        appendAttribute(buf, "target", target);
        appendAttribute(buf, "paramId", paramId);
        return super.appendAttributeString(buf, fmt);
    }

    @Override
    public StringBuilder appendContent(StringBuilder buf)
    {
        trajectory.appendXML(buf);
        return super.appendContent(buf); // Description is registered at Behavior.
    }
    
    @Override
    public StringBuilder appendContent(StringBuilder buf, XMLFormatting fmt)
    {
        trajectory.appendXML(buf,fmt);
        return super.appendContent(buf, fmt); // Description is registered at Behavior.
    }
    
    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        target = getRequiredAttribute("target", attrMap, tokenizer);
        paramId = getRequiredAttribute("paramId", attrMap, tokenizer);
        super.decodeAttributes(attrMap, tokenizer);
    }

    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        while (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            if (tag.equals(BMLATrajectory.xmlTag()))
            {
                trajectory.readXML(tokenizer);                
            }
            ensureDecodeProgress(tokenizer);
        }
    }
    
    /*
     * The XML Stag for XML encoding
     */
    private static final String XMLTAG = "parametervaluechange";

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
}
