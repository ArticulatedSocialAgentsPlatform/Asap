/*******************************************************************************
 *******************************************************************************/
package asap.picture.picturebinding;

import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asap.picture.planunit.AddAnimationDirPU;
import asap.picture.planunit.AddAnimationXMLPU;
import asap.picture.planunit.AddImagePU;
import asap.picture.planunit.PictureUnit;
import asap.picture.planunit.SetImagePU;

public class PictureUnitAssembler extends XMLStructureAdapter
{
    private static Logger logger = LoggerFactory
            .getLogger(PictureUnitAssembler.class.getName());

    private PictureUnit pictureUnit;

    public PictureUnitAssembler()
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
    public PictureUnit getPictureUnit()
    {
        return pictureUnit;
    }

   /*
    * The XML Stag for XML encoding
    */
   private static final String XMLTAG = "PictureUnit";
 
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
