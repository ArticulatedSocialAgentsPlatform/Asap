/*******************************************************************************
 *******************************************************************************/
package asap.livemocapengine.loader;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.SensorLoader;
import hmi.headandgazeembodiments.EulerHeadEmbodiment;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import asap.livemocapengine.inputs.EulerInput;
import asap.realizer.AsapRealizer;
import asap.realizer.feedback.FeedbackManager;
import asap.realizerembodiments.AsapRealizerEmbodiment;

/**
 * Unit test for the LiveMocapEngineLoader
 * @author welberge
 */
public class LiveMocapEngineLoaderTest
{
    private AsapRealizer mockRealizer = mock(AsapRealizer.class);
    private AsapRealizerEmbodiment mockRealizerEmbodiment = mock(AsapRealizerEmbodiment.class);
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
        when(mockRealizer.getFeedbackManager()).thenReturn(mockFbm);
        when(mockRealizerEmbodiment.getFeedbackManager()).thenReturn(mockFbm);
        when(mockRealizerEmbodiment.getEmbodiment()).thenReturn(mockRealizerEmbodiment);
    }

    @Test(timeout = 1000)
    public void test() throws IOException
    {
        LiveMocapEngineLoader loader = new LiveMocapEngineLoader();
        String str = "<Loader id=\"livemocapengine\" loader=\"asap.livemocapengine.loader.LiveMocapEngineLoader\">"
                + "<input name=\"arroweuler\" interface=\"asap.livemocapengine.inputs.EulerInput\"/>"
                + "<output name=\"armandia\" interface=\"hmi.headandgazeembodiments.EulerHeadEmbodiment\"/>" + "</Loader>";
        XMLTokenizer tok = new XMLTokenizer(str);
        tok.takeSTag();
        loader.readXML(tok, "id1", "id1", "id1", new Environment[0], mockArmandiaLoader, mockArrowEulerLoader, mockRealizerEmbodiment);
        assertNotNull(loader.getEngine());
    }
}
