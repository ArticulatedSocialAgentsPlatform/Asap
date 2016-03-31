/*******************************************************************************
 *******************************************************************************/
package asap.faceengine.viseme;

import hmi.faceanimation.model.FACSConfiguration;
import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;

/**
 * given a viseme number, return the appropriate FACSConfiguration
 * 
 * The mapping is read from a resource file. Note: meaning of viseme number dependent on chose viseme set, e.g., Disney 13, or IKP
 * 
 * @author Dennis Reidsma
 * @author Merel Brandon
 */
public class VisemeToFACSMapping extends XMLStructureAdapter
{

    private HashMap<Integer, FACSConfiguration> mappings = new HashMap<Integer, FACSConfiguration>();

    /**
     * Get the FACSConfiguration for viseme vis. Returns null if not found.
     */
    public FACSConfiguration getFACSConfigurationForViseme(int vis)
    {
        return mappings.get(Integer.valueOf(vis));
    }

    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        while (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            if (!tag.equals("Mapping")) throw new XMLScanException("Unknown element in VisemeToFACSMapping: " + tag);
            HashMap<String, String> attrMap = tokenizer.getAttributes();
            int viseme = getRequiredIntAttribute("viseme", attrMap, tokenizer);

            tokenizer.takeSTag("Mapping");
            FACSConfiguration facsConfig = new FACSConfiguration();
            if (tokenizer.atSTag("FACSConfiguration"))
            { // moet een exception op gooien
                tokenizer.takeSTag("FACSConfiguration");
                facsConfig.decodeContent(tokenizer);
                tokenizer.takeETag("FACSConfiguration");
            }
            mappings.put(Integer.valueOf(viseme), facsConfig);
            tokenizer.takeETag("Mapping");
        }
    }

    /*
     * The XML Stag for XML encoding
     */
    private static final String XMLTAG = "VisemeToFACSMapping";

    /**
     * The XML Stag for XML encoding -- use this static method when you want to see if a given String equals
     * the xml tag for this class
     */
    public static String xmlTag()
    {
        return XMLTAG;
    }

    /**
     * The XML Stag for XML encoding -- use this method to find out the run-time xml tag of an object
     */
    @Override
    public String getXMLTag()
    {
        return XMLTAG;
    }
}
