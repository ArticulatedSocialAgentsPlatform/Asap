/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.noise;

import saiba.bml.feedback.BMLSyncPointProgressFeedback;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.motionunit.MUPlayException;
import asap.motionunit.TMUPlayException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.KeyPosition;
import asap.realizer.planunit.TimedPlanUnitPlayException;

/**
 * Timed motion unit for noise motion units.
 * Specific to noise motion units is that they run in line with "real time". They call the play function of noise motion units
 * with a value t = globaltime - starttime(MU)
 * 
 * @author Dennis Reidsma
 * 
 */
public class NoiseTMU extends TimedAnimationMotionUnit
{
    private NoiseMU nmu;

    // double startTime = 0;

    public NoiseTMU(FeedbackManager bfm, BMLBlockPeg bmlBlockPeg, String bmlId, String id, NoiseMU nmu, PegBoard pb, AnimationPlayer aniPlayer)
    {
        super(bfm, bmlBlockPeg, bmlId, id, nmu, pb, aniPlayer);
        this.nmu = nmu;
    }

    @Override
    public void startUnit(double time) throws TimedPlanUnitPlayException
    {
        // startTime = time;
        sendProgress(0d, time);
    }

    @Override
    public void playUnit(double time) throws TMUPlayException
    {
        try
        {
            // logger.debug("Timed Motion Unit play {}",time);
            nmu.play(time);
        }
        catch (MUPlayException ex)
        {
            throw new TMUPlayException(ex.getLocalizedMessage(), this, ex);
        }
    }

    @Override
    public void stopUnit(double time)
    {
        sendProgress(1d, time);
    }

    /**
     * Send progress feedback for all key positions passed at canonical time t.
     * 
     * @param t canonical time 0 &lt= t &lt=1
     * @param time time since start of BML execution
     */
    protected void sendProgress(double t, double time)
    {
        for (KeyPosition k : nmu.getKeyPositions())
        {
            if (k.time <= t)
            {
                if (!progressHandled.contains(k))
                {
                    String bmlId = getBMLId();
                    String behaviorId = getId();
                    String syncId = k.id;
                    double bmlBlockTime = time - bmlBlockPeg.getValue();
                    feedback(new BMLSyncPointProgressFeedback(bmlId, behaviorId, syncId, bmlBlockTime, time));
                    progressHandled.add(k);
                }
            }
        }
    }

}
