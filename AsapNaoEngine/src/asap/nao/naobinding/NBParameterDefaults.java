/*******************************************************************************
 *******************************************************************************/
package asap.nao.naobinding;

import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

/**
 * Holds default parameters values for a NaoUnit.
 * @author Robin ten Buuren
 *
 */
public class NBParameterDefaults extends XMLStructureAdapter
{
    private NaoUnitSpec spec;
    
    public NBParameterDefaults(NaoUnitSpec s)
    {
        spec = s;
    }
    
    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        while (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            if (tag.equals(NaoUnitParameterDefault.xmlTag())) 
            {
                NaoUnitParameterDefault nupc = new NaoUnitParameterDefault();
                nupc.readXML(tokenizer);
                spec.addParameterDefault(nupc);
            }
        }
    }
    
   /*
    * The XML Stag for XML encoding
    */
   private static final String XMLTAG = "parameterdefaults";
 
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
