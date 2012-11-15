package hmi.shaderengine.bml;
import java.util.HashMap;

import hmi.shaderengine.*;
import asap.bml.ext.bmlt.*;
import hmi.xml.XMLTokenizer;
import java.io.IOException;
/**
 * @author Dennis Reidsma
 */
public class SetShaderParameterBehaviour extends ShaderBehaviour
{

    public SetShaderParameterBehaviour(String bmlId,XMLTokenizer tokenizer)throws IOException
    {
        super(bmlId);
        readXML(tokenizer);
    }
    
     
    @Override
    public boolean specifiesParameter(String name)
    {
        return (name.equals("mesh")||name.equals("material")||name.equals("parameter")||name.equals("value"));
    }
    
    @Override
    public StringBuilder appendAttributeString(StringBuilder buf)
    {
        appendAttribute(buf, "mesh", getStringParameterValue("mesh"));
        appendAttribute(buf, "material", getStringParameterValue("material"));
        appendAttribute(buf, "parameter", getStringParameterValue("parameter"));
        appendAttribute(buf, "value", getFloatParameterValue("value"));
        return super.appendAttributeString(buf);
    }

    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
		BMLTParameter p = new BMLTParameter();
		p.name="mesh";
		p.value=getRequiredAttribute("mesh", attrMap, tokenizer);
		parameters.put("mesh", p);
		p = new BMLTParameter();
		p.name="material";
		p.value=getRequiredAttribute("material", attrMap, tokenizer);
		parameters.put("material", p);
		p = new BMLTParameter();
		p.name="parameter";
		p.value=getRequiredAttribute("parameter", attrMap, tokenizer);
		parameters.put("parameter", p);
		p = new BMLTParameter();
		p.name="value";
		p.value=getRequiredAttribute("value", attrMap, tokenizer);
		parameters.put("value", p);

        super.decodeAttributes(attrMap, tokenizer);
    }
    

    /**
     * The XML Stag for XML encoding -- use this static method when you want to see if a given
     * String equals the xml tag for this class
     */
    public static String xmlTag()
    {
        return "setshaderparameter";
    }

    /**
     * The XML Stag for XML encoding -- use this method to find out the run-time xml tag of an
     * object
     */
    @Override
    public String getXMLTag()
    {
        return "setshaderparameter";
    }    
}
