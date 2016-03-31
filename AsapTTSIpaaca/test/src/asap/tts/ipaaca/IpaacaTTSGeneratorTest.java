/*******************************************************************************
 *******************************************************************************/
package asap.tts.ipaaca;

import hmi.tts.TTSException;
import ipaaca.Initializer;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Unit tests for the IpaacaTTSGenerator, requires a running spread daemon and a running speech client
 * @author hvanwelbergen
 */
public class IpaacaTTSGeneratorTest
{
    static
    {
        Initializer.initializeIpaacaRsb();
    }
    private IpaacaTTSGenerator ttsGen = new IpaacaTTSGenerator(); 
    
    @Ignore
    @Test
    public void test() throws InterruptedException, TTSException
    {
        //System.out.println(ttsGen.speakBML("test<sync id=\"s1\"/> 1 2 3"));
        ttsGen.speakBMLToFile("test<sync id=\"s1\"/> 1 2 3 4","/tmp/test.wav");
        //ttsGen.speak("test 1 2 3");
        //ttsGen.getTiming("test 1 2 3");
        Thread.sleep(4000);
    }
}
