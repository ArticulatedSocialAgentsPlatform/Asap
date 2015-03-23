/*******************************************************************************
 *******************************************************************************/
package asap.murml;

import hmi.xml.XMLFormatting;
import hmi.xml.XMLScanException;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Parses the MURML dynamic element
 * @author hvanwelbergen
 */
public class Dynamic extends MURMLElement implements MovementConstraint
{
    @Getter
    private Keyframing keyframing;

    @Getter
    private List<DynamicElement> dynamicElements = new ArrayList<>();

    @Getter
    private Slot slot;

    @Getter
    @Setter
    private String scope;

    @Getter
    @Setter
    private Symmetry symmetryTransform = Symmetry.Sym;

    public static Parallel constructMirror(Dynamic d, Dominant dominantHand, Symmetry sym)
    {
        Parallel p = new Parallel();
        d.setScope(dominantHand.toString().toLowerCase());
        p.add(d);
        p.add(Dynamic.mirror(d, sym));
        return p;
    }

    public static Dynamic mirror(Dynamic d, Symmetry s)
    {
        Dynamic dMirror = new Dynamic();
        dMirror.slot = d.slot;
        dMirror.setSymmetryTransform(s);
        if (d.scope.equals("left_arm"))
        {
            dMirror.scope = "right_arm";
        }
        else
        {
            dMirror.scope = "left_arm";
        }
        for (DynamicElement de : d.dynamicElements)
        {
            dMirror.dynamicElements.add(de.copy());
        }

        return dMirror;
    }

    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        while (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            switch (tag)
            {
            case Keyframing.XMLTAG:
                keyframing = new Keyframing();
                keyframing.readXML(tokenizer);
                break;
            case DynamicElement.XMLTAG:
                DynamicElement dynamicElement = new DynamicElement();
                dynamicElement.readXML(tokenizer);
                dynamicElements.add(dynamicElement);
                break;
            default:
                throw new XMLScanException("Unkown tag " + tag + " in <dynamic>");
            }
        }
    }

    @Override
    public StringBuilder appendAttributes(StringBuilder buf)
    {
        if (scope != null)
        {
            appendAttribute(buf, "scope", scope);
        }
        if (slot != null)
        {
            appendAttribute(buf, "slot", slot.toString());
        }
        return buf;
    }

    @Override
    public StringBuilder appendContent(StringBuilder buf, XMLFormatting fmt)
    {
        if (keyframing != null)
        {
            keyframing.appendXML(buf, fmt);
        }
        for (DynamicElement dynamicElement : dynamicElements)
        {
            dynamicElement.appendXML(buf, fmt);
        }
        return buf;
    }

    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        scope = getOptionalAttribute("scope", attrMap);
        String sl = getOptionalAttribute("slot", attrMap);
        if (sl != null)
        {
            slot = Slot.valueOf(sl);
        }
    }

    static final String XMLTAG = "dynamic";

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
