/*******************************************************************************
 *******************************************************************************/
package asap.nao.naobinding;

import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

public class NaoUnitSpecConstraints extends XMLStructureAdapter
{
    private NaoUnitSpec spec;
    
    public NaoUnitSpecConstraints(NaoUnitSpec s)
    {
        spec = s;
    }
    
    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        while (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            if (tag.equals(NaoUnitSpecConstraint.xmlTag())) 
            {
                NaoUnitSpecConstraint c = new NaoUnitSpecConstraint();
                c.readXML(tokenizer);
                spec.addConstraint(c);
            }
        }
    }
    
   /*
    * The XML Stag for XML encoding
    */
   private static final String XMLTAG = "constraints";
 
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
