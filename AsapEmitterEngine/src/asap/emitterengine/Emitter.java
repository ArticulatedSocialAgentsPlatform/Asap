/*******************************************************************************
 *******************************************************************************/
package asap.emitterengine;

import java.util.ArrayList;

import asap.realizerport.RealizerPort;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * Emits bml behaviors to a RealizerPort.
 * 
 * @author Dennis Reidsma
 */
public abstract class Emitter 
{

    public abstract void setRealizerPort(RealizerPort port);
    public abstract void setId(String id);
    public abstract String getId();
    
    public static String namespace()
    {
      return "http://hmi.ewi.utwente.nl/bmlt";
    }
    public abstract String getNamespace();
    public static String xmlTag() 
    {
      return "emitter";
    }
    public abstract String getXMLTag();
    
    /** start emitter. Needs to be already connected to realizerport. */
    public abstract void start();
    
    /** 
     * stop and clean up emitter as soon as possible. After stop was called, 
     * no new BML should be emitted, but cleaning up emission threads may take a while. 
     */
    public abstract void stop();

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
    public abstract void setParameterValue(String name, String value);
    public abstract void setFloatParameterValue(String name, float value);
    public abstract String getParameterValue(String name);
    public abstract float getFloatParameterValue(String name);
    public abstract boolean hasValidParameters();
    
}
