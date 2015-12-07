/*******************************************************************************
 *******************************************************************************/
package asap.bml.ext.bmlt;

import saiba.bml.BMLInfo;
import saiba.bml.core.Behaviour;
import saiba.bml.core.GazeBehaviour;
import saiba.bml.core.GestureBehaviour;
import saiba.bml.core.HeadBehaviour;
import saiba.bml.core.SpeechBehaviour;
import saiba.bml.feedback.BMLBlockPredictionFeedback;
import saiba.bml.feedback.BMLBlockProgressFeedback;
import saiba.bml.feedback.BMLSyncPointProgressFeedback;
import asap.bml.ext.bmla.BMLAActivateBehaviour;
import asap.bml.ext.bmla.BMLAInfo;
import asap.bml.ext.bmla.BMLAInterruptBehaviour;
import asap.bml.ext.bmla.BMLAParameterValueChangeBehaviour;
import asap.bml.ext.bmla.feedback.BMLABlockPredictionFeedback;
import asap.bml.ext.bmla.feedback.BMLABlockProgressFeedback;
import asap.bml.ext.bmla.feedback.BMLASyncPointProgressFeedback;
import asap.bml.ext.maryxml.MaryAllophonesBehaviour;
import asap.bml.ext.maryxml.MaryWordsBehaviour;
import asap.bml.ext.maryxml.MaryXMLBehaviour;
import asap.bml.ext.msapi.MSApiBehaviour;
import asap.bml.ext.murml.MURMLFaceBehaviour;
import asap.bml.ext.murml.MURMLGestureBehaviour;
import asap.bml.ext.ssml.SSMLBehaviour;

import com.google.common.collect.ImmutableMap;

/**
 * BMLTInfo initializer 
 * @author Herwin
 *
 */
public final class BMLTInfo
{
    private BMLTInfo(){}
    // /Behaviors that are parsed
    private static final ImmutableMap<String, Class<? extends Behaviour>> BEHAVIOR_TYPES =
            new ImmutableMap.Builder<String, Class<? extends Behaviour>>()
            .put(BMLTTextBehaviour.xmlTag(), BMLTTextBehaviour.class)
            .put(BMLTProcAnimationBehaviour.xmlTag(), BMLTProcAnimationBehaviour.class)
            .put(BMLTProcAnimationGestureBehaviour.xmlTag(), BMLTProcAnimationGestureBehaviour.class)
            .put(BMLTControllerBehaviour.xmlTag(), BMLTControllerBehaviour.class)
            .put(BMLTNoiseBehaviour.xmlTag(), BMLTNoiseBehaviour.class)            
            .put(BMLTKeyframeBehaviour.xmlTag(), BMLTKeyframeBehaviour.class)
            .put(BMLTFaceKeyframeBehaviour.xmlTag(),BMLTFaceKeyframeBehaviour.class)
            .put(BMLTAudioFileBehaviour.xmlTag(), BMLTAudioFileBehaviour.class)
            .put(BMLTFaceMorphBehaviour.xmlTag(), BMLTFaceMorphBehaviour.class)
            .put(MURMLGestureBehaviour.xmlTag(), MURMLGestureBehaviour.class)
            .put(MURMLFaceBehaviour.xmlTag(), MURMLFaceBehaviour.class)
            .put(BMLAInterruptBehaviour.xmlTag(), BMLAInterruptBehaviour.class)
            .put(BMLAActivateBehaviour.xmlTag(), BMLAActivateBehaviour.class)
            .put(BMLAParameterValueChangeBehaviour.xmlTag(), BMLAParameterValueChangeBehaviour.class)            
            .put(SSMLBehaviour.xmlTag(), SSMLBehaviour.class)
            .put(MSApiBehaviour.xmlTag(), MSApiBehaviour.class)
            .put(MaryXMLBehaviour.xmlTag(), MaryXMLBehaviour.class)            
            .build();
    // /Description levels that can be parsed
    private static final ImmutableMap<String, Class<? extends Behaviour>> DESCRIPTION_EXTENSIONS = 
        new ImmutableMap.Builder<String, Class<? extends Behaviour>>()
            .put(BMLTProcAnimationBehaviour.xmlTag(), BMLTProcAnimationBehaviour.class)
            .put(BMLTProcAnimationGestureBehaviour.xmlTag(), BMLTProcAnimationGestureBehaviour.class)
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
        BMLInfo.addCustomStringAttribute(GazeBehaviour.class, BMLTBehaviour.BMLTNAMESPACE, "dynamic");
        BMLInfo.addCustomStringAttribute(SpeechBehaviour.class, BMLTBehaviour.BMLTNAMESPACE, "voice");
        BMLInfo.addCustomFeedbackStringAttribute(BMLABlockProgressFeedback.class, BMLAInfo.BMLA_NAMESPACE, "posixTime");
        BMLInfo.addCustomFeedbackStringAttribute(BMLBlockProgressFeedback.class, BMLAInfo.BMLA_NAMESPACE, "posixTime");
        BMLInfo.addCustomFeedbackStringAttribute(BMLASyncPointProgressFeedback.class, BMLAInfo.BMLA_NAMESPACE, "posixTime");
        BMLInfo.addCustomFeedbackStringAttribute(BMLSyncPointProgressFeedback.class, BMLAInfo.BMLA_NAMESPACE, "posixTime");
        BMLInfo.addCustomFeedbackStringAttribute(BMLBlockPredictionFeedback.class, BMLAInfo.BMLA_NAMESPACE, "posixStartTime");
        BMLInfo.addCustomFeedbackStringAttribute(BMLBlockPredictionFeedback.class, BMLAInfo.BMLA_NAMESPACE, "posixEndTime");
        BMLInfo.addCustomFeedbackStringAttribute(BMLABlockPredictionFeedback.class, BMLAInfo.BMLA_NAMESPACE, "posixStartTime");
        BMLInfo.addCustomFeedbackStringAttribute(BMLABlockPredictionFeedback.class, BMLAInfo.BMLA_NAMESPACE, "posixEndTime");
        BMLInfo.addCustomFeedbackStringAttribute(BMLABlockProgressFeedback.class, BMLAInfo.BMLA_NAMESPACE, "status");
        BMLInfo.addCustomFeedbackStringAttribute(BMLBlockProgressFeedback.class, BMLAInfo.BMLA_NAMESPACE, "status");
        BMLInfo.addCustomFeedbackStringAttribute(BMLABlockPredictionFeedback.class, BMLAInfo.BMLA_NAMESPACE, "status");
        BMLInfo.addCustomFeedbackStringAttribute(BMLBlockPredictionFeedback.class, BMLAInfo.BMLA_NAMESPACE, "status");
        
        
        BMLInfo.addCustomFloatAttribute(GazeBehaviour.class, BMLAInfo.BMLA_NAMESPACE, "priority");
        BMLInfo.addCustomFloatAttribute(HeadBehaviour.class, BMLAInfo.BMLA_NAMESPACE, "priority");
        BMLInfo.addCustomFloatAttribute(GestureBehaviour.class, BMLAInfo.BMLA_NAMESPACE, "priority");
        BMLInfo.addCustomFloatAttribute(MURMLGestureBehaviour.class,BMLAInfo.BMLA_NAMESPACE, "priority");
        BMLInfo.addCustomFloatAttribute(BMLTProcAnimationBehaviour.class,BMLAInfo.BMLA_NAMESPACE, "priority");
        BMLInfo.addCustomFloatAttribute(BMLTNoiseBehaviour.class,BMLAInfo.BMLA_NAMESPACE, "priority");
        BMLInfo.addCustomFloatAttribute(BMLTKeyframeBehaviour.class,BMLAInfo.BMLA_NAMESPACE, "priority");
        BMLInfo.addCustomFloatAttribute(BMLTControllerBehaviour.class,BMLAInfo.BMLA_NAMESPACE, "priority");
    }
}
