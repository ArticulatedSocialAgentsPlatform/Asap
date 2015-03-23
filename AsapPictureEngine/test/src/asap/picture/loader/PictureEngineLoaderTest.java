/*******************************************************************************
 *******************************************************************************/
package asap.picture.loader;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import asap.realizer.Engine;
import asap.realizerembodiments.AsapRealizerEmbodiment;

/**
 * Unit tests for the PictureEngineLoader
 * @author Herwin
 */
public class PictureEngineLoaderTest
{
    private PictureEmbodiment mockEmbodiment = mock(PictureEmbodiment.class);
    private EmbodimentLoader mockEmbodimentLoader = mock(EmbodimentLoader.class);
    private AsapRealizerEmbodiment  mockAsapRealizerEmbodiment = mock(AsapRealizerEmbodiment.class);
    
    @Before
    public void setup()
    {
        when(mockEmbodimentLoader.getEmbodiment()).thenReturn(mockEmbodiment);
        when(mockAsapRealizerEmbodiment.getEmbodiment()).thenReturn(mockAsapRealizerEmbodiment);
    }
    
    @Test
    public void test() throws IOException
    {
        String loaderStr = 
        "<Loader id=\"pictureengine\" loader=\"asap.picture.loader.PictureEngineLoader\" requiredloaders=\"pictureembodiment\">"+
        "<PictureBinding basedir=\"\" resources=\"pictureengine/example/\" filename=\"picturebinding.xml\"/>"+
        "</Loader>";
        PictureEngineLoader loader = new PictureEngineLoader();        
        XMLTokenizer tok = new XMLTokenizer(loaderStr);
        tok.takeSTag();
        loader.readXML(tok, "pa1", "billie", "billie", new Environment[0], new Loader[]{mockEmbodimentLoader,mockAsapRealizerEmbodiment});
        verify(mockAsapRealizerEmbodiment).addEngine(any(Engine.class));
        assertNotNull(loader.getEngine());
    }
}
