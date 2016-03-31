/*******************************************************************************
 *******************************************************************************/
package asap.bml.ext.bmlt;

import hmi.xml.XMLFormatting;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import saiba.bml.parser.SyncPoint;

import com.google.common.collect.ImmutableList;

/**
 * This class represents face morph target behaviour. This is represented in BML by
 * the <code>&lt;facemorph&gt;</code>-tag in the http://hmi.ewi.utwente.nl/bmlt namespace.
 * 
 * @author dennisr
 */
public class BMLTFaceMorphBehaviour extends BMLTBehaviour
{
    protected String targetName = "";
    protected float intensity = 0;

    public BMLTFaceMorphBehaviour(String bmlId)
    {
        super(bmlId);
    }
    
    private static final List<String> DEFAULT_SYNCS = ImmutableList.of("start","attackPeak","relax","end");
    public static List<String> getDefaultSyncPoints()
    {
        return DEFAULT_SYNCS;
    }

    @Override
    public String getStringParameterValue(String name)
    {
        if (name.equals("targetname")) return targetName;
        if (name.equals("intensity")) return "" + intensity;
        return super.getStringParameterValue(name);
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
    public float getFloatParameterValue(String name)
    {
        if (name.equals("intensity")) return intensity;
        return super.getFloatParameterValue(name);
    }

    @Override
    public boolean specifiesParameter(String name)
    {
        if (name.equals("targetname") || name.equals("intensity")) return true;
        return super.specifiesParameter(name);
    }

    public BMLTFaceMorphBehaviour(String id, XMLTokenizer tokenizer) throws IOException
    {
        super(id);
        readXML(tokenizer);
    }

    @Override
    public StringBuilder appendAttributeString(StringBuilder buf, XMLFormatting fmt)
    {
        appendAttribute(buf, "intensity", intensity);
        appendAttribute(buf, "targetname", targetName);
        return super.appendAttributeString(buf, fmt);
    }

    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        targetName = getRequiredAttribute("targetname", attrMap, tokenizer);
        intensity = getOptionalFloatAttribute("intensity", attrMap, 1f);
        super.decodeAttributes(attrMap, tokenizer);        
    }

    @Override
    public boolean hasContent()
    {
        return false;
    }

    @Override
    public StringBuilder appendContent(StringBuilder buf, XMLFormatting fmt)
    {
        return super.appendContent(buf, fmt); // Description is registered at Behavior.
    }

    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        super.decodeContent(tokenizer);
    }

    /*
     * The XML Stag for XML encoding
     */
    private static final String XMLTAG = "facemorph";

    /**
     * The XML Stag for XML encoding -- use this static method when you want to see if a given String equals
     * the xml tag for this class
     */
    public static String xmlTag()
    {
        return XMLTAG;
    }

    /**
     * The XML Stag for XML encoding -- use this method to find out the run-time xml tag of an object
     */
    @Override
    public String getXMLTag()
    {
        return XMLTAG;
    }

    /**
     * @return the content
     */
    public String getContent()
    {
        return null;
    }

}
