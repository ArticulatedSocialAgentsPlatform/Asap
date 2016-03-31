/*******************************************************************************
 *******************************************************************************/
package asap.faceengine.viseme;

import hmi.faceanimation.FaceController;
import saiba.bml.core.Behaviour;
import asap.faceengine.faceunit.TimedFaceUnit;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;

/**
 * The VisemeBinding maps from visemes to FaceUnits. different avatars have
 * really different VisemeBindings, because some avatars only support morphing,
 * or other only FAPs, etc....
 * 
 * @author Dennis Reidsma
 */
public interface VisemeBinding
{
    /**
     * Get a viseme unit for viseme viseme. If the viseme is not found, an 'empty' TimedFaceUnit is returned.
     * 
     * note: each viseme has attackPeak=relax=peak, and start=prev.peak and
     * end=next.peak for timing. Ugly but effective.<br>
     * 
     */
    TimedFaceUnit getVisemeUnit(FeedbackManager bfm,BMLBlockPeg bbPeg, Behaviour b, int viseme,
            FaceController fc, PegBoard pb);
    
    /**
     * Get a visime unit that is not hooked up to the feedbackmanager
     * If the viseme is not found, an 'empty' TimedFaceUnit is returned.
     * 
     * note: each viseme has attackPeak=relax=peak, and start=prev.peak and
     * end=next.peak for timing. Ugly but effective.<br>
     */
    TimedFaceUnit getVisemeUnit(BMLBlockPeg bbPeg, Behaviour b, int viseme,            
            FaceController fc, PegBoard pb);
}
