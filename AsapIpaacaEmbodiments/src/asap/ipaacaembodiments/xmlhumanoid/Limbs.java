package asap.ipaacaembodiments.xmlhumanoid;

import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * Limbs element
 * @author hvanwelbergen
 *
 */
class Limbs extends XMLStructureAdapter
{
    public static final String XMLTAG = "Limbs";
    private List<Limb> limbs = new ArrayList<>();
    
    
    
    public ImmutableList<Limb>getLimbs()
    {
        return ImmutableList.copyOf(limbs);
    }
    
    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        while (tokenizer.atSTag())
        {
            switch (tokenizer.getTagName())
            {
            case Limb.XMLTAG:Limb lb = new Limb(); lb.readXML(tokenizer);limbs.add(lb);break;
            default:
                throw new XMLScanException("Invalid tag " + tokenizer.getTagName() + " in <Humanoid>");
            }
        }
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
