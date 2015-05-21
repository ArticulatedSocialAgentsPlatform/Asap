/*******************************************************************************
 *******************************************************************************/
package asap.speechengine;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hmi.tts.TimingInfo;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import saiba.bml.core.Behaviour;
import saiba.bml.core.SpeechBehaviour;
import saiba.bml.feedback.BMLWarningFeedback;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.MultiThreadedPlanPlayer;
import asap.realizer.planunit.PlanManager;
import asap.realizer.planunit.PlanUnitFloatParameterNotFoundException;
import asap.realizer.planunit.PlanUnitParameterNotFoundException;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.planunit.TimedPlanUnitState;
import asap.realizerport.util.ListBMLFeedbackListener;
import asap.speechengine.ttsbinding.TTSBinding;


/**
 * VerbalPlanPlayer unit test cases 
 * @author Herwin
 */
public class VerbalPlanPlayerTest
{
    private TTSBinding mockTTSBind = mock(TTSBinding.class);
    private TimedAbstractSpeechUnit mockPlanUnit1 = mock(TimedAbstractSpeechUnit.class);
    private TimedAbstractSpeechUnit mockPlanUnit2 = mock(TimedAbstractSpeechUnit.class);
    private TimedAbstractSpeechUnit mockPlanUnit3 = mock(TimedAbstractSpeechUnit.class);
    
    private FeedbackManager mockFeedbackManager = mock(FeedbackManager.class);
    private PlanManager<TimedAbstractSpeechUnit> planManager = new PlanManager<TimedAbstractSpeechUnit>();
    
    private static class StubTTSUnit extends TimedTTSUnit
    {

        public StubTTSUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String text, String id, String bmlId, TTSBinding ttsBin,
                Class<? extends Behaviour> behClass)
        {
            super(bfm,bbPeg, text, bmlId, id, ttsBin, behClass);            
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
        protected void startUnit(double time) throws TimedPlanUnitPlayException
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
            return 5;
        }

        @Override
        public void setParameterValue(String paramId, String value)
        {

        }

        @Override
        public float getFloatParameterValue(String paramId) throws PlanUnitFloatParameterNotFoundException
        {
            return 0;
        }

        @Override
        public String getParameterValue(String paramId) throws PlanUnitParameterNotFoundException
        {
            return "";
        }        
    }

    @Test
    public void testPlayTTSUnit() throws InterruptedException
    {
        List<BMLWarningFeedback> bwList = new ArrayList<BMLWarningFeedback>();

        StubTTSUnit ttsUnitStub = new StubTTSUnit(mockFeedbackManager, BMLBlockPeg.GLOBALPEG, "Hello world", "id1", "bml1", mockTTSBind,
                SpeechBehaviour.class);
        ttsUnitStub.setState(TimedPlanUnitState.LURKING);
        TimePeg tpStart = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpStart.setGlobalValue(0);
        ttsUnitStub.setStart(tpStart);

        MultiThreadedPlanPlayer<TimedAbstractSpeechUnit> vpp = new MultiThreadedPlanPlayer<TimedAbstractSpeechUnit>(mockFeedbackManager,planManager);
        vpp.addFeedbackListener(new ListBMLFeedbackListener.Builder().warningList(bwList).build());

        planManager.addPlanUnit(ttsUnitStub);
        vpp.play(0);

        Thread.sleep(100);
        assertEquals(0, bwList.size());
        assertEquals(TimedPlanUnitState.IN_EXEC, ttsUnitStub.getState());
        vpp.shutdown();
    }

    @Test
    public void testInterruptUnit() throws InterruptedException
    {
        MultiThreadedPlanPlayer<TimedAbstractSpeechUnit> vpp = new MultiThreadedPlanPlayer<TimedAbstractSpeechUnit>(mockFeedbackManager,planManager);
        when(mockPlanUnit1.getEndTime()).thenReturn(3d);
        when(mockPlanUnit1.getBMLId()).thenReturn("bml1");
        when(mockPlanUnit1.getId()).thenReturn("beh1");
        when(mockPlanUnit2.getStartTime()).thenReturn(1d);
        when( mockPlanUnit2.getEndTime()).thenReturn(4d);
        when(mockPlanUnit2.getBMLId()).thenReturn("bml1");
        when(mockPlanUnit2.getId()).thenReturn("beh2");
        when(mockPlanUnit3.getStartTime()).thenReturn(0d);
        when(mockPlanUnit3.getEndTime()).thenReturn(5d);
        when(mockPlanUnit3.getBMLId()).thenReturn("bml2");
        when(mockPlanUnit3.getId()).thenReturn("beh3");
        
        planManager.addPlanUnit(mockPlanUnit1);
        planManager.addPlanUnit(mockPlanUnit2);
        planManager.addPlanUnit(mockPlanUnit3);
        assertEquals(3, vpp.getNumberOfPlanUnits());

        vpp.stopBehaviourBlock("bml1", 1.0);
        assertEquals(1, vpp.getNumberOfPlanUnits());
        vpp.shutdown();
    }
}
