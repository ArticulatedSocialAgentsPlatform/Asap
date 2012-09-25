package asap.ipaacaembodiments.xmlhumanoid;

import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;

import lombok.Getter;

public class Segment extends XMLStructureAdapter
{
    @Getter
    private Joint joint;
    @Getter
    private String name;
    @Getter
    private String type;
    @Getter
    private int index;
    @Getter
    private float[] translation;
    
    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        while (tokenizer.atSTag())
        {
            switch (tokenizer.getTagName())
            {
            case Joint.XMLTAG:joint = new Joint(); joint.readXML(tokenizer);break;
            default:
                throw new XMLScanException("Invalid tag " + tokenizer.getTagName() + " in <Humanoid>");
            }
        }
    }
    
    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        name = getRequiredAttribute("name", attrMap, tokenizer);
        type = getRequiredAttribute("type", attrMap, tokenizer);
        index = getRequiredIntAttribute("index", attrMap, tokenizer);
        translation = decodeFloatArray(getRequiredAttribute("translation", attrMap, tokenizer));
    }
    
    public static final String XMLTAG = "Segment";
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
