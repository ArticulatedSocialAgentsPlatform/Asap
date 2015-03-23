/*******************************************************************************
 *******************************************************************************/
package asap.ipaacaiuadapters.loader;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import hmi.util.Clock;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Test;

import asap.realizerport.BMLFeedbackListener;
import asap.realizerport.RealizerPort;


/**
 * Unit tests for the IpaacaToBMLRealizerAdapterLoader
 * @author Herwin
 *
 */
public class IpaacaToBMLRealizerAdapterLoaderTest
{
    private RealizerPort mockRealizerPort = mock(RealizerPort.class);
    private Clock mockSchedulingClock = mock(Clock.class);
    
    @Test
    public void testReadFromXML() throws IOException
    {
        String pipeLoaderStr = "<PipeLoader id=\"id1\" loader=\"x\"/>";
        XMLTokenizer tok = new XMLTokenizer(pipeLoaderStr);
        tok.takeSTag("PipeLoader");
        IpaacaToBMLRealizerAdapterLoader loader = new IpaacaToBMLRealizerAdapterLoader();
        loader.readXML(tok, "id1", "vh1", "name", mockRealizerPort, mockSchedulingClock);
        verify(mockRealizerPort, times(1)).addListeners(any(BMLFeedbackListener[].class));
        assertEquals(mockRealizerPort, loader.getAdaptedRealizerPort());
    }
}
