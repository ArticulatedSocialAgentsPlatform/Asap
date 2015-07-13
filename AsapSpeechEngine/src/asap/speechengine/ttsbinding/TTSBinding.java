/*******************************************************************************
 *******************************************************************************/
package asap.speechengine.ttsbinding;

import hmi.tts.AbstractTTSGenerator;
import hmi.tts.TTSBridge;
import hmi.tts.TTSCallback;
import hmi.tts.TTSException;
import hmi.tts.TTSTiming;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import saiba.bml.core.Behaviour;
import asap.realizer.planunit.ParameterException;
import asap.realizer.planunit.ParameterNotFoundException;

/**
 * Binds a BML behavior to a TTSBridge
 * @author welberge
 */
public abstract class TTSBinding
{
    protected AbstractTTSGenerator ttsGenerator;

    protected List<Class<? extends Behaviour>> supportedBehaviours = new ArrayList<Class<? extends Behaviour>>();
    protected Map<Class<? extends Behaviour>, TTSBridge> ttsBridgeMap = new HashMap<Class<? extends Behaviour>, TTSBridge>();

    public List<Class<? extends Behaviour>> getSupportedBMLDescriptionExtensions()
    {
        return supportedBehaviours;
    }

    public void setParameterValue(String parameter, String value) throws ParameterException
    {
        throw new ParameterNotFoundException(parameter);
    }

    public void setFloatParameterValue(String parameter, float value) throws ParameterException
    {
        throw new ParameterNotFoundException(parameter);
    }

    public float getFloatParameterValue(String parameter) throws ParameterException
    {
        throw new ParameterNotFoundException(parameter);
    }

    public String getParameterValue(String parameter) throws ParameterException
    {
        throw new ParameterNotFoundException(parameter);
    }

    /**
     * Speaks out the sentence, does the appropriate callbacks, fills out visime,
     * phoneme, bookmark and word lists, uses the behClass to identify the content (e.g. BML, MS SAPI XML, Mary XML, SSML, ..)
     * 
     * @param text
     *            the text or script to speak
     * @throws TTSException
     */
    public TTSTiming speak(Class<? extends Behaviour> behClass, String text) throws TTSException
    {
        TTSBridge bridge = ttsBridgeMap.get(behClass);
        if(bridge!=null)
        {
            return bridge.speak(text);        
        }
        else
        {
            throw new TTSException("bridge not available", new Exception("bridge for "+behClass.getName()+" not available."));
        }
    }

    public TTSTiming speakToFile(Class<? extends Behaviour> behClass, String text, String filename) throws IOException,TTSException
    {
        TTSBridge bridge = ttsBridgeMap.get(behClass);
        if(bridge!=null)
        {
            return bridge.speakToFile(text, filename);        
        }
        else
        {
            throw new TTSException("bridge not available", new Exception("bridge for "+behClass.getName()+" not available."));
        }        
    }

    public TTSTiming getTiming(Class<? extends Behaviour> behClass, String text) throws TTSException
    {
        TTSBridge bridge = ttsBridgeMap.get(behClass);
        if (bridge != null)
        {
            return bridge.getTiming(text);
        }
        else
        {
            throw new TTSException("bridge not available", new Exception("bridge for "+behClass.getName()+" not available."));
        }
    }

    public void setCallback(TTSCallback cb)
    {
        ttsGenerator.setCallback(cb);
    }

    public void setVoice(String voice)
    {
        ttsGenerator.setVoice(voice);
    }

    public String getVoice()
    {
        return ttsGenerator.getVoice();
    }

    public String[] getVoices()
    {
        return ttsGenerator.getVoices();
    }

    public abstract void cleanup();
}
