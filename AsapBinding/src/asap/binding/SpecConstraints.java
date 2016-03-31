/*******************************************************************************
 *******************************************************************************/
package asap.binding;

import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import saiba.bml.core.Behaviour;

/**
 * An XML element to group SpecConstraint-s
 * @author welberge
 *
 */
public class SpecConstraints extends XMLStructureAdapter
{
    private List<SpecConstraint> constraints = new ArrayList<SpecConstraint>(); 
    
    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        while (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            if (tag.equals(SpecConstraint.xmlTag()))
            {
                SpecConstraint c = new SpecConstraint();
                c.readXML(tokenizer);
                constraints.add(c);
            }
            else
            {
                throw new XMLScanException("Unknown XML element "+tag+" in constraints");
            }
        }
    }

    public boolean satisfiesConstraints(Behaviour b)
    {
        for (SpecConstraint c : constraints)
        {
            if (c.namespace!=null)
            {
                if (!b.satisfiesConstraint(c.namespace+":"+c.name, c.value)) return false;
            }
            else
            {
                if (!b.satisfiesConstraint(c.name, c.value)) return false;
            }
        }
        return true;
    }
    
    public static final String XMLTAG = "constraints";

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
