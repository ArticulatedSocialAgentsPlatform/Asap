/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.ace.lmp;

import asap.animationengine.ace.GuidingSequence;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;

/**
 * Base class of all local motor programs for positioning in Cartesian space.
 * @author hvanwelbergen
 * @author Stefan Kopp (original C++ version)
 */
public abstract class LMPPos extends LMP
{
    public LMPPos(FeedbackManager bbf, BMLBlockPeg bmlBlockPeg, String bmlId, String id, PegBoard pb)
    {
        super(bbf, bmlBlockPeg, bmlId, id, pb);        
    }
    protected GuidingSequence gSeq;    

    public void setGuidingSeq(GuidingSequence gSeq)
    {
        /*
         * if (gSeq!=0) delete gSeq;
         * gSeq = seq.clone();
         * 
         * if ( gSeq!=0 && !gSeq->empty() )
         * {
         * //setStartTime(gstrokes.front().sT);
         * setEndTime( gSeq->getEndTime() );
         * 
         * // transform guiding strokes into local frame of reference
         * gSeq->transform( baseFrame );
         * }
         * else
         * cerr << "LMP_Pos::setGuidingSeq : empty trajectory!!" << endl;
         */
        this.gSeq = gSeq;
    }
    
   
}
