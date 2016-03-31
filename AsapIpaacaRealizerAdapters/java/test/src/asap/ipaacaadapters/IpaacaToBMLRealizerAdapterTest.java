/*******************************************************************************
 *******************************************************************************/
package asap.ipaacaadapters;

import static org.hamcrest.Matchers.allOf;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import ipaaca.Initializer;
import ipaaca.InputBuffer;
import ipaaca.LocalIU;
import ipaaca.OutputBuffer;
import ipaaca.util.ComponentNotifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.realizerport.RealizerPort;

/**
 * Unit tests for the IpaacaToBMLRealizerAdapter
 * @author hvanwelbergen
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ IpaacaToBMLRealizerAdapter.class, Initializer.class, InputBuffer.class, OutputBuffer.class })
public class IpaacaToBMLRealizerAdapterTest
{
    private final InputBuffer mockInputBuffer = mock(InputBuffer.class);
    private final OutputBuffer mockOutputBuffer = mock(OutputBuffer.class);
    private IpaacaToBMLRealizerAdapter adapter;
    private RealizerPort mockRealizerPort = mock(RealizerPort.class);
    
    @Before
    public void setup() throws Exception
    {
        whenNew(InputBuffer.class).withArguments(any(String.class), any(Set.class)).thenReturn(mockInputBuffer);
        whenNew(OutputBuffer.class).withArguments(any(String.class)).thenReturn(mockOutputBuffer);
        PowerMockito.mockStatic(Initializer.class);
        adapter = new IpaacaToBMLRealizerAdapter(mockRealizerPort);
    }
    
    @Test
    public void testInit()
    {
        Map<String,String> expectedPayload = new HashMap<>();
        expectedPayload.put(ComponentNotifier.RECEIVE_CATEGORIES,IpaacaBMLConstants.BML_KEY);
        expectedPayload.put(ComponentNotifier.SEND_CATEGORIES,IpaacaBMLConstants.BML_FEEDBACK_KEY);
        expectedPayload.put(ComponentNotifier.STATE, "new");
        expectedPayload.put(ComponentNotifier.NAME, "IpaacaToBMLRealizerAdapter");
        expectedPayload.put(ComponentNotifier.FUNCTION, "bmlrealizer");
        verify(mockOutputBuffer, times(1)).add(argThat(allOf(new IUCategoryMatcher<LocalIU>(ComponentNotifier.NOTIFY_CATEGORY), 
                new IUPayloadMatcher<LocalIU>(expectedPayload))));
    }
    
    @Test
    public void testFeedback()
    {
        String feedback = "feedback";
        adapter.feedback(feedback);
        Map<String,String> expectedPayload = new HashMap<>();
        expectedPayload.put(IpaacaBMLConstants.BML_FEEDBACK_KEY,feedback);
        verify(mockOutputBuffer,times(1)).add(argThat(allOf(new IUCategoryMatcher<LocalIU>(IpaacaBMLConstants.BML_FEEDBACK_CATEGORY)
                ,new IUPayloadMatcher<LocalIU>(expectedPayload))));
    }
}
