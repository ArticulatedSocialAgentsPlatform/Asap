package hmi.naoqiengine.bml;
import java.util.HashMap;

import hmi.naoqiengine.*;
import asap.bml.ext.bmlt.*;
import hmi.xml.XMLTokenizer;
import java.io.IOException;
/**
 * @author Dennis Reidsma
 */
public class PlayCachedClipBehaviour extends NaoQiBehaviour
{

    public PlayCachedClipBehaviour(String bmlId,XMLTokenizer tokenizer)throws IOException
    {
        super(bmlId);
        readXML(tokenizer);
    }
    
     
    @Override
    public boolean specifiesParameter(String name)
    {
        return (name.equals("name"));
    }
    
    @Override
    public StringBuilder appendAttributeString(StringBuilder buf)
    {
        appendAttribute(buf, "name", getStringParameterValue("name"));
        return super.appendAttributeString(buf);
    }

    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
		BMLTParameter p = new BMLTParameter();
		p.name="clip";
		p.value=getRequiredAttribute("clip", attrMap, tokenizer);
		parameters.put("clip", p);
        super.decodeAttributes(attrMap, tokenizer);
    }
    

    /**
     * The XML Stag for XML encoding -- use this static method when you want to see if a given
     * String equals the xml tag for this class
     */
    public static String xmlTag()
    {
        return "playcachedclip";
    }

    /**
     * The XML Stag for XML encoding -- use this method to find out the run-time xml tag of an
     * object
     */
    @Override
    public String getXMLTag()
    {
        return "playcachedclip";
    }    
}
