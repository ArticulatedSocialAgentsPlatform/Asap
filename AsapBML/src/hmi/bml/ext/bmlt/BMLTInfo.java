package hmi.bml.ext.bmlt;

import hmi.bml.BMLInfo;
import hmi.bml.core.Behaviour;
import hmi.bml.ext.maryxml.MaryAllophonesBehaviour;
import hmi.bml.ext.maryxml.MaryWordsBehaviour;
import hmi.bml.ext.maryxml.MaryXMLBehaviour;
import hmi.bml.ext.msapi.MSApiBehaviour;
import hmi.bml.ext.ssml.SSMLBehaviour;

import com.google.common.collect.ImmutableMap;

/**
 * BMLTInfo initializer 
 * @author Herwin
 *
 */
public final class BMLTInfo
{
    // /Behaviors that are parsed
    private static final ImmutableMap<String, Class<? extends Behaviour>> BEHAVIOR_TYPES =
            new ImmutableMap.Builder<String, Class<? extends Behaviour>>()
            .put(BMLTProcAnimationBehaviour.xmlTag(), BMLTProcAnimationBehaviour.class)            
            .put(BMLTControllerBehaviour.xmlTag(), BMLTControllerBehaviour.class)
            .put(BMLTNoiseBehaviour.xmlTag(), BMLTNoiseBehaviour.class)
            .put(BMLTTransitionBehaviour.xmlTag(), BMLTTransitionBehaviour.class)
            .put(BMLTKeyframeBehaviour.xmlTag(), BMLTKeyframeBehaviour.class)
            .put(BMLTAudioFileBehaviour.xmlTag(), BMLTAudioFileBehaviour.class)
            .put(BMLTFaceMorphBehaviour.xmlTag(), BMLTFaceMorphBehaviour.class)
            .put(BMLTInterruptBehaviour.xmlTag(), BMLTInterruptBehaviour.class)
            .put(BMLTActivateBehaviour.xmlTag(), BMLTActivateBehaviour.class)
            .put(BMLTParameterValueChangeBehaviour.xmlTag(), BMLTParameterValueChangeBehaviour.class)            
            .build();
    // /Description levels that can be parsed
    private static final ImmutableMap<String, Class<? extends Behaviour>> DESCRIPTION_EXTENSIONS = 
        new ImmutableMap.Builder<String, Class<? extends Behaviour>>()
            .put(BMLTProcAnimationBehaviour.xmlTag(), BMLTProcAnimationBehaviour.class)
            .put(BMLTControllerBehaviour.xmlTag(), BMLTControllerBehaviour.class)
            .put(BMLTKeyframeBehaviour.xmlTag(), BMLTKeyframeBehaviour.class)            
            .put("application/msapi+xml", MSApiBehaviour.class)
            .put("application/ssml+xml", SSMLBehaviour.class)
            .put("maryxml", MaryXMLBehaviour.class)
            .put("marywords", MaryWordsBehaviour.class)
            .put("maryallophones", MaryAllophonesBehaviour.class)
            .put(BMLTAudioFileBehaviour.xmlTag(), BMLTAudioFileBehaviour.class)
            
            .build();
    
    public static void init()
    {
        BMLInfo.addBehaviourTypes(BEHAVIOR_TYPES);
        BMLInfo.addDescriptionExtensions(DESCRIPTION_EXTENSIONS);
    }
}
