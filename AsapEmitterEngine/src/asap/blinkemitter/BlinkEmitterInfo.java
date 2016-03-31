/*******************************************************************************
 *******************************************************************************/
package asap.blinkemitter;


import java.util.ArrayList;

import asap.emitterengine.Emitter;
import asap.emitterengine.EmitterInfo;
import asap.emitterengine.bml.CreateEmitterBehaviour;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 
 * @author Dennis Reidsma
 */
public class BlinkEmitterInfo extends EmitterInfo
{
    
    public BlinkEmitterInfo()
    {
      optionalParameters.add("range");
      optionalParameters.add("avgwaitingtime");
    }
      
    static final String BMLTNAMESPACE = "http://hmi.ewi.utwente.nl/bmlt";
    
    public static String namespace()
    {
      return BMLTNAMESPACE;
    }
    @Override
    public String getNamespace()
    {
      return BMLTNAMESPACE;
    }
    
    static final String XMLTAG = "blinkemitter";
    
    public static String xmlTag()
    {
      return XMLTAG;
    }
    @Override
    public String getXMLTag()
    {
      return XMLTAG;
    }

    @Override
    public  boolean specifiesFloatParameter(String name)
    {
      return optionalParameters.contains(name) || requiredParameters.contains(name);
    }
    @Override
    public  boolean specifiesStringParameter(String name)
    {
      return false;
    }
    
    private  ArrayList<String> optionalParameters = new ArrayList<String>();
    private  ArrayList<String> requiredParameters = new ArrayList<String>();
    
    @Override
    public  ArrayList<String> getOptionalParameters()
    {
      return optionalParameters;
    }

    @Override
    public  ArrayList<String> getRequiredParameters()
    {
      return requiredParameters;
    }

    @Override
    public Class<? extends Emitter> getEmitterClass()
    {
      return BlinkEmitter.class;
    }
    @Override
    public Class<? extends CreateEmitterBehaviour> getCreateEmitterBehaviour()
    {
      return CreateBlinkEmitterBehaviour.class;
    }
         
}
