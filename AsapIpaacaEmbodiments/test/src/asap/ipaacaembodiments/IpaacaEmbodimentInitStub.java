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

import java.util.ArrayList;
import java.util.List;
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
    private List<IUEventHandler> handlers = new ArrayList<>();
    
    public void callHandlers(InputBuffer inBuffer, String iuId, boolean x, IUEventType type, String category)
    {
        for (IUEventHandler handler : handlers)
        {
            handler.call(inBuffer, iuId,x,type,category);
        }
    }
    public void stubInit(final OutputBuffer outBuffer, final InputBuffer inBuffer) throws Exception
    {
        AbstractIU mockIU = mock(AbstractIU.class);
        Payload mockPayload = mock(Payload.class);
        
        whenNew(InputBuffer.class).withArguments(anyString(), any(Set.class)).thenReturn(inBuffer);
        whenNew(OutputBuffer.class).withArguments(anyString()).thenReturn(outBuffer);
        doAnswer(new Answer<Void>(){

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable
            {
                IUEventHandler handler = (IUEventHandler)(invocation.getArguments()[0]);
                handlers.add(handler);
                handler.call(inBuffer,"iu1", false, IUEventType.ADDED, "jointDataConfigRequest");
                return null;
            }}).when(inBuffer).registerHandler(any(IUEventHandler.class));
        when(inBuffer.getIU("iu1")).thenReturn(mockIU);
        when(mockIU.getPayload()).thenReturn(mockPayload);
        when(mockPayload.get("joints")).thenReturn("joint1,joint2,joint3");
        when(mockPayload.get("morphs")).thenReturn("morph1,morph2,morph3");  
    }
}
