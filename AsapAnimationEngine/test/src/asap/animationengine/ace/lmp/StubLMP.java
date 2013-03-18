package asap.animationengine.ace.lmp;

import java.util.Set;

import lombok.Setter;

import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.TimedPlanUnitPlayException;

/**
 * LMP testing stub
 * @author hvanwelbergen
 * 
 */
public class StubLMP extends LMP
{
    private final Set<String> kinematicJoints;
    private final Set<String> physicalJoints;

    @Setter
    private double prepDuration;

    @Setter
    private double retrDuration;
    
    @Setter
    private double strokeDuration;
    
    private final boolean hasFixedStrokeDuration;

    public StubLMP(FeedbackManager fbm, BMLBlockPeg bmlPeg, String bmlId, String behId, PegBoard pegBoard, Set<String> kinematicJoints,
            Set<String> physicalJoints, double prepDuration, double retrDuration, double strokeDuration)
    {
        this(fbm, bmlPeg, bmlId, behId, pegBoard, kinematicJoints, physicalJoints, prepDuration, retrDuration, strokeDuration, false);
    }

    public StubLMP(FeedbackManager fbm, BMLBlockPeg bmlPeg, String bmlId, String behId, PegBoard pegBoard, Set<String> kinematicJoints,
            Set<String> physicalJoints, double prepDuration, double retrDuration, double strokeDuration, boolean hasFixedStrokeDuration)
    {
        super(fbm, bmlPeg, bmlId, behId, pegBoard);
        this.kinematicJoints = kinematicJoints;
        this.physicalJoints = physicalJoints;
        this.prepDuration = prepDuration;
        this.retrDuration = retrDuration;
        this.strokeDuration = strokeDuration;
        this.hasFixedStrokeDuration = hasFixedStrokeDuration;
    }

    @Override
    public boolean hasFixedStrokeDuration()
    {
        return hasFixedStrokeDuration;
    }

    @Override
    public Set<String> getKinematicJoints()
    {
        return kinematicJoints;
    }

    @Override
    public Set<String> getPhysicalJoints()
    {
        return physicalJoints;
    }

    @Override
    public double getPreparationDuration()
    {
        return prepDuration;
    }

    @Override
    public double getRetractionDuration()
    {
        return retrDuration;
    }

    @Override
    public double getStrokeDuration()
    {
        return strokeDuration;
    }

    @Override
    public boolean hasValidTiming()
    {
        return true;
    }

    @Override
    protected void setInternalStrokeTiming(double time)
    {

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
