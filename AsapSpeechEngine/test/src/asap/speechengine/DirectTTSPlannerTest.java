/*******************************************************************************
 *******************************************************************************/
package asap.speechengine;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import hmi.tts.Bookmark;
import hmi.tts.Phoneme;
import hmi.tts.TTSException;
import hmi.tts.TimingInfo;
import hmi.tts.Visime;
import hmi.tts.WordDescription;

import java.util.ArrayList;
import java.util.List;

import saiba.bml.core.SpeechBehaviour;
import asap.realizer.pegboard.BMLBlockPeg;

/**
 * Unit test cases for a DirectTTSPlanner
 * @author welberge
 *
 */
public class DirectTTSPlannerTest extends TTSPlannerTest
{
    
    
    @Override
    protected void mockTTSUnitFactoryExpectations() throws TTSException
    {
        final TimedDirectTTSUnit ttsUnit = new TimedDirectTTSUnit(mockFeedbackManager,bbPeg,
                SPEECHTEXT, BMLID, SPEECHID, mockTTSBinding,
                SpeechBehaviour.class);
        Phoneme p = new Phoneme(0,(int)(SPEECH_DURATION*1000),false);
        List<Phoneme> ps = new ArrayList<Phoneme>();
        ps.add(p);
        WordDescription wd2 = new WordDescription("world", ps, new ArrayList<Visime>());
        final List<Bookmark> bms = new ArrayList<Bookmark>();
        final Bookmark bm = new Bookmark("s1", wd2, 500);
        bms.add(bm);
        final List<WordDescription> wds = new ArrayList<WordDescription>();
        wds.add(wd2);
        TimingInfo tInfo = new TimingInfo(wds,bms,new ArrayList<Visime>());
        
        when(mockTTSUnitFactory.createTimedTTSUnit(
                (BMLBlockPeg)any(), 
                anyString(),
                anyString(),
                eq(BMLID), 
                eq(SPEECHID), 
                eq(mockTTSBinding), 
                eq(SpeechBehaviour.class))).thenReturn(ttsUnit);  
        when(mockTTSBinding.getTiming(SpeechBehaviour.class, SPEECHTEXT)).thenReturn(tInfo);        
    }
}
