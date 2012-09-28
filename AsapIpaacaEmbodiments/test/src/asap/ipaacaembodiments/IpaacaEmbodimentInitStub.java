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
    public static final String MORPHS[]={"morph1","morph2","morph3"};
    public static final String JOINTS[]={"joint1","joint2","joint3"};
    public static final String JOINT_PARENTS[]={"-","joint1","joint2"};
    public static final float JOINT_TRANSLATIONS[][]={{1,2,3},{4,5,6},{7,8,9}};
    public static final float JOINT_ROTATIONS[][]={{1,0,0,0},{0,1,0,0},{0,0.707f,0.707f,0}};
    
    private static String toCommaSeperatedString(String str[])
    {
        StringBuffer buf = new StringBuffer();
        for(String s:str)
        {
            buf.append(",");   
            buf.append(s);                     
        }
        return buf.toString().substring(1);
    }
    
    private static String toSeperatedString(float [][]input)
    {
        StringBuffer buf = new StringBuffer();
        for(float []v:input)
        {
            buf.append(",");   
            for(float vi:v)
            {
                buf.append(" ");
                buf.append(vi);
            }
        }
        return buf.toString().substring(1);
    }
    
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
        when(mockPayload.get("joints")).thenReturn(toCommaSeperatedString(JOINTS));
        when(mockPayload.get("morphs")).thenReturn(toCommaSeperatedString(MORPHS));
        when(mockPayload.get("joint_parents")).thenReturn(toCommaSeperatedString(JOINT_PARENTS));
        when(mockPayload.get("joint_translations")).thenReturn(toSeperatedString(JOINT_TRANSLATIONS));
        when(mockPayload.get("joint_rotations")).thenReturn(toSeperatedString(JOINT_ROTATIONS));
    }
}
