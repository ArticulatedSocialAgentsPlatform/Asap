/*******************************************************************************
 *******************************************************************************/
package asap.murml;

import hmi.xml.XMLFormatting;
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

    @Override
    public StringBuilder appendContent(StringBuilder buf, XMLFormatting fmt)
    {
        appendXMLStructureList(buf, fmt, sequence);
        return buf;
    }
    
    static final String XMLTAG = "sequence";

    public static String xmlTag()
    {
        return XMLTAG;
    }

    public void normalizeInnerSymmetricals()
    {
        List<MovementConstraint> newSequence = new ArrayList<MovementConstraint>();
        for(MovementConstraint mc:sequence)
        {
            if (mc instanceof Symmetrical)
            {
                newSequence.add(((Symmetrical)mc).normalize());
            }
            else
            {
                newSequence.add(mc);
                if(mc instanceof Parallel)
                {
                    ((Parallel)mc).normalizeInnerSymmetricals();
                }
            }
        }
        sequence = newSequence;
    }
    
    public void makeSymmetric(Dominant dominantHand, Symmetry sym)
    {
        List<MovementConstraint> newSequence = new ArrayList<MovementConstraint>();

        for (MovementConstraint mc : sequence)
        {
            if (mc instanceof Dynamic)
            {
                newSequence.add(Dynamic.constructMirror((Dynamic) mc, dominantHand, sym));
            }
            else if (mc instanceof Static)
            {
                newSequence.add(Static.constructMirror((Static) mc, dominantHand, sym));
            }
            else if (mc instanceof Parallel)
            {
                ((Parallel)mc).makeSymmetric(dominantHand, sym);
                newSequence.add(mc);
            }
            else if (mc instanceof Symmetrical)
            {
                throw new XMLScanException("Cannot have inner <symmetric> inside another symmetric block.");
            }
        }
        sequence = newSequence;
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
            case Parallel.XMLTAG:
                Parallel par = new Parallel();
                par.readXML(tokenizer);
                sequence.add(par);
                break;
            case Symmetrical.XMLTAG:
                Symmetrical sym = new Symmetrical();
                sym.readXML(tokenizer);
                sequence.add(sym);
                break;
            default:
                throw new XMLScanException("Unknown element " + tag + " in sequence");
            }
        }
    }

    @Override
    public String getXMLTag()
    {
        return XMLTAG;
    }
}
