/*******************************************************************************
 *******************************************************************************/
package asap.murml;

import hmi.xml.XMLFormatting;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

/**
 * Used to group a bunch of frames into ONE continuous motion. Note: Each phase is timed locally, ie. its timing starts over from 0
 * @author hvanwelbergen
 */
public class Phase extends MURMLElement
{
    @Getter
    private List<Frame> frames = new ArrayList<Frame>();
    
    @Override
    public boolean hasContent()
    {
        return true;
    }
    
    @Override
    public StringBuilder appendContent(StringBuilder buf, XMLFormatting fmt)
    {
        appendXMLStructureList(buf, fmt, frames);
        return buf;
    }
    
    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        while (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            if (tag.equals(Frame.xmlTag()))
            {
                Frame frame = new Frame();
                frame.readXML(tokenizer);
                frames.add(frame);
            }
        }
    }
    /*
     * The XML Stag for XML encoding
     */
    private static final String XMLTAG = "phase";

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
