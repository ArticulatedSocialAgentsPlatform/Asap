/*******************************************************************************
 *******************************************************************************/
package asap.emitterengine.bml;
import hmi.xml.XMLTokenizer;

import java.util.HashMap;

import asap.bml.ext.bmlt.BMLTParameter;
import asap.emitterengine.EmitterInfo;

/**
 * Create Emitter behavior
 * @author Dennis Reidsma
 */
public class CreateEmitterBehaviour extends EmitterBehaviour
{

    public CreateEmitterBehaviour(String bmlId)
    {
        super(bmlId);        
    }
    
    static EmitterInfo emitterInfo = null;
     
    protected static void setEmitterInfo(EmitterInfo ei)
    {
      emitterInfo = ei;
    }    

    @Override
    public boolean specifiesParameter(String name)
    {
        return emitterInfo.specifiesFloatParameter(name)||emitterInfo.specifiesStringParameter(name);
    }
    
    @Override
    public StringBuilder appendAttributeString(StringBuilder buf)
    {
        for (String name: emitterInfo.getRequiredParameters())
        {
          appendAttribute(buf, name, getStringParameterValue(name));
        }
        for (String name: emitterInfo.getOptionalParameters())
        {
          if (getStringParameterValue(name) != null)
          {
            appendAttribute(buf, name, getStringParameterValue(name));
          }
        }
        return super.appendAttributeString(buf);
    }

    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        for (String name: emitterInfo.getRequiredParameters())
        {
            BMLTParameter p = new BMLTParameter();
            p.name=name;
            p.value=getRequiredAttribute(name, attrMap, tokenizer);
            parameters.put(name, p);
        }
        for (String name: emitterInfo.getOptionalParameters())
        {
          String value = getOptionalAttribute(name, attrMap);
          if (value != null)
          {
            BMLTParameter p = new BMLTParameter();
            p.name=name;
            p.value=value;
            parameters.put(name, p);
          }
        }
        super.decodeAttributes(attrMap, tokenizer);
    }
    

    @Override
    public String getNamespace()
    {
        return emitterInfo.getNamespace();
    }

    /**
     * The XML Stag for XML encoding -- use this static method when you want to see if a given
     * String equals the xml tag for this class
     */
    public static String xmlTag()
    {
        return emitterInfo.getXMLTag();
    }

    /**
     * The XML Stag for XML encoding -- use this method to find out the run-time xml tag of an
     * object
     */
    @Override
    public String getXMLTag()
    {
        return emitterInfo.getXMLTag();
    }    
}
