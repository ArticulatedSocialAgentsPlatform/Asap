package asap.animationengine.motionunit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asap.animationengine.controller.ControllerMU;

import hmi.elckerlyc.BMLBlockPeg;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.pegboard.PegBoard;
import hmi.elckerlyc.planunit.TimedPlanUnitPlayException;

/**
 * Timed motion unit steering a Physical motion unit.
 * @author Herwin
 * 
 */
public class PhysicalTMU extends TimedMotionUnit
{
    private static Logger logger = LoggerFactory.getLogger(PhysicalTMU.class.getName());

    public PhysicalTMU(FeedbackManager bbm, BMLBlockPeg bbPeg, String bmlId, String id, MotionUnit m, PegBoard pb)
    {
        super(bbm, bbPeg, bmlId, id, m, pb);
    }

    @Override
    protected void startUnit(double t) throws TimedPlanUnitPlayException
    {
        ControllerMU pc = (ControllerMU) getMotionUnit();
        pc.reset();
        logger.debug("Resetting controller {}:{}", getBMLId(), getId());
    }
}
