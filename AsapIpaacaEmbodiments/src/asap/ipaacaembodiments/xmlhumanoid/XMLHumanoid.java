package asap.ipaacaembodiments.xmlhumanoid;

import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

import com.google.common.collect.ImmutableList;

/**
 * Parser for the ACE XML humanoid
 * @author hvanwelbergen
 * 
 */
public class XMLHumanoid extends XMLStructureAdapter
{
    private static final String XMLTAG = "Humanoid";

    @Getter
    private ImmutableList<Limb> limbs = ImmutableList.of();
    
    @Getter
    private Joint rootJoint;

    private Map<String,Joint>jointMap = new HashMap<>();
    
    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        while (tokenizer.atSTag())
        {
            switch (tokenizer.getTagName())
            {
            case Limbs.XMLTAG:
                Limbs lbs = new Limbs();
                lbs.readXML(tokenizer);
                limbs = lbs.getLimbs();
                break;
            case Joint.XMLTAG:
                rootJoint = new Joint();
                rootJoint.readXML(tokenizer);
                break;
            default:
                throw new XMLScanException("Invalid tag " + tokenizer.getTagName() + " in <Humanoid>");
            }
        }
        constructJointMap(rootJoint);
    }

    private void constructJointMap(Joint j)
    {
        jointMap.put(j.getName(),j);
        jointMap.put(j.getAlias(),j);
        for(Segment s:j.getSegments())
        {
            constructJointMap(s.getJoint());
        }
    }
    
    /**
     * Gets joint by name or alias
     */
    public Joint getJoint(String id)
    {
        return jointMap.get(id);
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
