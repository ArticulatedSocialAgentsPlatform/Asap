package asap.animationengine.procanimation;

import lombok.extern.slf4j.Slf4j;
import asap.animationengine.motionunit.TMUPlayException;
import asap.animationengine.motionunit.TimedMotionUnit;
import asap.planunit.Priority;
import hmi.bml.BMLGestureSync;
import hmi.elckerlyc.BMLBlockPeg;
import hmi.elckerlyc.OffsetPeg;
import hmi.elckerlyc.SyncPointNotFoundException;
import hmi.elckerlyc.TimePeg;
import hmi.elckerlyc.TimedPlanUnitPlayException;
import hmi.elckerlyc.feedback.FeedbackManager;

/**
 * TimedMotionUnit for ProcAnimationGestureMU
 * @author Herwin
 *
 */
@Slf4j
public class ProcAnimationGestureTMU extends TimedMotionUnit
{
    private final ProcAnimationGestureMU mu;
    public ProcAnimationGestureTMU(FeedbackManager bbf, BMLBlockPeg bmlBlockPeg, String bmlId, String id, ProcAnimationGestureMU m)
    {
        super(bbf, bmlBlockPeg, bmlId, id, m);
        setPriority(Priority.GESTURE);
        mu = m;        
    }
    
    @Override
    public void startUnit(double time) throws TimedPlanUnitPlayException
    {
        mu.setupTransitionUnits();
        TimePeg relaxPeg = getTimePeg(BMLGestureSync.RELAX.getId());
        if(relaxPeg==null)
        {
            double readyTime;
            try
            {
                readyTime = puTimeManager.getRelativeTime("relax");
            }
            catch (SyncPointNotFoundException e)
            {
                throw new TimedPlanUnitPlayException("No ready keyposition defined, cannot set relax timepeg",this,e);
            }
            
            OffsetPeg tpRelax = new OffsetPeg(this.getTimePeg("start"),readyTime);            
            setTimePeg("relax", tpRelax);
        }
        super.startUnit(time);
    }
    
    @Override
    public void stopUnit(double time)
    {
        super.stopUnit(time);
        log.debug("Tmu:{}:{} time={} relax={} stop={}",new Object[]{getBMLId(),getId(), time, getRelaxTime(), getEndTime()});
        if(time >= getRelaxTime() && time < getEndTime())
        {
            sendFeedback("end", time);
        }        
    }
    
    @Override
    public void playUnit(double time) throws TMUPlayException
    {
        super.playUnit(time);
        setPriority(mu.getPriority());
    }
}
