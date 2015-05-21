/*******************************************************************************
 *******************************************************************************/
package asap.murml;

import hmi.xml.XMLFormatting;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;

import lombok.Getter;

/**
 * Parses a MURML frame
 * @author hvanwelbergen
 */
public class Frame extends MURMLElement
{
    @Getter
    private double ftime;

    @Getter
    private Posture posture;

    public Frame()
    {

    }

    public Frame(double ftime, Posture p)
    {
        this.ftime = ftime;
        this.posture = p;
    }

    @Override
    public boolean hasContent()
    {
        return true;
    }

    @Override
    public StringBuilder appendAttributes(StringBuilder buf)
    {
        appendAttribute(buf, "ftime", ftime);
        return buf;
    }

    @Override
    public StringBuilder appendContent(StringBuilder buf, XMLFormatting fmt)
    {
        return posture.appendXML(buf, fmt);
    }

    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        ftime = getRequiredDoubleAttribute("ftime", attrMap, tokenizer);
    }

    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        String tag = tokenizer.getTagName();
        if (tag.equals(Posture.xmlTag()))
        {
            posture = new Posture();
            posture.readXML(tokenizer);
        }
    }

    /*
     * The XML Stag for XML encoding
     */
    private static final String XMLTAG = "frame";

    /**
     * The XML Stag for XML encoding -- use this static method when you want to
     * see if a given String equals the xml tag for this class
     */
    public static String xmlTag()
    {
        return XMLTAG;
    }

    /**
     * The XML Stag for XML encoding -- use this method to find out the run-time
     * xml tag of an object
     */
    @Override
    public String getXMLTag()
    {
        return XMLTAG;
    }
}
