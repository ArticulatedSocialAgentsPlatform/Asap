package asap.asaplivemocapengine.planunit;

import lombok.extern.slf4j.Slf4j;
import asap.asaplivemocapengine.inputs.EulerInput;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedAbstractPlanUnit;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.utils.EulerHeadEmbodiment;

/**
 * Uses an EulerInput to remotely control a head embodiment
 * @author welberge
 */
@Slf4j
public class RemoteHeadTMU extends TimedAbstractPlanUnit
{
    private final EulerHeadEmbodiment headEmbodiment;
    private final EulerInput headInput;
    private TimePeg startPeg;
    private TimePeg endPeg;

    public RemoteHeadTMU(EulerInput headInput, EulerHeadEmbodiment headEmbodiment, FeedbackManager fbm, BMLBlockPeg bmlPeg, String bmlId,
            String behId)
    {
        super(fbm, bmlPeg, bmlId, behId);
        this.headEmbodiment = headEmbodiment;
        this.headInput = headInput;
        endPeg = new TimePeg(bmlPeg);
        startPeg = new TimePeg(bmlPeg);
    }

    @Override
    public double getEndTime()
    {
        return endPeg.getGlobalValue();
    }

    @Override
    public double getRelaxTime()
    {
        return getEndTime();
    }

    @Override
    public double getStartTime()
    {
        return startPeg.getGlobalValue();
    }

    /**
     * @param startPeg the startPeg to set
     */
    public void setStartPeg(TimePeg startPeg)
    {
        this.startPeg = startPeg;
    }

    /**
     * @param endPeg the endPeg to set
     */
    public void setEndPeg(TimePeg endPeg)
    {
        this.endPeg = endPeg;
    }

    @Override
    public TimePeg getTimePeg(String syncId)
    {
        if (syncId.equals("start")) return startPeg;
        else if (syncId.equals("end")) return endPeg;
        return null;
    }

    @Override
    public boolean hasValidTiming()
    {
        if (endPeg.getGlobalValue() != TimePeg.VALUE_UNKNOWN && startPeg.getGlobalValue() != TimePeg.VALUE_UNKNOWN)
        {
            return endPeg.getGlobalValue() >= startPeg.getGlobalValue();
        }
        return true;
    }

    @Override
    public void setTimePeg(String syncId, TimePeg peg)
    {
        if (syncId.equals("start"))
        {
            startPeg = peg;
        }
        else if (syncId.equals("end"))
        {
            endPeg = peg;
        }
        else
        {
            log.warn("Can't set TimePeg for sync {}", syncId);
        }
    }

    @Override
    protected void playUnit(double time) throws TimedPlanUnitPlayException
    {
        headEmbodiment.setHeadRollPitchYawDegrees(headInput.getRollDegrees(), headInput.getPitchDegrees(), headInput.getYawDegrees());
    }

    @Override
    protected void startUnit(double time) throws TimedPlanUnitPlayException
    {
        sendFeedback("start", time);
    }

    @Override
    protected void stopUnit(double time) throws TimedPlanUnitPlayException
    {
        sendFeedback("end", time);
    }
}
