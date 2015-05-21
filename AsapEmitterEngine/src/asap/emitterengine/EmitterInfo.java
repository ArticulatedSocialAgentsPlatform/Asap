/*******************************************************************************
 *******************************************************************************/
package asap.emitterengine;


import java.util.ArrayList;

import asap.emitterengine.bml.CreateEmitterBehaviour;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 
 * @author Dennis Reidsma
 */
public abstract class EmitterInfo 
{
    
    public abstract String getNamespace();
    public static String namespace()
    {
      return null;
    }
    public abstract String getXMLTag();
    public static String xmlTag()
    {
      return null;
    }

    public  boolean specifiesFloatParameter(String name)
    {
      return false;
    }
    public  boolean specifiesStringParameter(String name)
    {
      return false;
    }
    
    public  ArrayList<String> getOptionalParameters()
    {
      return new ArrayList<String>();
    }
    public  ArrayList<String> getRequiredParameters()
    {
      return new ArrayList<String>();
    }
    
    public abstract Class<? extends Emitter> getEmitterClass();
    public abstract Class<? extends CreateEmitterBehaviour> getCreateEmitterBehaviour();
     
}
