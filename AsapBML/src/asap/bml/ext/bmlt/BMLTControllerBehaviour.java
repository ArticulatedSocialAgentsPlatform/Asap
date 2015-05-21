/*******************************************************************************
 *******************************************************************************/
package asap.bml.ext.bmlt;

import hmi.xml.XMLFormatting;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import saiba.bml.parser.SyncPoint;

import com.google.common.collect.ImmutableList;

/**
 * BML spec for physical controllers 
 * @author welberge
 */
public class BMLTControllerBehaviour extends BMLTBehaviour
{
    public String className;
    public String name;

    private static final List<String> DEFAULT_SYNCS = ImmutableList.of("start","end");
    public static List<String> getDefaultSyncPoints()
    {
        return DEFAULT_SYNCS;
    }
    
    @Override
    public boolean satisfiesConstraint(String n, String value)
    {
        if (n.equals("class") && value.equals(className)) return true;
        if (n.equals("name") && value.equals(name)) return true;
        return super.satisfiesConstraint(n, value);
    }

    public BMLTControllerBehaviour(String bmlId,XMLTokenizer tokenizer) throws IOException
    {
        super(bmlId);
        readXML(tokenizer);
    }
    
    @Override
    public void addDefaultSyncPoints()
    {
        for(String s:getDefaultSyncPoints())
        {
            addSyncPoint(new SyncPoint(bmlId, id, s));
        }        
    }

    @Override
    public StringBuilder appendAttributeString(StringBuilder buf, XMLFormatting fmt)
    {
        appendAttribute(buf, "class", className);
        appendAttribute(buf, "name", name);
        return super.appendAttributeString(buf, fmt);
    }

    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        className = getRequiredAttribute("class", attrMap, tokenizer);
        name = getOptionalAttribute("name", attrMap);
        super.decodeAttributes(attrMap, tokenizer);
    }

    /*
     * The XML Stag for XML encoding
     */
    private static final String XMLTAG = "controller";

    /**
     * The XML Stag for XML encoding -- use this static method when you want to see if a given
     * String equals the xml tag for this class
     */
    public static String xmlTag()
    {
        return XMLTAG;
    }

    /**
     * The XML Stag for XML encoding -- use this method to find out the run-time xml tag of an
     * object
     */
    @Override
    public String getXMLTag()
    {
        return XMLTAG;
    }
}
