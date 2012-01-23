package asap.animationengine.procanimation;

import asap.animationengine.motionunit.TMUPlayException;
import asap.animationengine.motionunit.TimedMotionUnit;
import asap.planunit.Priority;
import hmi.elckerlyc.BMLBlockPeg;
import hmi.elckerlyc.TimedPlanUnitPlayException;
import hmi.elckerlyc.feedback.FeedbackManager;

/**
 * TimedMotionUnit for ProcAnimationGestureMU
 * @author Herwin
 *
 */
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
        super.startUnit(time);
    }
    
    @Override
    public void playUnit(double time) throws TMUPlayException
    {
        super.playUnit(time);
        setPriority(mu.getPriority());
    }
}
