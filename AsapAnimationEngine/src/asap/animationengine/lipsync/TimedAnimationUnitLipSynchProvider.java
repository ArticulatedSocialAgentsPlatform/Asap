/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.lipsync;

import hmi.tts.TTSTiming;
import hmi.tts.Visime;

import java.util.ArrayList;
import java.util.HashMap;

import lombok.extern.slf4j.Slf4j;
import saiba.bml.core.Behaviour;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.gesturebinding.SpeechBinding;
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.realizer.lipsync.LipSynchProvider;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.OffsetPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.PlanManager;
import asap.realizer.planunit.TimedPlanUnit;

/**
 * Creates TimedMotionUnit for lipsync
 * @author Herwin
 * 
 */
@Slf4j
public class TimedAnimationUnitLipSynchProvider implements LipSynchProvider
{
    private final SpeechBinding speechBinding;
    private final PegBoard pegBoard;
    private final AnimationPlayer animationPlayer;
    private final PlanManager<TimedAnimationUnit> animationPlanManager;

    public TimedAnimationUnitLipSynchProvider(SpeechBinding sb, AnimationPlayer ap, PlanManager<TimedAnimationUnit> animationPlanManager,
            PegBoard pegBoard)
    {
        speechBinding = sb;
        animationPlayer = ap;
        this.pegBoard = pegBoard;
        this.animationPlanManager = animationPlanManager;
    }

    @Override
    public void addLipSyncMovement(BMLBlockPeg bbPeg, Behaviour beh, TimedPlanUnit bs, TTSTiming timing)
    {
        ArrayList<TimedAnimationMotionUnit> tmus = new ArrayList<TimedAnimationMotionUnit>();
        double totalDuration = 0d;
        double prevDuration = 0d;
        HashMap<TimedAnimationMotionUnit, Double> startTimes = new HashMap<TimedAnimationMotionUnit, Double>();
        HashMap<TimedAnimationMotionUnit, Double> endTimes = new HashMap<TimedAnimationMotionUnit, Double>();

        TimedAnimationMotionUnit tmu = null;        

        for (Visime vis : timing.getVisimes())
        {
            // OOK: de visemen zijn nu te kort (sluiten aan op interpolatie 0/0
            // ipv 50/50)
            // make visemeunit, add to faceplanner...
            double start = totalDuration / 1000d - prevDuration / 2000;
            double peak = totalDuration / 1000d + vis.getDuration() / 2000d;
            double end = totalDuration / 1000d + vis.getDuration() / 1000d;
            if (tmu != null)
            {
                endTimes.put(tmu, peak); // extend previous tfu to the peak of this one!
            }
            try
            {
                tmu = speechBinding.getMotionUnit(vis.getNumber(), bbPeg, beh.getBmlId(), beh.id, animationPlayer, pegBoard);
                if (tmu == null)
                {
                    tmu = speechBinding.getMotionUnit(0, bbPeg, beh.getBmlId(), beh.id, animationPlayer, pegBoard);
                }
                startTimes.put(tmu, start);
                endTimes.put(tmu, end);
                tmus.add(tmu);
                tmu.resolveGestureKeyPositions();
                totalDuration += vis.getDuration();
                prevDuration = vis.getDuration();
            }
            catch (MUSetupException e)
            {
                log.warn("Exception planning first timedmotionunit for speechbehavior {}", e, beh);
            }
            log.debug("Viseme number {}", vis.getNumber());
        }

        for (TimedAnimationMotionUnit tm : tmus)
        {
            tm.setSubUnit(true);            
            animationPlanManager.addPlanUnit(tm);
        }

        // and now link viseme units to the speech timepeg!
        for (TimedAnimationMotionUnit plannedFU : tmus)
        {
            TimePeg startPeg = new OffsetPeg(bs.getTimePeg("start"), startTimes.get(plannedFU));

            plannedFU.setTimePeg("start", startPeg);
            TimePeg endPeg = new OffsetPeg(bs.getTimePeg("start"), endTimes.get(plannedFU));
            plannedFU.setTimePeg("end", endPeg);
            log.debug("adding jaw movement at {}-{}", plannedFU.getStartTime(), plannedFU.getEndTime());
        }
    }

}
