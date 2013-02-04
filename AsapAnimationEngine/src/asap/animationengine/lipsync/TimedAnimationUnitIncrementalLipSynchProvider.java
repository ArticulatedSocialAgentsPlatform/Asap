package asap.animationengine.lipsync;

import hmi.tts.Visime;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import saiba.bml.core.Behaviour;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.gesturebinding.SpeechBinding;
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.realizer.lipsync.IncrementalLipSynchProvider;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.PlanManager;
import asap.realizer.planunit.TimedPlanUnitState;

/**
 * Provides incremental lipsync using TimedAnimationMotionUnit.
 * @author hvanwelbergen
 *
 */
@Slf4j
public class TimedAnimationUnitIncrementalLipSynchProvider implements IncrementalLipSynchProvider
{
    private final SpeechBinding speechBinding;
    private final PegBoard pegBoard;
    private final AnimationPlayer animationPlayer;
    private final PlanManager<TimedAnimationUnit> animationPlanManager;
    private Map<Object, TimedAnimationMotionUnit> tmuMap = new HashMap<>();

    public TimedAnimationUnitIncrementalLipSynchProvider(SpeechBinding sb, AnimationPlayer ap,
            PlanManager<TimedAnimationUnit> animationPlanManager, PegBoard pegBoard)
    {
        speechBinding = sb;
        animationPlayer = ap;
        this.pegBoard = pegBoard;
        this.animationPlanManager = animationPlanManager;
    }

    @Override
    public void setLipSyncUnit(BMLBlockPeg bbPeg, Behaviour beh, double start, Visime vis, Object identifier)
    {
        TimedAnimationMotionUnit tmu = tmuMap.get(identifier);
        if (tmu == null)
        {
            try
            {
                tmu = speechBinding.getMotionUnit(vis.getNumber(), bbPeg, beh.getBmlId(), beh.id, animationPlayer, pegBoard);
                if (tmu == null)
                {
                    tmu = speechBinding.getMotionUnit(0, bbPeg, beh.getBmlId(), beh.id, animationPlayer, pegBoard);
                }                
            }
            catch (MUSetupException e)
            {
                log.warn("Exception planning timedmotionunit for speechbehavior {}", e, beh);
                return;
            }
            tmu.resolveGestureKeyPositions();
            tmu.setTimePeg("start", new TimePeg(bbPeg));
            tmu.setTimePeg("end", new TimePeg(bbPeg));
            tmu.setSubUnit(true);
            tmu.resolveGestureKeyPositions();
            tmu.setState(TimedPlanUnitState.LURKING);      
            animationPlanManager.addPlanUnit(tmu);
            tmuMap.put(identifier, tmu);
        }

        // TODO: setup crude co-articulation mechanism from TimedAnimationUnitLipSynchProvider

        tmu.getTimePeg("start").setGlobalValue(start);
        tmu.getTimePeg("end").setGlobalValue(start + (double) vis.getDuration() / 1000d);
    }
}
