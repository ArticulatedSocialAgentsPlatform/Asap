package asap.faceengine.lipsync;

import hmi.faceanimation.FaceController;
import hmi.tts.Visime;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import saiba.bml.core.Behaviour;
import asap.faceengine.faceunit.TimedFaceUnit;
import asap.faceengine.viseme.VisemeBinding;
import asap.realizer.lipsync.IncrementalLipSynchProvider;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.PlanManager;
import asap.realizer.planunit.TimedPlanUnitState;

/**
 * Provides incremental lipsync using TimedFaceUnits.
 * @author hvanwelbergen
 * 
 */
public class TimedFaceUnitIncrementalLipSynchProvider implements IncrementalLipSynchProvider
{
    private final VisemeBinding visimeBinding;
    private final FaceController faceController;
    private final PlanManager<TimedFaceUnit> facePlanManager;

    private Map<Object, TimedFaceUnit> tfuMap = new HashMap<>();
    private Map<TimedFaceUnit, Visime> tfuToVisimeMap = new HashMap<>();

    public TimedFaceUnitIncrementalLipSynchProvider(VisemeBinding visBinding, FaceController fc, PlanManager<TimedFaceUnit> facePlanManager)
    {
        visimeBinding = visBinding;
        faceController = fc;
        this.facePlanManager = facePlanManager;
    }

    private TimedFaceUnit getPrevious(String bmlId, String id, double startTime)
    {
        Collection<TimedFaceUnit> tfus = facePlanManager.getPlanUnits(bmlId, id);
        TimedFaceUnit tfuPrevious = null;
        for (TimedFaceUnit tfu : tfus)
        {
            if (tfu.getStartTime() < startTime  && tfu.getStartTime() != TimePeg.VALUE_UNKNOWN)
            {
                if (tfuPrevious == null || tfu.getStartTime() > tfuPrevious.getStartTime())
                {
                    tfuPrevious = tfu;
                }
            }
        }
        return tfuPrevious;
    }

    @Override
    public void setLipSyncUnit(BMLBlockPeg bbPeg, Behaviour beh, double start, Visime vis, Object identifier)
    {
        TimedFaceUnit tfu = tfuMap.get(identifier);
        if (tfu == null)
        {
            tfu = visimeBinding.getVisemeUnit(bbPeg, beh, vis.getNumber(), faceController);
            tfu.setTimePeg("start", new TimePeg(bbPeg));
            tfu.setTimePeg("end", new TimePeg(bbPeg));
            tfu.setSubUnit(true);
            tfu.setState(TimedPlanUnitState.LURKING);
            facePlanManager.addPlanUnit(tfu);
            tfuMap.put(identifier, tfu);            
        }
        TimedFaceUnit tfuPrevious = getPrevious(beh.getBmlId(), beh.id, start);
        
        if (tfuPrevious != null)
        {
            Visime prevVis = tfuToVisimeMap.get(tfuPrevious);
            double prevDuration = (double)prevVis.getDuration()/1000d;
            tfu.getTimePeg("start").setGlobalValue(start - prevDuration * 0.5);
            tfuPrevious.getTimePeg("end").setGlobalValue(start + (double)vis.getDuration()/1000d * 0.5);
        }
        else
        {
            tfu.getTimePeg("start").setGlobalValue(start);
        }
        tfuToVisimeMap.put(tfu,vis);
        tfu.getTimePeg("end").setGlobalValue(start + (double) vis.getDuration() / 1000d);
    }
}
