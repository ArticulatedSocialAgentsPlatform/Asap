package asap.hns;

import java.util.HashMap;

import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;
import lombok.Getter;

/**
 * Autoswivel parameters
 * @author hvanwelbergen
 */
public class AutoSwivel extends XMLStructureAdapter
{
    @Getter
    private double minSwivel;
    @Getter
    private double maxSwivel;
    @Getter
    private double freedomOfTheGaussianMean;
    
    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        minSwivel = getRequiredFloatAttribute("minSwivel", attrMap, tokenizer);
        maxSwivel = getRequiredFloatAttribute("maxSwivel", attrMap, tokenizer);
        freedomOfTheGaussianMean = getRequiredFloatAttribute("freedomOfTheGaussianMean", attrMap, tokenizer);
    }
    
    public static final String XMLTAG = "autoswivel";

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
