package asap.faceengine.loader;

import static org.junit.Assert.*;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Test;

import asap.environment.AsapVirtualHuman;
import asap.utils.Environment;
import static org.mockito.Mockito.*;

public class TimedFaceUnitLipSynchProviderLoaderTest
{
    AsapVirtualHuman mockAsapVH = mock(AsapVirtualHuman.class);
    FaceEngineLoader mockMixedAnimationEngineLoader = mock(FaceEngineLoader.class);
    
    @Test
    public void test() throws IOException
    {
        TimedFaceUnitLipSynchProviderLoader loader = new TimedFaceUnitLipSynchProviderLoader();
        String str="<Loader id=\"l1\">" +
        		"<MorphVisemeBinding resources=\"Humanoids/armandia/facebinding/\" filename=\"ikpvisemebinding.xml\"/></Loader>";
        XMLTokenizer tok = new XMLTokenizer(str);
        tok.takeSTag();
        loader.readXML(tok, "id1", mockAsapVH, new Environment[0],mockMixedAnimationEngineLoader);
        assertNotNull(loader.getLipSyncProvider());
    }
}
