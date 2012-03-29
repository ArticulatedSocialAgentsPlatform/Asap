package asap.animationengine.lipsync;

import hmi.bml.core.Behaviour;
import hmi.elckerlyc.lipsync.LipSynchProvider;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.pegboard.OffsetPeg;
import hmi.elckerlyc.pegboard.PegBoard;
import hmi.elckerlyc.pegboard.TimePeg;
import hmi.elckerlyc.planunit.PlanManager;
import hmi.elckerlyc.planunit.TimedPlanUnit;
import hmi.tts.Visime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.gesturebinding.SpeechBinding;
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.motionunit.TimedAnimationUnit;

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
    public void addLipSyncMovement(BMLBlockPeg bbPeg, Behaviour beh, TimedPlanUnit bs, List<Visime> visemes)
    {
        ArrayList<TimedAnimationUnit> tmus = new ArrayList<TimedAnimationUnit>();
        double totalDuration = 0d;
        double prevDuration = 0d;
        HashMap<TimedAnimationUnit, Double> startTimes = new HashMap<TimedAnimationUnit, Double>();
        HashMap<TimedAnimationUnit, Double> endTimes = new HashMap<TimedAnimationUnit, Double>();

        TimedAnimationUnit tmu = null;
        try
        {
            tmu = speechBinding.getMotionUnit(0, bbPeg, beh.getBmlId(), beh.id, animationPlayer, pegBoard);
            tmu.resolveDefaultBMLKeyPositions();
            startTimes.put(tmu, Double.valueOf(0d));
            endTimes.put(tmu, Double.valueOf(0d));
            tmus.add(tmu);
        }
        catch (MUSetupException e)
        {
            log.warn("Exception planning first timedmotionunit for speechbehavior {}", e, beh);
        }

        for (Visime vis : visemes)
        {
            // OOK: de visemen zijn nu te kort (sluiten aan op interpolatie 0/0
            // ipv 50/50)
            // make visemeunit, add to faceplanner...
            double start = totalDuration / 1000d - prevDuration / 2000;
            double peak = totalDuration / 1000d + vis.getDuration() / 2000d;
            double end = totalDuration / 1000d + vis.getDuration() / 1000d;
            if (tmu != null) endTimes.put(tmu, peak); // extend previous tfu to the peak of this one!
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
                tmu.resolveDefaultBMLKeyPositions();
                totalDuration += vis.getDuration();
                prevDuration = vis.getDuration();
            }
            catch (MUSetupException e)
            {
                log.warn("Exception planning first timedmotionunit for speechbehavior {}", e, beh);
            }
            log.debug("Viseme number {}", vis.getNumber());
        }

        // add null viseme at end
        try
        {
            tmu = speechBinding.getMotionUnit(0, bbPeg, beh.getBmlId(), beh.id, animationPlayer, pegBoard);
            tmu.resolveDefaultBMLKeyPositions();
            tmus.add(tmu);

            startTimes.put(tmu, Double.valueOf(totalDuration / 1000d));
            endTimes.put(tmu, Double.valueOf(totalDuration / 1000d));

        }
        catch (MUSetupException e)
        {
            log.warn("Exception planning last timedmotionunit for speechbehavior {}", e, beh);
        }

        // animationPlayer.addVisemesForSpeechUnit(tmus);
        for (TimedAnimationUnit tm : tmus)
        {
            tm.setSubUnit(true);
            animationPlanManager.addPlanUnit(tm);
        }

        // and now link viseme units to the speech timepeg!
        for (TimedAnimationUnit plannedFU : tmus)
        {
            TimePeg startPeg = new OffsetPeg(bs.getTimePeg("start"), startTimes.get(plannedFU));

            plannedFU.setTimePeg(plannedFU.getKeyPosition("start"), startPeg);
            TimePeg endPeg = new OffsetPeg(bs.getTimePeg("start"), endTimes.get(plannedFU));
            plannedFU.setTimePeg(plannedFU.getKeyPosition("end"), endPeg);
            log.debug("adding jaw movement at {}-{}", plannedFU.getStartTime(), plannedFU.getEndTime());
        }
    }

}
