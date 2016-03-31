/*******************************************************************************
 *******************************************************************************/
package asap.binding;

import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.util.HashMap;

/**
 * Encodes a default value for a planunit parameter
 * @author welberge
 *
 */
public class SpecParameterDefault extends XMLStructureAdapter
{
    public String name;
    public String value;
    
    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        name = getRequiredAttribute("name", attrMap, tokenizer);
        value = getRequiredAttribute("value", attrMap, tokenizer);
    }
    
   /*
    * The XML Stag for XML encoding
    */
   private static final String XMLTAG = "parameterdefault";
 
   /**
    * The XML Stag for XML encoding -- use this static method when you want to see if a given String equals
    * the xml tag for this class
    */
   public static String xmlTag() { return XMLTAG; }
 
   /**
    * The XML Stag for XML encoding -- use this method to find out the run-time xml tag of an object
    */
   @Override
   public String getXMLTag() {
      return XMLTAG;
   }

}
