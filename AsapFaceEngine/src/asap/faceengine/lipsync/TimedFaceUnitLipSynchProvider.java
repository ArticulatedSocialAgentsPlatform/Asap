package asap.faceengine.lipsync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import hmi.bml.core.Behaviour;
import hmi.elckerlyc.lipsync.LipSynchProvider;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.pegboard.OffsetPeg;
import hmi.elckerlyc.pegboard.TimePeg;
import hmi.elckerlyc.planunit.PlanManager;
import hmi.elckerlyc.planunit.TimedPlanUnit;
import hmi.faceanimation.FaceController;
import asap.faceengine.faceunit.TimedFaceUnit;
import asap.faceengine.viseme.DisneyVisemes;
import asap.faceengine.viseme.VisemeBinding;
import hmi.tts.Visime;

/**
 * Creates TimedFaceUnits for lipsync 
 * @author Herwin
 *
 */
@Slf4j
public class TimedFaceUnitLipSynchProvider implements LipSynchProvider
{
    private final VisemeBinding visimeBinding;
    private final FaceController faceController;
    private final PlanManager<TimedFaceUnit>facePlanManager;
    
    public TimedFaceUnitLipSynchProvider(VisemeBinding visBinding, FaceController fc, PlanManager<TimedFaceUnit>facePlanManager)
    {
        visimeBinding = visBinding;
        faceController = fc;
        this.facePlanManager= facePlanManager; 
    }
    
    @Override
    public void addLipSyncMovement(BMLBlockPeg bbPeg, Behaviour beh, TimedPlanUnit bs, List<Visime> visemes)
    {
        ArrayList<TimedFaceUnit> tfus = new ArrayList<TimedFaceUnit>();
        double totalDuration = 0d;
        double prevDuration = 0d;

        // add null viseme before
        TimedFaceUnit tfu = visimeBinding.getVisemeUnit(bbPeg, beh, DisneyVisemes.V_NULL, faceController);
        tfu.setSubUnit(true);
        HashMap<TimedFaceUnit, Double> startTimes = new HashMap<TimedFaceUnit, Double>();
        HashMap<TimedFaceUnit, Double> endTimes = new HashMap<TimedFaceUnit, Double>();
        startTimes.put(tfu, Double.valueOf(0d));
        endTimes.put(tfu, Double.valueOf(0d));

        for (Visime vis : visemes)
        {
            // OOK: de visemen zijn nu te kort (sluiten aan op interpolatie 0/0
            // ipv 50/50)
            // make visemeunit, add to faceplanner...
            double start = totalDuration / 1000d - prevDuration / 2000;
            double peak = totalDuration / 1000d + vis.getDuration() / 2000d;
            double end = totalDuration / 1000d + vis.getDuration() / 1000d;
            endTimes.put(tfu, peak); // extend previous tfu to the peak of this
                                     // one!
            tfu = visimeBinding.getVisemeUnit(bbPeg, beh, vis.getNumber(), faceController);

            startTimes.put(tfu, start);
            endTimes.put(tfu, end);
            tfus.add(tfu);
            totalDuration += vis.getDuration();
            prevDuration = vis.getDuration();
        }
        // add null viseme at end
        tfu = visimeBinding.getVisemeUnit(bbPeg, beh, DisneyVisemes.V_NULL, faceController);
        tfus.add(tfu);
        startTimes.put(tfu, Double.valueOf(totalDuration / 1000d));
        endTimes.put(tfu, Double.valueOf(totalDuration / 1000d));

        for (TimedFaceUnit vfu : tfus)
        {
            vfu.setSubUnit(true);
            facePlanManager.addPlanUnit(vfu);
        }

        // and now link viseme units to the speech timepeg!
        for (TimedFaceUnit plannedFU : tfus)
        {
            TimePeg startPeg = new OffsetPeg(bs.getTimePeg("start"), startTimes.get(plannedFU));

            plannedFU.setTimePeg("start", startPeg);
            TimePeg endPeg = new OffsetPeg(bs.getTimePeg("start"), endTimes.get(plannedFU));
            plannedFU.setTimePeg("end", endPeg);
            log.debug("adding face movement at {}-{}", plannedFU.getStartTime(), plannedFU.getEndTime());
        }        
    }
}
