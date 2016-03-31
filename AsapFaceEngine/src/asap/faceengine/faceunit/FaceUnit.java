/*******************************************************************************
 *******************************************************************************/
package asap.faceengine.faceunit;

import hmi.faceanimation.FaceController;
import hmi.faceanimation.converters.EmotionConverter;
import hmi.faceanimation.converters.FACSConverter;
import asap.motionunit.MotionUnit;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;

/**
 * A facial animation, typically with a peak-like structure. 
 * Contains a set of keys that map to 'world' time to animation time.
 * @author Dennis Reidsma
 */
public interface FaceUnit extends MotionUnit
{
    boolean hasValidParameters();    
   
    /**
     * Creates the TimedFaceUnit corresponding to this face unit
     * @param bmlId     BML block id
     * @param id         behaviour id
     * @return          the TFU
     */
    TimedFaceUnit createTFU(FeedbackManager bfm, BMLBlockPeg bbPeg,String bmlId,String id, PegBoard pb);    
    
    /**
     * Create a copy of this face unit and link it to the faceplayer
     */
    FaceUnit copy(FaceController fc, FACSConverter fconv, EmotionConverter econv); 
    
    void interruptFromHere();
}