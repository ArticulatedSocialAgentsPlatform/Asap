package asap.animationengine.loader;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.xml.XMLScanException;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Test;

import asap.realizerembodiments.AsapRealizerEmbodiment;

/**
 * Unit tests for HeadVisualProsodyProviderLoader
 * @author hvanwelbergen
 *
 */
public class HeadVisualProsodyProviderLoaderTest
{
    private MixedAnimationEngineLoader mockMixedAnimationEngineLoader = mock(MixedAnimationEngineLoader.class);
    private AsapRealizerEmbodiment mockAsapRealizerEmbodiment = mock(AsapRealizerEmbodiment.class);

    @Test
    public void test() throws IOException
    {
        HeadVisualProsodyProviderLoader loader = new HeadVisualProsodyProviderLoader();
        //@formatter:off
        String loaderStr =
          "<Loader id=\"visualprosody\""+ 
                "loader=\"asap.animationengine.loader.HeadVisualProsodyProviderLoader\""+
                "requiredloaders=\"\">"+       
                "<?include resources=\"\" file=\"visualprosodyprovider.xml\"?>"+
          "</Loader>";          
        //@formatter:on
        XMLTokenizer tok = new XMLTokenizer(loaderStr);
        tok.takeSTag();
        loader.readXML(tok, "id1", "id1", "id1", new Environment[0], new Loader[] { mockMixedAnimationEngineLoader,
                mockAsapRealizerEmbodiment });
        assertNotNull(loader.getVisualProsodyProvider());
    }

    @Test(expected = XMLScanException.class)
    public void testNoVisualProsodyProviderXML() throws IOException
    {
        HeadVisualProsodyProviderLoader loader = new HeadVisualProsodyProviderLoader();
        //@formatter:off
        String loaderStr =
          "<Loader id=\"visualprosody\""+ 
                "loader=\"asap.animationengine.loader.HeadVisualProsodyProviderLoader\""+
                "requiredloaders=\"\">"+
          "</Loader>";          
        //@formatter:on
        XMLTokenizer tok = new XMLTokenizer(loaderStr);
        tok.takeSTag();
        loader.readXML(tok, "id1", "id1", "id1", new Environment[0], new Loader[] { mockMixedAnimationEngineLoader,
                mockAsapRealizerEmbodiment });
    }

}
