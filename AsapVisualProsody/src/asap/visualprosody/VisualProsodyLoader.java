package asap.visualprosody;

import hmi.math.Vec3f;
import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;

import lombok.Getter;

public class VisualProsodyLoader extends XMLStructureAdapter
{
    private static final String XMLTAG = "visualprosodyprovider";
    
    @Getter
    private float offset[] = Vec3f.getVec3f(0,0,0);
    
    @Getter
    private GaussianMixtureModel roll;
    @Getter
    private GaussianMixtureModel pitch;
    @Getter
    private GaussianMixtureModel yaw;
    @Getter
    private GaussianMixtureModel v;
    @Getter
    private GaussianMixtureModel a;
    
    private GaussianMixtureModel getInnerMixtureModel(XMLTokenizer tokenizer) throws IOException
    {
        tokenizer.takeSTag();
        GMMParser gpp = new GMMParser();
        gpp.readXML(tokenizer);
        tokenizer.takeETag();
        return gpp.constructMixtureModel();
    }
    
    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        String off = getOptionalAttribute("offset", attrMap, "0 0 0");
        offset = decodeFloatArray(off);
    }
    
    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        while (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            switch (tag)
            {
            case "roll":
                roll = getInnerMixtureModel(tokenizer);
                break;
            case "pitch":
                pitch = getInnerMixtureModel(tokenizer);
                break;
            case "yaw":
                yaw = getInnerMixtureModel(tokenizer);
                break;
            case "v":
                v = getInnerMixtureModel(tokenizer);
                break;
            case "a":
                a = getInnerMixtureModel(tokenizer);
                break;
            default:
                throw new XMLScanException("Unknown tag " + tag + " in <visualprosodyprovider>");
            }
        }
    }
    
    public VisualProsody constructProsodyProvider()
    {
        return new VisualProsody(roll, pitch, yaw, v, a, offset);
    }
    
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
