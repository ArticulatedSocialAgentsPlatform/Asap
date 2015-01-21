/*******************************************************************************
 *******************************************************************************/
package asap.faceengine.lipsync;

import hmi.faceanimation.FaceController;
import hmi.tts.Visime;

import java.util.HashMap;
import java.util.Map;

import saiba.bml.core.Behaviour;
import asap.faceengine.faceunit.TimedFaceUnit;
import asap.faceengine.viseme.VisemeBinding;
import asap.realizer.lipsync.IncrementalLipSynchProvider;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
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
    private final PegBoard pegBoard;

    public TimedFaceUnitIncrementalLipSynchProvider(VisemeBinding visBinding, FaceController fc,
            PlanManager<TimedFaceUnit> facePlanManager, PegBoard pb)
    {
        visimeBinding = visBinding;
        faceController = fc;
        this.facePlanManager = facePlanManager;
        pegBoard = pb;
    }

    private TimedFaceUnit getPrevious(double start, TimedFaceUnit tfuCur)
    {
        TimedFaceUnit previous = null;
        for (TimedFaceUnit tfu : tfuToVisimeMap.keySet())
        {
            if (tfu.getStartTime() != TimePeg.VALUE_UNKNOWN && tfu.getStartTime() < start && tfu != tfuCur)
            {
                if (previous == null || tfu.getStartTime() > previous.getStartTime())
                {
                    previous = tfu;
                }
            }
        }
        return previous;
    }

    @Override
    public synchronized void setLipSyncUnit(BMLBlockPeg bbPeg, Behaviour beh, double start, Visime vis, Object identifier)
    {
        TimedFaceUnit tfu = tfuMap.get(identifier);
        if (tfu == null)
        {
            tfu = visimeBinding.getVisemeUnit(bbPeg, beh, vis.getNumber(), faceController, pegBoard);
            tfu.setTimePeg("start", new TimePeg(bbPeg));
            tfu.setTimePeg("end", new TimePeg(bbPeg));
            tfu.setSubUnit(true);
            facePlanManager.addPlanUnit(tfu);
            tfuMap.put(identifier, tfu);
        }
        TimedFaceUnit tfuPrevious = getPrevious(start, tfu);

        if (tfuPrevious != null)
        {
            Visime prevVis = tfuToVisimeMap.get(tfuPrevious);
            double prevDuration = (double) prevVis.getDuration() / 1000d;
            tfu.getTimePeg("start").setGlobalValue(start - prevDuration * 0.5);
            tfuPrevious.getTimePeg("end").setGlobalValue(start + (double) vis.getDuration() / 1000d * 0.5);
        }
        else
        {
            tfu.getTimePeg("start").setGlobalValue(start);
        }
        tfuToVisimeMap.put(tfu, vis);
        tfu.getTimePeg("end").setGlobalValue(start + (double) vis.getDuration() / 1000d);
        tfu.setState(TimedPlanUnitState.LURKING);
    }
}
