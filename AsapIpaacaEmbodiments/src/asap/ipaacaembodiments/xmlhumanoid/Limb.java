package asap.ipaacaembodiments.xmlhumanoid;

import java.util.HashMap;

import lombok.Getter;

import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

/**
 * Limb element
 * @author hvanwelbergen
 *
 */
public class Limb extends XMLStructureAdapter
{
    @Getter
    private String name;
    @Getter
    private String type;
    @Getter
    private String startJoint;
    @Getter
    private String endJoint;
    @Getter
    private String prefix;
    @Getter
    private boolean isStartJoint;
    
    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        name = getRequiredAttribute("name", attrMap, tokenizer);
        type = getRequiredAttribute("type", attrMap, tokenizer);
        startJoint = getRequiredAttribute("start_joint", attrMap, tokenizer);
        endJoint = getRequiredAttribute("end_joint", attrMap, tokenizer);
        prefix = getRequiredAttribute("prefix", attrMap, tokenizer);
        isStartJoint = getRequiredIntAttribute("is_start_limb", attrMap, tokenizer)!=0;
    }
    
    public static final String XMLTAG = "Limb";
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
