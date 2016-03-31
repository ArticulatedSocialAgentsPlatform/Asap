package asap.faceengine.lipsync;

import java.io.IOException;
import java.util.HashMap;

import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

/**
 * Fills a HashMap with a set of a phoneme and the corresponding magnitude-modifiers read from a XML-file.
 * 
 * @author mklemens
 *
 */
public class MagnitudeLoader extends XMLStructureAdapter
{
	// A HashMap containing the magnitude-modifier for the phoneme
    private HashMap<String, Double> mappings = new HashMap<>();
    
    /*
     * Returns the HashMap filled with pairs of phonemes and their new magnitude-modifiers
     */
    public HashMap<String, Double> getMagnitudeModifierMappings() {
    	return mappings;
    }
  
    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        while (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            if (!tag.equals("ParamSet")) throw new XMLScanException("Unknown element in PhonemeMagnitude-File: "+tag);
            
            // Get magnitude-modifiers for every phoneme from the XML-file!
            HashMap<String, String> attrMap = tokenizer.getAttributes();
            String phoneme 				= 	getRequiredAttribute("phoneme", attrMap, tokenizer);
            double magnitudeModifier	=	Double.valueOf(getRequiredAttribute("magnitude", attrMap, tokenizer));
            
            mappings.put(phoneme, magnitudeModifier);

            tokenizer.takeSTag("ParamSet");
            tokenizer.takeETag("ParamSet");
        }
    }

    /*
     * The XML Stag for XML encoding
     */
    private static final String XMLTAG = "PhonemeMagnitudeMapping";
 
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
