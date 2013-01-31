package asap.incrementalttsengine.loader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Test;

import asap.incrementalspeechengine.loader.IncrementalTTSEngineLoader;
import asap.realizerembodiments.AsapRealizerEmbodiment;
import asap.realizerembodiments.LipSynchProviderLoader;

/**
 * unit tests for the IncrementalTTSEngineLoader
 * @author hvanwelbergen
 * 
 */
public class IncrementalTTSEngineLoaderTest
{
    private AsapRealizerEmbodiment mockAsapRealizerEmbodiment = mock(AsapRealizerEmbodiment.class);

    @Test
    public void test() throws IOException
    {
        //@formatter:off
        String loaderStr =
          "<Loader id=\"incrementaltts\" loader=\"asap.incrementalttsengine.loader.IncrementalTTSEngineLoader\">"+
          "<Dispatcher resources=\"\" filename=\"sphinx-config.xml\"/>"+
          "<MaryTTSIncremental localdir=\"AsapResource/MARYTTSIncremental/resource/MARYTTSIncremental\"/>"+
          "</Loader>";
        //@formatter:on
        XMLTokenizer tok = new XMLTokenizer(loaderStr);
        tok.takeSTag();
        IncrementalTTSEngineLoader loader = new IncrementalTTSEngineLoader();
        loader.readXML(tok, "ma1", "billie", "billie", new Environment[] {}, new Loader[] { mockAsapRealizerEmbodiment });
        assertEquals("ma1", loader.getId());
        assertNotNull(loader.getEngine());
        assertEquals(System.getProperty("shared.project.root") + "/AsapResource/MARYTTSIncremental/resource/MARYTTSIncremental",
                System.getProperty("mary.base"));
    }
    
    
    @Test
    public void testWithLipSync() throws IOException
    {
        LipSynchProviderLoader mockLipsync = mock(LipSynchProviderLoader.class);
        when(mockLipsync.getId()).thenReturn("facelipsync");
        
        //@formatter:off
        String loaderStr =
          "<Loader id=\"incrementaltts\" loader=\"asap.incrementalttsengine.loader.IncrementalTTSEngineLoader\" requiredloaders=\"facelipsync\">"+
          "<Dispatcher resources=\"\" filename=\"sphinx-config.xml\"/>"+
          "<PhonemeToVisemeMapping resources=\"Humanoids/shared/phoneme2viseme/\" filename=\"sampade2ikp.xml\"/>"+
          "<MaryTTSIncremental localdir=\"AsapResource/MARYTTSIncremental/resource/MARYTTSIncremental\"/>"+
          "</Loader>";
        //@formatter:on
        XMLTokenizer tok = new XMLTokenizer(loaderStr);
        tok.takeSTag();
        IncrementalTTSEngineLoader loader = new IncrementalTTSEngineLoader();
        loader.readXML(tok, "ma1", "billie", "billie", new Environment[] {}, new Loader[] { mockAsapRealizerEmbodiment,mockLipsync});
        assertEquals("ma1", loader.getId());
        assertNotNull(loader.getEngine());
        assertEquals(System.getProperty("shared.project.root") + "/AsapResource/MARYTTSIncremental/resource/MARYTTSIncremental",
                System.getProperty("mary.base"));
    }
}
