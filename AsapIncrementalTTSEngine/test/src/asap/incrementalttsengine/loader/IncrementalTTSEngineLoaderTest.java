package asap.incrementalttsengine.loader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.xml.XMLTokenizer;

import org.junit.Before;
import org.junit.Test;

import asap.incrementalspeechengine.loader.IncrementalTTSEngineLoader;
import asap.realizerembodiments.AsapRealizerEmbodiment;

/**
 * unit tests for the IncrementalTTSEngineLoader
 * @author hvanwelbergen
 *
 */
public class IncrementalTTSEngineLoaderTest
{
    private AsapRealizerEmbodiment  mockAsapRealizerEmbodiment = mock(AsapRealizerEmbodiment.class);
    
    @Before
    public void setup()
    {
        when(mockAsapRealizerEmbodiment.getEmbodiment()).thenReturn(mockAsapRealizerEmbodiment);
    }
    
    @Test
    public void test() throws IOException
    {
        //@formatter:off
        String loaderStr =
          "<Loader id=\"incrementaltts\" loader=\"asap.incrementalttsengine.loader.IncrementalTTSEngineLoader\"/>";
        //@formatter:on
        XMLTokenizer tok = new XMLTokenizer(loaderStr);
        tok.takeSTag();
        IncrementalTTSEngineLoader loader = new IncrementalTTSEngineLoader();
        loader.readXML(tok, "ma1", "billie", "billie", new Environment[]{}, new Loader[]{mockAsapRealizerEmbodiment});
        assertEquals("ma1", loader.getId());
        assertNotNull(loader.getEngine());
    }
}
