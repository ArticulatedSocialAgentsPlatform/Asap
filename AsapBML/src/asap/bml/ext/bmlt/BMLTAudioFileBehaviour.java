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
 * This class represents audio behaviour. This is represented in BML by the
 * <code>&lt;audiofile&gt;</code>-tag, in the http://hmi.ewi.utwente.nl/bmlt namespace.
 * 
 * @author dennisr
 */
public class BMLTAudioFileBehaviour extends BMLTBehaviour
{
    protected String fileName;

    private static final List<String> DEFAULT_SYNCS = ImmutableList.of("start","end");
    public static List<String> getDefaultSyncPoints()
    {
        return DEFAULT_SYNCS;
    }
    
    @Override
    public String getStringParameterValue(String name)
    {
        if (name.equals("fileName")) return fileName;
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
        return super.getFloatParameterValue(name);
    }

    @Override
    public boolean specifiesParameter(String name)
    {
        if (name.equals("fileName")) return true;
        return super.specifiesParameter(name);
    }

    public BMLTAudioFileBehaviour(String bmlId,XMLTokenizer tokenizer) throws IOException
    {
        super(bmlId);
        readXML(tokenizer);
    }

    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        fileName = getRequiredAttribute("fileName", attrMap, tokenizer);
        super.decodeAttributes(attrMap, tokenizer);        
    }

    @Override
    public boolean hasContent()
    {
        return false;
    }

    @Override
    public StringBuilder appendAttributeString(StringBuilder buf, XMLFormatting fmt)
    {
        appendAttribute(buf,"fileName",fileName);
        return super.appendAttributeString(buf, fmt);
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
    private static final String XMLTAG = "audiofile";

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

    /**
     * @return the content
     */
    public String getContent()
    {
        return null;
    }

}
