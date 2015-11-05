package asap.faceengine.lipsync;

import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;

/**
 * Fills a HashMap with a set of a viseme-number and the corresponding parameters for a dominance function read from a XML-file
 * 
 * @author mklemens
 */
public class DominanceParameterLoader extends XMLStructureAdapter
{
	// A HashMap containing the parameters for a corresponding viseme-type
    private HashMap<String, DominanceParameters> mappings = new HashMap<String, DominanceParameters>();
    
    /*
     * Returns the HashMap filled with pairs of viseme-numbers and parameters
     */
    public HashMap<String, DominanceParameters> getParameterMappings() {
    	return mappings;
    }
  
    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        while (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            if (!tag.equals("ParamSet")) throw new XMLScanException("Unknown element in DominanceParameter-File: "+tag);
            
            // Get all the parameter values from the XML-file!
            HashMap<String, String> attrMap = tokenizer.getAttributes();
            String soundclass 	= 	getRequiredAttribute("soundclass", attrMap, tokenizer);
            double magnitude 	= 	Double.valueOf(getRequiredAttribute("magnitude", attrMap, tokenizer));
            double stretchLeft	=	getOptionalDoubleAttribute("stretchLeft", attrMap, 1.0);
            double stretchRight	=	getOptionalDoubleAttribute("stretchRight", attrMap, 1.0);
            double rateLeft		=	getOptionalDoubleAttribute("rateLeft", attrMap, -0.03);
            double rateRight	=	getOptionalDoubleAttribute("rateRight", attrMap, 0.03);
            double peak			=	getOptionalDoubleAttribute("peak", attrMap, 0.5);
            double startOffset	=	getOptionalDoubleAttribute("startOffsetMultiplicator", attrMap, 0.5);
            double endOffset	=	getOptionalDoubleAttribute("endOffsetMultiplicator", attrMap, 1.5);
            
            mappings.put(soundclass, new DominanceParameters(magnitude, stretchLeft, stretchRight, rateLeft, rateRight, peak, startOffset, endOffset));

            tokenizer.takeSTag("ParamSet");
            tokenizer.takeETag("ParamSet");
        }
    }

    /*
     * The XML Stag for XML encoding
     */
    private static final String XMLTAG = "DominanceParameterMapping";
 
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
