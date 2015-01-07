/*******************************************************************************
 *******************************************************************************/
package asap.sapittsbinding;

import hmi.tts.BMLTTSBridge;
import hmi.tts.sapi5.SAPI5SSMLTTSBridge;
import hmi.tts.sapi5.SAPI5TTSGenerator;
import hmi.tts.sapi5.SAPITTSBridge;
import hmi.util.StringUtil;
import saiba.bml.core.SpeechBehaviour;
import asap.bml.ext.msapi.MSApiBehaviour;
import asap.bml.ext.ssml.SSMLBehaviour;
import asap.realizer.planunit.InvalidParameterException;
import asap.realizer.planunit.ParameterException;
import asap.realizer.planunit.ParameterNotFoundException;
import asap.speechengine.ttsbinding.TTSBinding;

/**
 * Binds BML speech, msapi, ssml behaviors to bridges with a sapi ttsgenerator. 
 * @author welberge
 */
public class SAPITTSBinding extends TTSBinding
{
    private final SAPI5TTSGenerator sapiTTSGenerator;
    
    public SAPITTSBinding()
    {
        sapiTTSGenerator = new SAPI5TTSGenerator();
        ttsGenerator = sapiTTSGenerator;
        
        ttsBridgeMap.put(SpeechBehaviour.class,new BMLTTSBridge(sapiTTSGenerator));
        ttsBridgeMap.put(MSApiBehaviour.class,new SAPITTSBridge(sapiTTSGenerator));
        ttsBridgeMap.put(SSMLBehaviour.class, new SAPI5SSMLTTSBridge(sapiTTSGenerator));
        
        supportedBehaviours.add(SSMLBehaviour.class);
        supportedBehaviours.add(MSApiBehaviour.class);
    }
    
    @Override
    public void setParameterValue(String parameter, String value)throws ParameterException
    {
        if(StringUtil.isNumeric(value))
        {
            setFloatParameterValue(parameter, Float.parseFloat(value));
        }
        throw new InvalidParameterException(parameter,value);
    }
    
    @Override
    public float getFloatParameterValue(String parameter)throws ParameterException
    {
        if(parameter.equals("rate"))
        {
            return sapiTTSGenerator.getRate();
        }
        else if(parameter.equals("volume"))
        {
            return sapiTTSGenerator.getVolume();
        }
        throw new ParameterNotFoundException(parameter);
    }
    
    @Override
    public String getParameterValue(String parameter)throws ParameterException
    {
        return ""+getFloatParameterValue(parameter);        
    }
    
    @Override
    public void setFloatParameterValue(String parameter, float value)throws ParameterException
    {
        if(parameter.equals("rate"))
        {
            sapiTTSGenerator.setRate((int)value);
        }
        else if(parameter.equals("volume"))
        {
            sapiTTSGenerator.setVolume((int)value);
        }
        else
        {
            throw new ParameterNotFoundException(parameter);
    }
    }

    @Override
    public void cleanup()
    {
        sapiTTSGenerator.cleanup();
    }
}
