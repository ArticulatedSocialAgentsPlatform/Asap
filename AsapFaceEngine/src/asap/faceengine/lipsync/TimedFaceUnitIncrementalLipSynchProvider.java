package asap.faceengine.lipsync;

import hmi.faceanimation.FaceController;
import hmi.tts.Visime;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import saiba.bml.core.Behaviour;
import asap.faceengine.faceunit.TimedFaceUnit;
import asap.faceengine.viseme.VisemeBinding;
import asap.realizer.lipsync.IncrementalLipsyncProvider;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.PlanManager;

/**
 * Provides incremental lipsync using TimedFaceUnits.
 * @author hvanwelbergen
 *
 */
public class TimedFaceUnitIncrementalLipSynchProvider implements IncrementalLipsyncProvider 
{
    private final VisemeBinding visimeBinding;
    private final FaceController faceController;
    private final PlanManager<TimedFaceUnit>facePlanManager;
    
    private Map<Object, TimedFaceUnit> tfuMap = new HashMap<>();
    public TimedFaceUnitIncrementalLipSynchProvider(VisemeBinding visBinding, FaceController fc, PlanManager<TimedFaceUnit>facePlanManager)
    {
        visimeBinding = visBinding;
        faceController = fc;
        this.facePlanManager= facePlanManager; 
    }
    
    private TimedFaceUnit getPrevious(TimedFaceUnit tfuCurr, String bmlId, String id)
    {
        Collection<TimedFaceUnit> tfus = facePlanManager.getPlanUnits(bmlId,id);
        TimedFaceUnit tfuPrevious = null;
        for(TimedFaceUnit tfu:tfus)
        {
            if(tfu.getStartTime()<tfuCurr.getStartTime())
            {
                if(tfuPrevious==null || tfu.getStartTime()>tfuPrevious.getStartTime())
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
        if(tfu==null)
        {
            tfu = visimeBinding.getVisemeUnit(bbPeg, beh, vis.getNumber(), faceController);
            tfu.setTimePeg("start", new TimePeg(bbPeg));
            tfu.setTimePeg("end", new TimePeg(bbPeg));
            tfuMap.put(identifier, tfu);
            tfu.setSubUnit(true);
            facePlanManager.addPlanUnit(tfu);
        }
        TimedFaceUnit tfuPrevious = getPrevious(tfu, beh.getBmlId(), beh.id);
        
        tfu.getTimePeg("start").setGlobalValue(start);
        tfu.getTimePeg("end").setGlobalValue(start+vis.getDuration());        
    }
}
