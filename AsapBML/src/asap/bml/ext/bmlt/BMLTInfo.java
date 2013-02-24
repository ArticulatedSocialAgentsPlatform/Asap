package asap.bml.ext.bmlt;

import saiba.bml.BMLInfo;
import saiba.bml.core.Behaviour;
import saiba.bml.core.GazeBehaviour;
import saiba.bml.core.SpeechBehaviour;

import asap.bml.ext.bmla.BMLAInterruptBehaviour;
import asap.bml.ext.maryxml.MaryAllophonesBehaviour;
import asap.bml.ext.maryxml.MaryWordsBehaviour;
import asap.bml.ext.maryxml.MaryXMLBehaviour;
import asap.bml.ext.msapi.MSApiBehaviour;
import asap.bml.ext.ssml.SSMLBehaviour;

import com.google.common.collect.ImmutableMap;

/**
 * BMLTInfo initializer 
 * @author Herwin
 *
 */
public final class BMLTInfo
{
    public static final String ANTICIPATORBLOCKID = "anticipators";
    private BMLTInfo(){}
    // /Behaviors that are parsed
    private static final ImmutableMap<String, Class<? extends Behaviour>> BEHAVIOR_TYPES =
            new ImmutableMap.Builder<String, Class<? extends Behaviour>>()
            .put(BMLTProcAnimationBehaviour.xmlTag(), BMLTProcAnimationBehaviour.class)            
            .put(BMLTControllerBehaviour.xmlTag(), BMLTControllerBehaviour.class)
            .put(BMLTNoiseBehaviour.xmlTag(), BMLTNoiseBehaviour.class)            
            .put(BMLTKeyframeBehaviour.xmlTag(), BMLTKeyframeBehaviour.class)
            .put(BMLTAudioFileBehaviour.xmlTag(), BMLTAudioFileBehaviour.class)
            .put(BMLTFaceMorphBehaviour.xmlTag(), BMLTFaceMorphBehaviour.class)
            .put(BMLAInterruptBehaviour.xmlTag(), BMLAInterruptBehaviour.class)
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
        BMLInfo.addExternalBlockId(ANTICIPATORBLOCKID);
        BMLInfo.addCustomStringAttribute(GazeBehaviour.class, BMLTBehaviour.BMLTNAMESPACE, "dynamic");
        BMLInfo.addCustomStringAttribute(SpeechBehaviour.class, BMLTBehaviour.BMLTNAMESPACE, "voice");
    }
}
