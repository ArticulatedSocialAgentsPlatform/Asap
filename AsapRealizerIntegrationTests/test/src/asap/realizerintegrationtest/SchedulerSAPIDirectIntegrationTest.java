/*******************************************************************************
 *******************************************************************************/
package asap.realizerintegrationtest;

import hmi.testutil.LabelledParameterized;
import hmi.util.OS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;

import asap.realizer.feedback.FeedbackManagerImpl;
import asap.realizer.scheduler.BMLBlockManager;
import asap.sapittsbinding.SAPITTSBindingFactory;
import asap.speechengine.DirectTTSUnitFactory;

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
        bbm = new BMLBlockManager();
        bfm = new FeedbackManagerImpl(bbm, "character1");

        ArrayList<SpeechEngineFactory> speechEngineFactories = new ArrayList<SpeechEngineFactory>();

        if (OS.equalsOS(OS.WINDOWS))
        {
            speechEngineFactories.add(new TTSEngineFactory(new DirectTTSUnitFactory(bfm), 
                    new SAPITTSBindingFactory(), soundManager));            
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
