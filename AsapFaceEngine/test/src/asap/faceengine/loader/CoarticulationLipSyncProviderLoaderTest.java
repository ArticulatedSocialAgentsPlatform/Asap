package asap.faceengine.loader;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Test;

import asap.faceengine.lipsync.CoarticulationLipSyncProviderLoader;
import asap.realizerembodiments.AsapRealizerEmbodiment;

/**
 * Unit tests for the CoarticulationLipSyncProviderLoader
 * @author hvanwelbergen
 *
 */
public class CoarticulationLipSyncProviderLoaderTest
{
    private FaceEngineLoader mockFaceAnimationEngineLoader = mock(FaceEngineLoader.class);
    private AsapRealizerEmbodiment mockAsapRealizerEmbodiment = mock(AsapRealizerEmbodiment.class);
    
    @Test(timeout=200)
    public void test() throws IOException
    {
        CoarticulationLipSyncProviderLoader loader = new CoarticulationLipSyncProviderLoader();
        String str = "<Loader id=\"l1\" loader=\"asap.faceengine.lipsync.CoarticulationLipSyncProviderLoader\">"
                + "<MorphVisemeBinding resources=\"\" filename=\"ikpvisemebinding_dominance.xml\"/>"
                + "<DominanceParameters resources=\"\" filename=\"dominance.xml\"/>"
                + "<PhonemeClasses resources=\"\" filename=\"phonemeclass.xml\"/>"
                + "<PhonemeMagnitudes resources=\"\" filename=\"phonememags.xml\"/>"
                + "</Loader>";
        
        XMLTokenizer tok = new XMLTokenizer(str);
        tok.takeSTag();
        loader.readXML(tok, "id1", "id1", "id1", new Environment[0], new Loader[]{mockFaceAnimationEngineLoader, mockAsapRealizerEmbodiment});
        assertNotNull(loader.getLipSyncProvider());
    }
}
