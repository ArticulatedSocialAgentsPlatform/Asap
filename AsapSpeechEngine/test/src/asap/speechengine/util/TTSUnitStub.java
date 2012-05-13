package asap.speechengine.util;

import saiba.bml.core.Behaviour;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.planunit.TimedPlanUnitPlayException;
import hmi.tts.Bookmark;
import hmi.tts.TimingInfo;
import java.util.List;

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
    final double prefDuration;

    public TTSUnitStub(FeedbackManager bfm, BMLBlockPeg bbPeg, String text, String id, String bmlId, TTSBinding ttsBin,
            Class<? extends Behaviour> behClass, double prefDuration, List<Bookmark> bms)
    {
        super(bfm, bbPeg, text, bmlId, id, ttsBin, behClass);
        this.prefDuration = prefDuration;
        bookmarks.addAll(bms);
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
    public double getPreferedDuration()
    {
        return prefDuration;
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
