package asap.murml;

import hmi.xml.XMLScanException;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;

import lombok.Getter;

/**
 * Parser for the symmetrical murml element
 * @author hvanwelbergen
 */
public class Symmetrical extends MURMLElement
{
    @Getter
    private Dominant dominant;
    
    @Getter
    private Symmetry symmetry;
    
    @Getter
    private Parallel parallel;
    
    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        dominant = Dominant.valueOf(getRequiredAttribute("dominant", attrMap,tokenizer).toUpperCase());
        symmetry = Symmetry.valueOf(getRequiredAttribute("symmetry", attrMap,tokenizer));        
    }
    
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        String tag = tokenizer.getTagName();
        if (tag.equals(Parallel.xmlTag()))
        {
            parallel = new Parallel();
            parallel.readXML(tokenizer);
        }
        else
        {
            throw new XMLScanException("Unkown tag "+tag+" in <symmetrical>");
        }
    }
    
    private static final String XMLTAG = "symmetrical";

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
