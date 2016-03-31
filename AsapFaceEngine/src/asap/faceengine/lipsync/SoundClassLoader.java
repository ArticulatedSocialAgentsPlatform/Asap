package asap.faceengine.lipsync;

import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;

/**
 * Fills a HashMap with a set of a phoneme and the corresponding sound-class read from a XML-file. There are overall eight different sound-classes.
 * 
 * @author mklemens
 */
public class SoundClassLoader extends XMLStructureAdapter
{
	// A HashMap containing the sound-class for the phoneme
    private HashMap<String, String> mappings = new HashMap<String, String>();
    
    /*
     * Returns the HashMap filled with pairs of phonemes and their sound-class
     */
    public HashMap<String, String> getSoundClassMappings() {
    	return mappings;
    }
  
    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        while (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            if (!tag.equals("ParamSet")) throw new XMLScanException("Unknown element in PhonemeToClass-File: "+tag);
            
            // Get sound-class for every phoneme from the XML-file!
            HashMap<String, String> attrMap = tokenizer.getAttributes();
            String phoneme 		= 	getRequiredAttribute("phoneme", attrMap, tokenizer);
            String soundclass	= 	getRequiredAttribute("soundclass", attrMap, tokenizer);
            
            mappings.put(phoneme, soundclass);

            tokenizer.takeSTag("ParamSet");
            tokenizer.takeETag("ParamSet");
        }
    }

    /*
     * The XML Stag for XML encoding
     */
    private static final String XMLTAG = "PhonemeClassMapping";
 
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
