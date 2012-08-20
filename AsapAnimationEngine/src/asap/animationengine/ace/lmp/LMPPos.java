package asap.animationengine.ace.lmp;

import hmi.math.Mat4f;
import asap.animationengine.ace.GuidingSequence;
import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.TimedAbstractPlanUnit;

/**
 * Base class of all local motor programs for positioning in Cartesian space.
 * @author hvanwelbergen
 * @author Stefan Kopp (original C++ version)
 */
public abstract class LMPPos extends TimedAbstractPlanUnit implements TimedAnimationUnit
{
    public LMPPos(FeedbackManager bbf, BMLBlockPeg bmlBlockPeg, String bmlId, String id)
    {
        super(bbf, bmlBlockPeg, bmlId, id, true);
        Mat4f.setIdentity(baseFrame);
    }
    protected GuidingSequence gSeq;
    private float baseFrame[] = Mat4f.getMat4f();

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
