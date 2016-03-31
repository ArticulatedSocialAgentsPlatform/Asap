/*******************************************************************************
 *******************************************************************************/
package asap.realizer.planunit;

import lombok.Delegate;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;

/**
 * Testing stub for TimedPlanUnits
 * @author hvanwelbergen
 * 
 */
public class StubTimedPlanUnit extends TimedAbstractPlanUnit
{
    private double prefDuration;
    public static final double READY_RELATIVE_TIME = 0.25;
    public static final double STROKE_RELATIVE_TIME = 0.5;
    public static final double RELAX_RELATIVE_TIME = 0.75;
    
    public StubTimedPlanUnit(FeedbackManager fbm, BMLBlockPeg bmlPeg, String bmlId, String behId, double pd)
    {
        super(fbm, bmlPeg, bmlId, behId);
        prefDuration = pd;

        KeyPositionManagerImpl kp = new KeyPositionManagerImpl();
        puTimeManager = new PlanUnitTimeManager(kp);
        kp.addKeyPosition(new KeyPosition("ready",READY_RELATIVE_TIME));
        kp.addKeyPosition(new KeyPosition("stroke",STROKE_RELATIVE_TIME));
        kp.addKeyPosition(new KeyPosition("relax",RELAX_RELATIVE_TIME));
        resolveGestureKeyPositions();        
    }

    @Delegate
    protected final PlanUnitTimeManager puTimeManager;

    @Override
    public double getPreferedDuration()
    {
        return prefDuration;
    }

    @Override
    protected void playUnit(double time) throws TimedPlanUnitPlayException
    {

    }

    @Override
    protected void stopUnit(double time) throws TimedPlanUnitPlayException
    {

    }
}
