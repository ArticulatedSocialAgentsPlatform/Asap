package asap.faceengine.loader;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.io.IOException;

import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.xml.XMLTokenizer;

import org.junit.Test;

import asap.realizerembodiments.AsapRealizerEmbodiment;

/**
 * Unit test for the TimeFaceUnitIncrementalLipSyncProviderLoader
 * @author hvanwelbergen
 *
 */
public class TimeFaceUnitIncrementalLipSyncProviderLoaderTest
{
    private FaceEngineLoader mockFaceAnimationEngineLoader = mock(FaceEngineLoader.class);
    private AsapRealizerEmbodiment mockAsapRealizerEmbodiment = mock(AsapRealizerEmbodiment.class);
    
    @Test
    public void test() throws IOException
    {
        TimeFaceUnitIncrementalLipSyncProviderLoader loader = new TimeFaceUnitIncrementalLipSyncProviderLoader();
        String str = "<Loader id=\"l1\">"
                + "<MorphVisemeBinding resources=\"Humanoids/armandia/facebinding/\" filename=\"ikpvisemebinding.xml\"/></Loader>";
        XMLTokenizer tok = new XMLTokenizer(str);
        tok.takeSTag();
        loader.readXML(tok, "id1", "id1", "id1", new Environment[0], new Loader[]{mockFaceAnimationEngineLoader, mockAsapRealizerEmbodiment});
        assertNotNull(loader.getLipSyncProvider());
    }
}
