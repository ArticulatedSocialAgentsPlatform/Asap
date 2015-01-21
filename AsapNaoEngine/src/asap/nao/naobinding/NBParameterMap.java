/*******************************************************************************
 *******************************************************************************/
package asap.nao.naobinding;

import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

public class NBParameterMap extends XMLStructureAdapter
{
    NaoUnitSpec spec;
    
    public NBParameterMap(NaoUnitSpec s)
    {
        spec = s;
    }
    
    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        while (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            if (tag.equals(NaoUnitParameter.xmlTag())) 
            {
                NaoUnitParameter nup = new NaoUnitParameter();
                nup.readXML(tokenizer);
                spec.addParameter(nup);
            }
        }
    }
    
   /*
    * The XML Stag for XML encoding
    */
   private static final String XMLTAG = "parametermap";
 
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
