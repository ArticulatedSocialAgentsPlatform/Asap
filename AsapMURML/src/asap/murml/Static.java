/*******************************************************************************
 *******************************************************************************/
package asap.murml;

import hmi.xml.XMLTokenizer;

import java.util.HashMap;

import lombok.Getter;
import lombok.Setter;

/**
 * Parser for the static murml element
 * @author hvanwelbergen
 *
 */
public class Static extends MURMLElement implements MovementConstraint
{
    @Getter @Setter
    private String scope;
    
    @Getter
    private Slot slot;
    
    @Getter
    private String value;
    
    @Getter
    @Setter
    private Symmetry symmetryTransform = Symmetry.Sym;
    
    public static Parallel constructMirror(Static s, Dominant dominantHand, Symmetry sym)
    {
        Parallel p = new Parallel();
        s.setScope(dominantHand.toString().toLowerCase());
        p.add(s);
        p.add(Static.mirror(s, sym));
        return p;
    }
    
    public static Static mirror(Static s, Symmetry sym)
    {
        Static sMirror = new Static();
        if (s.scope.equals("left_arm"))
        {
            sMirror.scope = "right_arm";
        }
        else
        {
            sMirror.scope = "left_arm";
        }
        sMirror.setSymmetryTransform(sym);
        sMirror.slot = s.slot;
        sMirror.value = s.value;
        return sMirror;
    }
    
    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        scope = getOptionalAttribute("scope", attrMap);
        String sl = getOptionalAttribute("slot", attrMap);
        if(sl!=null)
        {
            slot = Slot.valueOf(sl);
        }
        value = getRequiredAttribute("value", attrMap, tokenizer);        
    }
    
    @Override
    public StringBuilder appendAttributes(StringBuilder buf)
    {
        if(scope!=null)
        {
            appendAttribute(buf, "scope", scope);
        }
        if(slot!=null)
        {
            appendAttribute(buf, "slot", slot.toString());
        }
        appendAttribute(buf, "value", value);
        return buf;
    }
    
    static final String XMLTAG = "static";

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
