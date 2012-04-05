package asap.animationengine.loader;

import java.io.IOException;

import hmi.xml.XMLTokenizer;

import org.junit.Test;

import asap.environment.AsapVirtualHuman;
import asap.utils.Environment;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
/**
 * Unit test cases for TimedAnimationUnitLipSynchProviderLoader
 * @author Herwin
 *
 */
public class TimedAnimationUnitLipSynchProviderLoaderTest
{
    AsapVirtualHuman mockAsapVH = mock(AsapVirtualHuman.class);
    MixedAnimationEngineLoader mockMixedAnimationEngineLoader = mock(MixedAnimationEngineLoader.class);
    @Test
    public void test() throws IOException
    {
        TimedAnimationUnitLipSynchProviderLoader loader = new TimedAnimationUnitLipSynchProviderLoader();
        String str="<Loader id=\"l1\"><SpeechBinding basedir=\"\" resources=\"Humanoids/shared/speechbinding/\" " +
        		"filename=\"ikpspeechbinding.xml\"/></Loader>";
        XMLTokenizer tok = new XMLTokenizer(str);
        tok.takeSTag();
        loader.readXML(tok, "id1", mockAsapVH, new Environment[0],mockMixedAnimationEngineLoader);
        assertNotNull(loader.getLipSyncProvider());
    }
}
