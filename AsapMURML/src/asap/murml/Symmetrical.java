/*******************************************************************************
 *******************************************************************************/
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
public class Symmetrical extends MURMLElement implements MovementConstraint
{
    @Getter
    private Dominant dominant;

    @Getter
    private Symmetry symmetry;

    @Getter
    private Parallel parallel;

    @Getter
    private Sequence sequence;

    @Getter
    private Dynamic dynamic;

    @Getter
    private Static staticElem;

    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        dominant = Dominant.valueOf(getRequiredAttribute("dominant", attrMap, tokenizer).toUpperCase());
        symmetry = Symmetry.valueOf(getRequiredAttribute("symmetry", attrMap, tokenizer));
    }

    public Parallel normalize()
    {
        if (dynamic != null)
        {
            return Dynamic.constructMirror(dynamic, dominant, symmetry);            
        }
        else if (staticElem != null)
        {
            return Static.constructMirror(staticElem, dominant, symmetry);
        }
        else if (sequence != null)
        {
            Parallel p = new Parallel();
            sequence.makeSymmetric(dominant, symmetry);
            p.add(sequence);
            return p;
        }
        else if (parallel != null)
        {
            parallel.makeSymmetric(dominant, symmetry);
            return parallel;
        }
        return new Parallel();
    }

    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        String tag = tokenizer.getTagName();
        if (tag.equals(Parallel.xmlTag()))
        {
            parallel = new Parallel();
            parallel.readXML(tokenizer);
        }
        else if (tag.equals(Sequence.xmlTag()))
        {
            sequence = new Sequence();
            sequence.readXML(tokenizer);
        }
        else if (tag.equals(Static.xmlTag()))
        {
            staticElem = new Static();
            staticElem.readXML(tokenizer);
        }
        else if (tag.equals(Dynamic.xmlTag()))
        {
            dynamic = new Dynamic();
            dynamic.readXML(tokenizer);
        }
        else
        {
            throw new XMLScanException("Unkown tag " + tag + " in <symmetrical>");
        }
    }

    static final String XMLTAG = "symmetrical";

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
