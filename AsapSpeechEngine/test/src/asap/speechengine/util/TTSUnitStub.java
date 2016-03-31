/*******************************************************************************
 *******************************************************************************/
package asap.speechengine.util;

import hmi.tts.TTSTiming;
import hmi.tts.TimingInfo;
import saiba.bml.core.Behaviour;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.speechengine.SpeechUnitPlanningException;
import asap.speechengine.TimedTTSUnit;
import asap.speechengine.ttsbinding.TTSBinding;


/**
 * Testing stub for a TimedTTSUnit
 * @author welberge
 *
 */
public class TTSUnitStub extends TimedTTSUnit
{
    public TTSUnitStub(FeedbackManager bfm, BMLBlockPeg bbPeg, String text, String id, String bmlId, TTSBinding ttsBin,
            Class<? extends Behaviour> behClass, TTSTiming timing)
    {
        super(bfm, bbPeg, text, bmlId, id, ttsBin, behClass);         
        this.timing = timing;
    }

    @Override
    protected TimingInfo getTiming() throws SpeechUnitPlanningException
    {
        return null;
    }

    @Override
    public void sendProgress(double playTime, double time)
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

    @Override
    public void setup()
    {

    }

    @Override
    public void setParameterValue(String paramId, String value)
    {
    }
}
