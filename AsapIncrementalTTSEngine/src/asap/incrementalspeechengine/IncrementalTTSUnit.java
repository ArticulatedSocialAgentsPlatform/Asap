package asap.incrementalspeechengine;

import inpro.audio.DispatchStream;

import java.util.List;

import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedAbstractPlanUnit;

import com.google.common.collect.ImmutableList;

import done.inpro.system.carchase.HesitatingSynthesisIU;


public class IncrementalTTSUnit extends TimedAbstractPlanUnit
{
    private HesitatingSynthesisIU synthesisIU;
    private DispatchStream dispatcher;
    private final TimePeg startPeg;
    private final TimePeg relaxPeg;
    private final TimePeg endPeg;
    private double duration;
    
    public IncrementalTTSUnit(FeedbackManager fbm, BMLBlockPeg bmlPeg, String bmlId, String behId, String text, DispatchStream dispatcher)
    {
        super(fbm, bmlPeg, bmlId, behId);        
        synthesisIU = new HesitatingSynthesisIU(text);
        this.dispatcher = dispatcher;
        startPeg = new TimePeg(bmlPeg);
        endPeg = new TimePeg(bmlPeg);
        relaxPeg = new TimePeg(bmlPeg);
        duration = synthesisIU.duration();
    }

    @Override
    public double getStartTime()
    {
        return startPeg.getGlobalValue();
    }

    @Override
    public double getEndTime()
    {
        return endPeg.getGlobalValue();
    }

    @Override
    public double getRelaxTime()
    {
        return relaxPeg.getGlobalValue();
    }

    @Override
    public List<String> getAvailableSyncs()
    {
        //TODO: add marks
        return ImmutableList.of("start","relax","end");
    }

    @Override
    public TimePeg getTimePeg(String syncId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setTimePeg(String syncId, TimePeg peg)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean hasValidTiming()
    {
        //TODO Auto-generated method stub
        return true;
    }

    protected void startUnit(double time) 
    {
        dispatcher.playStream(synthesisIU.getAudio(), true);
    }
    
    @Override
    protected void playUnit(double time) 
    {
                
    }

    @Override
    protected void stopUnit(double time) 
    {
        dispatcher.interruptPlayback();        
    }
    
}
