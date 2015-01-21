/*******************************************************************************
 *******************************************************************************/
package asap.murml;

import hmi.xml.XMLTokenizer;

import java.io.IOException;

import lombok.Getter;

/**
 * MURML definition parser
 * @author hvanwelbergen
 *
 */
public class Definition extends MURMLElement
{
    @Getter private Posture posture;
    @Getter private Keyframing keyframing; 
    
    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        String tag = tokenizer.getTagName();
        if (tag.equals(Posture.xmlTag()))
        {
            posture = new Posture();
            posture.readXML(tokenizer);
        }
        if (tag.equals(Keyframing.xmlTag()))
        {
            keyframing = new Keyframing();
            keyframing.readXML(tokenizer);
        }        
    }
    
    private static final String XMLTAG = "definition";

    public static String xmlTag()
    {
        return XMLTAG;
    }

    @Override
    public String getXMLTag()
    {
        return XMLTAG;
    }
}
