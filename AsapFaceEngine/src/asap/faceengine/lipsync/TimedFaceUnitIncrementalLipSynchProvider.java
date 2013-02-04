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
            tfu.setSubUnit(true);
            tfu.setState(TimedPlanUnitState.LURKING);
            facePlanManager.addPlanUnit(tfu);
            tfuMap.put(identifier, tfu);        
            
        }
        
        //TODO: setup crude co-articulation mechanism from TimedFaceUnitLipSynchProvider
        TimedFaceUnit tfuPrevious = getPrevious(tfu, beh.getBmlId(), beh.id);
        
        tfu.getTimePeg("start").setGlobalValue(start);
        tfu.getTimePeg("end").setGlobalValue(start+(double)vis.getDuration()/1000d);                
    }
}
