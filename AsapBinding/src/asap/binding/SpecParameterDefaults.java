/*******************************************************************************
 *******************************************************************************/
package asap.binding;

import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Holds default parameter values for a plan unit.
 * @author Herwin van Welbergen
 * 
 */
public class SpecParameterDefaults extends XMLStructureAdapter
{
    private List<SpecParameterDefault> paramdefaults = new ArrayList<>();
    
    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        while (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            if (tag.equals(SpecParameterDefault.xmlTag()))
            {
                SpecParameterDefault mupc = new SpecParameterDefault();
                mupc.readXML(tokenizer);
                paramdefaults.add(mupc);
            }
            else
            {
                throw new XMLScanException("Unknown XML element "+tag+" in parameterdefaults");
            }
        }
    }

    /**
     * Get motion unit parameter for BML parameter src
     */
    public Collection<SpecParameterDefault> getParameterDefaults()
    {
        return paramdefaults;
    }
    
    public static final String XMLTAG = "parameterdefaults";

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
