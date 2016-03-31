/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.loader;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Test;

import asap.realizerembodiments.AsapRealizerEmbodiment;

/**
 * Unit test cases for TimedAnimationUnitLipSynchProviderLoader
 * @author Herwin
 * 
 */
public class TimedAnimationUnitIncrementalLipSynchProviderLoaderTest
{
    MixedAnimationEngineLoader mockMixedAnimationEngineLoader = mock(MixedAnimationEngineLoader.class);
    AsapRealizerEmbodiment mockAsapRealizerEmbodiment = mock(AsapRealizerEmbodiment.class);

    @Test
    public void test() throws IOException
    {
        TimedAnimationUnitIncrementalLipSynchProviderLoader loader = new TimedAnimationUnitIncrementalLipSynchProviderLoader();
        String str = "<Loader id=\"l1\"><SpeechBinding basedir=\"\" resources=\"Humanoids/shared/speechbinding/\" "
                + "filename=\"ikpspeechbinding.xml\"/></Loader>";
        XMLTokenizer tok = new XMLTokenizer(str);
        tok.takeSTag();
        loader.readXML(tok, "id1", "id1", "id1" , new Environment[0], new Loader[]{mockMixedAnimationEngineLoader, mockAsapRealizerEmbodiment});
        assertNotNull(loader.getLipSyncProvider());
    }
}
