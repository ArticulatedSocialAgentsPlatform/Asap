/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.gaze;

import saiba.bml.BMLGestureSync;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.TMUSetupException;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.motionunit.MUPlayException;
import asap.motionunit.TMUPlayException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.feedback.NullFeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.OffsetPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedPlanUnitPlayException;

/**
 * Timed motion unit for gaze, makes sure the gaze start pose is set at start.
 * @author Herwin
 * 
 */
public class GazeTMU extends TimedAnimationMotionUnit
{
    private GazeMU gmu;
    private TimedAnimationUnit relaxUnit;
    private final AnimationPlayer aniPlayer;

    public GazeTMU(FeedbackManager bfm, BMLBlockPeg bmlBlockPeg, String bmlId, String id, GazeMU mu, PegBoard pb, AnimationPlayer aniPlayer)
    {
        super(bfm, bmlBlockPeg, bmlId, id, mu, pb, aniPlayer);
        this.aniPlayer = aniPlayer;
        gmu = mu;
    }

    @Override
    protected void startUnit(double time) throws TimedPlanUnitPlayException
    {
        try
        {
            gmu.setStartPose();
        }
        catch (MUPlayException ex)
        {
            throw new TMUPlayException(ex.getLocalizedMessage(), this, ex);
        }

        double readyTime = getTime("ready");
        double relaxTime = getTime("relax");

        double readyDuration;
        double relaxDuration;

        // Setting ready time
        if (readyTime != TimePeg.VALUE_UNKNOWN)
        {
            readyDuration = readyTime - getStartTime();
        }
        else
        {
            readyDuration = gmu.getPreferedReadyDuration();

            if (getEndTime() != TimePeg.VALUE_UNKNOWN && getStartTime() != TimePeg.VALUE_UNKNOWN)
            {
                double duration = getEndTime() - getStartTime();
                if (duration <= readyDuration * 2)
                {
                    readyDuration = duration * 0.5;
                }
            }

            TimePeg startPeg = getTimePeg(BMLGestureSync.START.getId());
            if (startPeg == null)
            {
                throw new TimedPlanUnitPlayException("Start peg is null", this);
            }
            OffsetPeg tpReady = new OffsetPeg(startPeg, readyDuration);
            setTimePeg("ready", tpReady);
        }

        // setting relax
        TimePeg endPeg = getTimePeg("end");
        if (relaxTime != TimePeg.VALUE_UNKNOWN)
        {
            relaxDuration = readyTime - getStartTime();
        }
        else
        {
            relaxDuration = readyDuration;
            TimePeg tpRelax = new TimePeg(getBMLBlockPeg());
            setTimePeg("relax", tpRelax);
            if (endPeg != null && endPeg.getGlobalValue() != TimePeg.VALUE_UNKNOWN)
            {
                tpRelax.setGlobalValue(getEndTime()-relaxDuration);
            }
            else
            {
                tpRelax.setGlobalValue(getTime("ready")+gmu.getPreferedStayDuration());
            }
        }

        // set end        
        if (endPeg == null)
        {
            endPeg = new TimePeg(getBMLBlockPeg());
            setTimePeg("end", endPeg);
        }
        if (getEndTime() == TimePeg.VALUE_UNKNOWN)
        {
            endPeg.setGlobalValue(getTime("relax")+relaxDuration);
        }

        gmu.setDurations(readyDuration, relaxDuration);
        super.startUnit(time);
    }

    @Override
    protected void playUnit(double time) throws TimedPlanUnitPlayException
    {
        if (time > getRelaxTime() && !isSubUnit())
        {
            relaxUnit.play(time);
        }
        else
        {
            super.playUnit(time);
        }
    }

    @Override
    protected void relaxUnit(double time) throws TimedPlanUnitPlayException
    {
        if (isSubUnit()) return;
        TimePeg relaxPeg = getTimePeg("relax");
        TimePeg endPeg = getTimePeg("end");
        double retractionDuration = aniPlayer.getGazeTransitionToRestDuration();
        if (pegBoard.getPegKeys(endPeg).size() == 1 && !endPeg.isAbsoluteTime())
        {
            endPeg.setGlobalValue(relaxPeg.getGlobalValue() + retractionDuration);
        }
        try
        {
            relaxUnit = aniPlayer.getRestGaze().createTransitionToRest(NullFeedbackManager.getInstance(), relaxPeg, endPeg, getBMLId(),
                    getId(), bmlBlockPeg, pegBoard);
        }
        catch (TMUSetupException e)
        {
            throw new TimedPlanUnitPlayException("TMUSetupException in construction of relax unit", this, e);
        }
        relaxUnit.setTimePeg("relax", new OffsetPeg(endPeg, 1));
        relaxUnit.setTimePeg("end", new OffsetPeg(endPeg, 2));
        relaxUnit.setSubUnit(true);
        relaxUnit.start(time);
        super.relaxUnit(time);
    }

    protected void gracefullInterrupt(double time) throws TimedPlanUnitPlayException
    {

        skipPegs(time, "ready", "strokeStart", "stroke", "strokeEnd");

        // XXX: should relax and end pegs also be detached if other behaviors are connected to them?
        getTimePeg("relax").setGlobalValue(time);
        getTimePeg("end").setGlobalValue(time + aniPlayer.getGazeTransitionToRestDuration());
    }
}
