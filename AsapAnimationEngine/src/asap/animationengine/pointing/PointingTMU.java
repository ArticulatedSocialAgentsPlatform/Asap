/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.pointing;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asap.animationengine.AnimationPlayer;
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
 * A timed motionunit for pointing
 * @author hvanwelbergen
 */
public class PointingTMU extends TimedAnimationMotionUnit
{
    private PointingMU pmu;
    private TimedAnimationUnit relaxUnit;
    private volatile boolean handleGracefulInterrupt = false;

    private static Logger logger = LoggerFactory.getLogger(PointingTMU.class.getName());
    
    public PointingTMU(FeedbackManager bfm, BMLBlockPeg bbPeg, String bmlId, String id, PointingMU mu, PegBoard pb, AnimationPlayer aniPlayer)
    {
        super(bfm, bbPeg, bmlId, id, mu, pb, aniPlayer);
        pmu = mu;
    }

    @Override
    public void startUnit(double time) throws TimedPlanUnitPlayException
    {
        try
        {
            double readyTime = getTime("ready");
            double relaxTime = getTime("relax");
            double readyDuration;
            double relaxDuration;
            if (readyTime != TimePeg.VALUE_UNKNOWN)
            {
                readyDuration = readyTime - getStartTime();
            }
            else
            {
                // no ready peg, create one
                // TODO: determine readyDuration with Fitts' law
                readyDuration = 1;
                double afterReady = getNextPegTime("ready");
                logger.debug("after ready: {}", afterReady);
                if (afterReady != TimePeg.VALUE_UNKNOWN)
                {
                    double preparationDur = afterReady - getPrevPegTime("ready");
                    logger.debug("preparationDur {}", preparationDur);
                    if (readyDuration > preparationDur * 0.5)
                    {
                        readyDuration = preparationDur * 0.5;
                    }
                }
                TimePeg startPeg = getTimePeg("start");
                if (startPeg == null)
                {
                    throw new TMUPlayException("Start peg of pointing tmu does not exist", this);
                }
                else
                {
                    OffsetPeg tpReady = new OffsetPeg(startPeg, readyDuration);
                    setTimePeg("ready", tpReady);
                }
            }

            if (relaxTime == TimePeg.VALUE_UNKNOWN)// insert relax time peg
            {
                relaxDuration = readyDuration;
                double retractionDur = getNextPegTime("relax") - getPrevPegTime("relax");
                logger.debug("retractionDur: {}= {} - {}", new Object[] { retractionDur, getNextPegTime("relax"), getPrevPegTime("relax") });
                if (relaxDuration > retractionDur)
                {
                    relaxDuration = retractionDur;
                }
                TimePeg endPeg = getTimePeg("end");
                OffsetPeg tpRelax;

                if (getEndTime() != TimePeg.VALUE_UNKNOWN)
                {
                    // only set relax if end is set, otherwise persistent point
                    tpRelax = new OffsetPeg(endPeg, -relaxDuration);
                    setTimePeg("relax", tpRelax);
                }

            }
            pmu.setStartPose(readyDuration);
        }
        catch (MUPlayException ex)
        {
            throw new TMUPlayException(ex.getLocalizedMessage(), this, ex);
        }
    }

    @Override
    protected void relaxUnit(double time) throws TimedPlanUnitPlayException
    {
        Set<String> usedJoints = new HashSet<String>();
        usedJoints.addAll(getKinematicJoints());
        usedJoints.addAll(getPhysicalJoints());
        double retractionDuration = pmu.getPlayer().getTransitionToRestDuration(usedJoints);

        TimePeg relaxPeg = getTimePeg("relax");
        TimePeg endPeg = getTimePeg("end");
        if (pegBoard.getPegKeys(endPeg).size() == 1 && !endPeg.isAbsoluteTime())
        {
            endPeg.setGlobalValue(relaxPeg.getGlobalValue() + retractionDuration);
        }
        relaxUnit = pmu.getPlayer().createTransitionToRest(NullFeedbackManager.getInstance(), usedJoints, relaxPeg, endPeg, getBMLId(),
                getId(), bmlBlockPeg, pegBoard);
        relaxUnit.start(time);
        super.relaxUnit(time);
    }

    @Override
    public void playUnit(double time) throws TimedPlanUnitPlayException
    {
        if (handleGracefulInterrupt)
        {
            skipPegs(time, "ready", "strokeStart", "stroke", "strokeEnd");

            // XXX: should relax and end pegs also be detached if other behaviors are connected to them?
            getTimePeg("relax").setGlobalValue(time);
            getTimePeg("end").setGlobalValue(time + pmu.getRelaxDuration());
            handleGracefulInterrupt = false;
            return;
        }
        
        if (time > getRelaxTime() && getRelaxTime() != TimePeg.VALUE_UNKNOWN)
        {
            try
            {
                relaxUnit.play(time);
            }
            catch (TimedPlanUnitPlayException e)
            {
                throw new TMUPlayException("Error relaxing pointing", this, e);
            }
        }
        else
        {
            super.playUnit(time);
        }
    }

    @Override
    protected void gracefullInterrupt(double time) throws TimedPlanUnitPlayException
    {
        handleGracefulInterrupt = true; //handle it in the play thread
    }
}
