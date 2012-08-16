package asap.animationengine.ace.lmp;

import hmi.math.Mat4f;
import lombok.Delegate;
import asap.animationengine.ace.GuidingSequence;
import asap.animationengine.motionunit.AnimationUnit;
import asap.realizer.planunit.KeyPositionManager;
import asap.realizer.planunit.KeyPositionManagerImpl;

/**
 * Base class of all local motor programs for positioning in Cartesian space.
 * @author hvanwelbergen
 * @author Stefan Kopp (original C++ version)
 */
public abstract class LMPPos implements AnimationUnit
{
    @Delegate(types = KeyPositionManager.class)
    private KeyPositionManager keyPositionManager = new KeyPositionManagerImpl();

    private GuidingSequence gSeq;
    private float baseFrame[] = Mat4f.getMat4f();

    public LMPPos()
    {
        Mat4f.setIdentity(baseFrame);
    }

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
