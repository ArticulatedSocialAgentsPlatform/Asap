/*******************************************************************************
 *******************************************************************************/
package asap.realizerembodiments.impl;

import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.util.HashMap;

import saiba.bml.core.BMLBehaviorAttributeExtension;

/**
 * Creates a BMLAttributeExtension from an XML description
 * @author Herwin
 *
 */
public class BMLAttributeExtensionAssembler extends XMLStructureAdapter
{
    private Class<? extends BMLBehaviorAttributeExtension> extension;

    public Class<? extends BMLBehaviorAttributeExtension> getAttributeExtension()
    {
        return extension;
    }
    
    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)  
    {
        String className = getRequiredAttribute("class", attrMap, tokenizer);
        try
        {
            extension = Class.forName(className).asSubclass(BMLBehaviorAttributeExtension.class);
        }
        catch (ClassNotFoundException e)
        {
            throw new XMLScanException("Cannot instantiate "+className,e);
        }
    }
    
    private static final String XMLTAG = "BMLAttributeExtension";    
    /**
     * The XML Stag for XML encoding -- use this static method when you want to see if a given String equals
     * the xml tag for this class
     */
    public static String xmlTag()
    {
        return XMLTAG;
    }

    /**
     * The XML Stag for XML encoding -- use this method to find out the run-time xml tag of an object
     */
    @Override
    public String getXMLTag()
    {
        return XMLTAG;
    }
}
