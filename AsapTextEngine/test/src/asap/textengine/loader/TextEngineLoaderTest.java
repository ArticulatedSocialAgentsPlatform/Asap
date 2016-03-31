/*******************************************************************************
 *******************************************************************************/
package asap.textengine.loader;

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
import asap.textengine.JLabelTextEmbodiment;

/**
 * Unit tests for the TextEngineLoader
 * @author hvanwelbergen
 * 
 */
public class TextEngineLoaderTest
{
    private AsapRealizerEmbodiment  mockAsapRealizerEmbodiment = mock(AsapRealizerEmbodiment.class);
    private EmbodimentLoader mockTextEmbodimentLoader = mock(EmbodimentLoader.class);
        
    @Before
    public void setup()
    {
        when(mockAsapRealizerEmbodiment.getEmbodiment()).thenReturn(mockAsapRealizerEmbodiment);
        when(mockTextEmbodimentLoader.getEmbodiment()).thenReturn(new JLabelTextEmbodiment());
    }
    
    @Test
    public void test() throws IOException, InterruptedException
    {
        String loaderStr = "<Loader id=\"textengine\" loader=\"asap.textengine.loader.TextEngineLoader\">" + "</Loader>";
        TextEngineLoader loader = new TextEngineLoader();
        XMLTokenizer tok = new XMLTokenizer(loaderStr);
        tok.takeSTag();
        loader.readXML(tok, "pa1", "billie", "billie", new Environment[0], new Loader[]{mockAsapRealizerEmbodiment, mockTextEmbodimentLoader});
        verify(mockAsapRealizerEmbodiment).addEngine(any(Engine.class));
        assertNotNull(loader.getEngine());
    }
}
