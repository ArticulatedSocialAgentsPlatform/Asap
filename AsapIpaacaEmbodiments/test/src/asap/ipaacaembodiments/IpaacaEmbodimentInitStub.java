package asap.ipaacaembodiments;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import ipaaca.AbstractIU;
import ipaaca.IUEventHandler;
import ipaaca.IUEventType;
import ipaaca.InputBuffer;
import ipaaca.OutputBuffer;
import ipaaca.Payload;

import java.util.Set;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Stubs out the init process of the IpaacaEmbodiment
 * @author hvanwelbergen
 *
 */
public class IpaacaEmbodimentInitStub
{
    public static void stubInit(OutputBuffer outBuffer) throws Exception
    {
        final InputBuffer mockInBuffer = mock(InputBuffer.class);         
        AbstractIU mockIU = mock(AbstractIU.class);
        Payload mockPayload = mock(Payload.class);
        
        whenNew(InputBuffer.class).withArguments(anyString(), any(Set.class)).thenReturn(mockInBuffer);
        whenNew(OutputBuffer.class).withArguments(anyString()).thenReturn(outBuffer);
        doAnswer(new Answer<Void>(){

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable
            {
                IUEventHandler handler = (IUEventHandler)(invocation.getArguments()[0]);
                handler.call(mockInBuffer,"iu1", false, IUEventType.ADDED, "jointDataConfigRequest");
                return null;
            }}).when(mockInBuffer).registerHandler(any(IUEventHandler.class));
        when(mockInBuffer.getIU("iu1")).thenReturn(mockIU);
        when(mockIU.getPayload()).thenReturn(mockPayload);
        when(mockPayload.get("joints")).thenReturn("joint1,joint2,joint3");
        when(mockPayload.get("morphs")).thenReturn("morph1,morph2,morph3");  
    }
}
