package asap.livemocapengine.loader;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import asap.environment.AsapVirtualHuman;
import asap.environment.EmbodimentLoader;
import asap.environment.SensorLoader;
import asap.livemocapengine.inputs.EulerInput;
import asap.realizer.AsapRealizer;
import asap.realizer.feedback.FeedbackManager;
import asap.utils.Environment;
import asap.utils.EulerHeadEmbodiment;
/**
 * Unit test for the LiveMocapEngineLoader
 * @author welberge
 */
public class LiveMocapEngineLoaderTest
{
    private AsapVirtualHuman mockAsapVH = mock(AsapVirtualHuman.class);
    private AsapRealizer mockRealizer = mock(AsapRealizer.class);
    private EmbodimentLoader mockArmandiaLoader = mock(EmbodimentLoader.class);
    private SensorLoader mockArrowEulerLoader = mock(SensorLoader.class);
    private FeedbackManager mockFbm = mock(FeedbackManager.class);
    private EulerInput mockEulerInput = mock(EulerInput.class);
    private EulerHeadEmbodiment mockEmbodiment = mock(EulerHeadEmbodiment.class);
    
    @Before
    public void setup()
    {
        when(mockArmandiaLoader.getId()).thenReturn("armandia");
        when(mockArrowEulerLoader.getId()).thenReturn("arroweuler");
        when(mockArrowEulerLoader.getSensor()).thenReturn(mockEulerInput);
        when(mockArmandiaLoader.getEmbodiment()).thenReturn(mockEmbodiment);
        when(mockAsapVH.getElckerlycRealizer()).thenReturn(mockRealizer);
        when(mockRealizer.getFeedbackManager()).thenReturn(mockFbm);
    }
    
    @Test(timeout=1000)
    public void test() throws IOException
    {
        LiveMocapEngineLoader loader = new LiveMocapEngineLoader();
        String str = "<Loader id=\"livemocapengine\" loader=\"asap.livemocapengine.loader.LiveMocapEngineLoader\">" +
                "<input name=\"arroweuler\" interface=\"asap.livemocapengine.inputs.EulerInput\"/>"+
                "<output name=\"armandia\" interface=\"asap.utils.EulerHeadEmbodiment\"/>"+
        		"</Loader>";
        XMLTokenizer tok = new XMLTokenizer(str);
        tok.takeSTag();        
        loader.readXML(tok, "id1", mockAsapVH, new Environment[0], mockArmandiaLoader, mockArrowEulerLoader);
        assertNotNull(loader.getEngine());
    }
}
