package asap.murml;

import hmi.xml.XMLScanException;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

/**
 * Parser for the MURML parallel element
 * @author hvanwelbergen
 * 
 */
public class Parallel extends MURMLElement implements MovementConstraint
{
    @Getter
    private List<Dynamic> dynamics = new ArrayList<>();

    @Getter
    private List<Sequence> sequences = new ArrayList<>();

    @Getter
    private List<Static> statics = new ArrayList<>();
    
    @Getter
    private List<Symmetrical> symmetricals = new ArrayList<>();

    public void add(Dynamic d)
    {
        dynamics.add(d);
    }
    
    public void add(Static s)
    {
        statics.add(s);
    }
    
    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        while (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            if (tag.equals(Dynamic.xmlTag()))
            {
                Dynamic dynamic = new Dynamic();
                dynamic.readXML(tokenizer);
                dynamics.add(dynamic);
            }
            else if (tag.equals(Static.xmlTag()))
            {
                Static s = new Static();
                s.readXML(tokenizer);
                statics.add(s);
            }
            else if (tag.equals(Sequence.xmlTag()))
            {
                Sequence s = new Sequence();
                s.readXML(tokenizer);
                sequences.add(s);
            }
            else if (tag.equals(Symmetrical.xmlTag()))
            {
                Symmetrical s = new Symmetrical();
                s.readXML(tokenizer);
                symmetricals.add(s);
            }
            else
            {
                throw new XMLScanException("Unknown element " + tag + " in parallel");
            }
        }
    }

    static final String XMLTAG = "parallel";

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
