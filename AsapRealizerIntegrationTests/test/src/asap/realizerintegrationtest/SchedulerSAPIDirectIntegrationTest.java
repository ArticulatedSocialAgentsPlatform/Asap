package asap.realizerintegrationtest;

import hmi.elckerlyc.feedback.FeedbackManagerImpl;
import hmi.elckerlyc.scheduler.BMLBlockManager;
import hmi.speechengine.DirectTTSUnitFactory;
import hmi.speechengine.TimedTTSUnitFactory;
import hmi.speechengine.ttsbinding.SAPITTSBindingFactory;
import hmi.speechengine.ttsbinding.TTSBindingFactory;
import hmi.testutil.LabelledParameterized;
import hmi.util.OS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;

/**
 * Rapid test using TestSchedulerParameterized with only SAPI ttsgeneration
 * using direct output
 * 
 * @author welberge
 * 
 */
@RunWith(LabelledParameterized.class)
public class SchedulerSAPIDirectIntegrationTest extends SchedulerParameterizedIntegrationTest
{
     
    
    public SchedulerSAPIDirectIntegrationTest(String label, SpeechEngineFactory vp) throws IOException
    {
        super(label, vp);
    }

    @Parameters
    public static Collection<Object[]> configs() throws Exception
    {
        ArrayList<TTSBindingFactory> ttsBinders = new ArrayList<TTSBindingFactory>();
        ttsBinders.add(new SAPITTSBindingFactory());
        
        ArrayList<TimedTTSUnitFactory> ttsUnitFactories = new ArrayList<TimedTTSUnitFactory>();
        
        if(OS.equalsOS(OS.WINDOWS))
        {
            ttsUnitFactories.add(new DirectTTSUnitFactory(new FeedbackManagerImpl(new BMLBlockManager(),"character1")));
        }

        ArrayList<SpeechEngineFactory> speechEngineFactories = new ArrayList<SpeechEngineFactory>();

        // generate all permutations of TTSUnitFactories and
        // AbstractTTSGenerators
        for (TTSBindingFactory ttsBind : ttsBinders)
        {
            for (TimedTTSUnitFactory ttsUFac : ttsUnitFactories)
            {
                speechEngineFactories.add(new TTSEngineFactory(ttsUFac, ttsBind,soundManager));
            }
        }

        Collection<Object[]> objs = new ArrayList<Object[]>();

        for (SpeechEngineFactory sp : speechEngineFactories)
        {
            Object obj[] = new Object[2];

            obj[0] = "SpeechPlanner = " + sp.getType();
            obj[1] = sp;
            objs.add(obj);
        }
        return objs;
    }    
}
