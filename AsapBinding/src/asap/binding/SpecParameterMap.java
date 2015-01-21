/*******************************************************************************
 *******************************************************************************/
package asap.binding;

import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

/**
 * Maps BML behavior parameters to planunit parameters
 * @author welberge
 *
 */
public class SpecParameterMap extends XMLStructureAdapter
{
    private HashMap<String, String> parametermap = new HashMap<String, String>();

    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        while (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            if (tag.equals(SpecParameter.xmlTag()))
            {
                SpecParameter mup = new SpecParameter();
                mup.readXML(tokenizer);
                parametermap.put(mup.src, mup.dst);
            }
            else
            {
                throw new XMLScanException("Unknown XML element "+tag+" in parametermap");
            }
        }
    }

    public Set<String> getParameters()
    {
        return parametermap.keySet();
    }

    public String getParameter(String src)
    {
        return parametermap.get(src);
    }
    
    public static final String XMLTAG = "parametermap";

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
