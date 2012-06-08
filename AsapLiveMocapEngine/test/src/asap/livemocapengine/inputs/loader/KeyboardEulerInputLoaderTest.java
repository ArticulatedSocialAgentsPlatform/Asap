package asap.livemocapengine.inputs.loader;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import asap.environment.AsapVirtualHuman;
import asap.environment.impl.JFrameEmbodiment;
import asap.realizer.AsapRealizer;
import asap.realizer.feedback.FeedbackManager;
import asap.utils.Environment;

/**
 * Unit tests for the KeyboardEulerInputLoader
 * @author Herwin
 *
 */
public class KeyboardEulerInputLoaderTest
{
    private AsapVirtualHuman mockAsapVH = mock(AsapVirtualHuman.class);
    private AsapRealizer mockRealizer = mock(AsapRealizer.class);
    private FeedbackManager mockFbm = mock(FeedbackManager.class);
    private JFrameEmbodiment mockJFrameEmbodiment = mock(JFrameEmbodiment.class);
    @Before
    public void setup()
    {
        when(mockAsapVH.getElckerlycRealizer()).thenReturn(mockRealizer);
        when(mockRealizer.getFeedbackManager()).thenReturn(mockFbm);
    }
    
    @Test
    public void testLoad() throws IOException
    {
        String str="<Loader id=\"l1\" loader=\"asap.livemocapengine.inputs.loader.KeyboardEulerInputLoader\"/>";
        KeyboardInputLoader loader = new KeyboardInputLoader();
        XMLTokenizer tok = new XMLTokenizer(str);
        tok.takeSTag();        
        loader.readXML(tok, "id1", mockAsapVH, new Environment[0], mockJFrameEmbodiment);
        assertNotNull(loader.getSensor());
        //verify(mockJFrameEmbodiment).addKeyListener(loader.getSensor());
    }
}
