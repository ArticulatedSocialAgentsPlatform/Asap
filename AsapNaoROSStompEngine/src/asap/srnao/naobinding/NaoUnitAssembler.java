/*******************************************************************************
 *******************************************************************************/
package asap.srnao.naobinding;

import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asap.srnao.planunit.AddAnimationDirPU;
import asap.srnao.planunit.AddAnimationXMLPU;
import asap.srnao.planunit.AddImagePU;
import asap.srnao.planunit.NaoUnit;
import asap.srnao.planunit.SetImagePU;

public class NaoUnitAssembler extends XMLStructureAdapter
{
    private static Logger logger = LoggerFactory
            .getLogger(NaoUnitAssembler.class.getName());

    private NaoUnit pictureUnit;

    public NaoUnitAssembler()
    {
    }

    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        String type = getRequiredAttribute("type", attrMap, tokenizer);
        
        if (type.equals("SetImagePU"))
        {
            pictureUnit = new SetImagePU();
        } else if (type.equals("AddImagePU"))
        {
            pictureUnit = new AddImagePU();
        }  
        else if (type.equals("AddAnimationDirPU"))
        {
            pictureUnit = new AddAnimationDirPU();
        } 
        else if (type.equals("AddAnimationXMLPU"))
        {
            pictureUnit = new AddAnimationXMLPU();
        } 
        else
        {
          logger.warn("Cannot read PictureUnit type \"{}\" in PictureBinding; omitting this PictureUnit", type);
        }
        
    }

    /**
     * @return the pictureUnit
     */
    public NaoUnit getPictureUnit()
    {
        return pictureUnit;
    }

   /*
    * The XML Stag for XML encoding
    */
   private static final String XMLTAG = "NaoUnit";
 
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
