/*******************************************************************************
 *******************************************************************************/
package asap.nao.naobinding;

import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.util.HashMap;

public class NaoUnitSpecConstraint extends XMLStructureAdapter
{
    public String name;
    public String value;
    
    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        name = getRequiredAttribute("name", attrMap, tokenizer);
        value = getRequiredAttribute("value", attrMap, tokenizer);
    }

    @Override
    public StringBuilder appendAttributeString(StringBuilder buf)
    {
        appendAttribute(buf, "name", name);
        appendAttribute(buf, "value", value);        
        return super.appendAttributeString(buf);
    }
    
   /*
    * The XML Stag for XML encoding
    */
   private static final String XMLTAG = "constraint";
 
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
