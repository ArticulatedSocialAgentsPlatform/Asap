package hmi.bml.ext.bmlt;

import saiba.bml.core.Behaviour;
import saiba.bml.parser.SyncPoint;
import hmi.xml.XMLFormatting;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * BMLT behavior that provides changes in parameters of ongoing other behaviors. 
 * @author welberge
 */
public class BMLTParameterValueChangeBehaviour extends Behaviour
{
    private static final String BMLTNAMESPACE = "http://hmi.ewi.utwente.nl/bmlt";

    @Override
    public String getNamespace()
    {
        return BMLTNAMESPACE;
    }
    
    private static final List<String> DEFAULT_SYNCS = ImmutableList.of("start","end");    
    public static List<String> getDefaultSyncPoints()
    {
        return DEFAULT_SYNCS;
    }

    public String target;
    public String paramId;
    private BMLTTrajectory trajectory = new BMLTTrajectory();
    
    @Override
    public float getFloatParameterValue(String name)
    {
        return trajectory.getFloatParameterValue(name);
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
        return trajectory.getStringParameterValue(name);
    }

    @Override
    public boolean specifiesParameter(String name)
    {
        if (name.equals("target")) return true;
        else if (name.equals("paramId")) return true;
        return trajectory.specifiesParameter(name);        
    }
    
    @Override
    public boolean satisfiesConstraint(String n, String value)
    {
        if (n.equals("target") && value.equals(target)) return true;
        else if (n.equals("paramId") && value.equals(paramId)) return true;
        return trajectory.satisfiesConstraint(n,value); 
    }

    public BMLTParameterValueChangeBehaviour(String bmlId,XMLTokenizer tokenizer) throws IOException
    {
        super(bmlId);
        readXML(tokenizer);
    }

    public BMLTParameterValueChangeBehaviour(String bmlId)
    {
        super(bmlId);
    }
    
    @Override
    public boolean hasContent()
    {
        return true;
    }
    
    @Override
    public StringBuilder appendAttributeString(StringBuilder buf)
    {
        appendAttribute(buf, "target", target);
        appendAttribute(buf, "paramId", paramId);
        return super.appendAttributeString(buf);
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
            if (tag.equals(BMLTTrajectory.xmlTag()))
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
