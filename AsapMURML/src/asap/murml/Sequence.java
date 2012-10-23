package asap.murml;

import hmi.xml.XMLScanException;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

/**
 * Parser for the MURML sequence element
 * @author hvanwelbergen
 * 
 */
public class Sequence extends MURMLElement implements MovementConstraint
{
    @Getter
    private List<MovementConstraint> sequence = new ArrayList<>();
    
    static final String XMLTAG = "sequence";

    public static String xmlTag()
    {
        return XMLTAG;
    }

    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        while (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            switch (tag)
            {
            case Dynamic.XMLTAG:
                Dynamic dynamic = new Dynamic();
                dynamic.readXML(tokenizer);
                sequence.add(dynamic);
                break;
            case Static.XMLTAG:
                Static s = new Static();
                s.readXML(tokenizer);
                sequence.add(s);
                break;
            default:
                throw new XMLScanException("Unknown element " + tag + " in parallel");
            }
        }
    }

    @Override
    public String getXMLTag()
    {
        return XMLTAG;
    }
}
