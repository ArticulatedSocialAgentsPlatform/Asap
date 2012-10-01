package asap.ipaacaembodiments;

import static hmi.testutil.math.Quat4fTestUtil.assertQuat4fRotationEquivalent;
import static hmi.testutil.math.Vec3fTestUtil.assertVec3fEquals;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import hmi.animation.VJoint;
import hmi.math.Quat4f;
import hmi.math.Vec3f;
import ipaaca.AbstractIU;
import ipaaca.IUEventType;
import ipaaca.InputBuffer;
import ipaaca.LocalIU;
import ipaaca.OutputBuffer;
import ipaaca.Payload;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
/**
 * Unit tests for the IpaacaEnvironment
 * @author hvanwelbergen
 * 
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(IpaacaEmbodiment.class)
public class IpaacaEmbodimentTest
{
    
    private IpaacaEmbodiment env = new IpaacaEmbodiment();
    private OutputBuffer mockOutBuffer = mock(OutputBuffer.class);
    private static final float PRECISION = 0.001f;
    
    private InputBuffer mockInBuffer = mock(InputBuffer.class);         
    private IpaacaEmbodimentInitStub initStub = new IpaacaEmbodimentInitStub();

    private void setupEnv() throws Exception
    {
        initStub.stubInit(mockOutBuffer,mockInBuffer);
        env.initialize();
    }
    
    private void assertVJointHasProperties(VJoint vj, String expectedSid, float[]expectedT, float expectedQ[], VJoint expectedParent)
    {
        float t[] = Vec3f.getVec3f();
        float q[] = Quat4f.getQuat4f();
        
        vj.getTranslation(t);
        assertVec3fEquals(expectedT,t, PRECISION);
        
        vj.getRotation(q);
        assertQuat4fRotationEquivalent(expectedQ,q, PRECISION);
        
        assertEquals(expectedParent, vj.getParent());
    }
    
    @Test(timeout=500)
    public void testInitiatialize() throws Exception
    {
        setupEnv();        
        assertThat(env.getAvailableJoints(), containsInAnyOrder(IpaacaEmbodimentInitStub.JOINTS));
        assertThat(env.getAvailableMorphs(), containsInAnyOrder(IpaacaEmbodimentInitStub.MORPHS));
        VJoint vj = env.getRootJointCopy("copy");
        assertEquals(IpaacaEmbodimentInitStub.JOINTS[0], vj.getSid());
        
        assertVJointHasProperties(vj, IpaacaEmbodimentInitStub.JOINTS[0], IpaacaEmbodimentInitStub.JOINT_TRANSLATIONS[0], 
                IpaacaEmbodimentInitStub.JOINT_ROTATIONS[0], null);
        
        assertEquals(1, vj.getChildren().size());
        VJoint vj2 = vj.getChildren().get(0);
        assertVJointHasProperties(vj2, IpaacaEmbodimentInitStub.JOINTS[1], IpaacaEmbodimentInitStub.JOINT_TRANSLATIONS[1], 
                IpaacaEmbodimentInitStub.JOINT_ROTATIONS[1], vj);
        
        assertEquals(1, vj2.getChildren().size());
        VJoint vj3 = vj2.getChildren().get(0);
        assertVJointHasProperties(vj3, IpaacaEmbodimentInitStub.JOINTS[2], IpaacaEmbodimentInitStub.JOINT_TRANSLATIONS[2], 
                IpaacaEmbodimentInitStub.JOINT_ROTATIONS[2], vj2);
    }
    
    @Test
    public void testNotifyAtInit() throws Exception
    {
        setupEnv(); 
        ArgumentCaptor<LocalIU> argument = ArgumentCaptor.forClass(LocalIU.class);
        verify(mockOutBuffer).add(argument.capture());
        LocalIU iu = argument.getValue();
        assertEquals("componentNotify", iu.getCategory());
        assertEquals("new", iu.getPayload().get("state"));
    }
    
    private void sendNotify(String state)
    {
        AbstractIU mockIUNotify = mock(AbstractIU.class);
        Payload mockNotifyPayload = mock(Payload.class);
        when(mockIUNotify.getCategory()).thenReturn("componentNotify");
        when(mockIUNotify.getPayload()).thenReturn(mockNotifyPayload);
        when(mockInBuffer.getIU("iuNotify")).thenReturn(mockIUNotify);
        when(mockNotifyPayload.get("state")).thenReturn(state);
        
        initStub.callHandlers(mockInBuffer, "iuNotify", false, IUEventType.ADDED, "componentNotify");        
    }
    
    @Test
    public void testNotifyAtNotifyNew() throws Exception
    {
        setupEnv(); 
        sendNotify("new");
        ArgumentCaptor<LocalIU> argument = ArgumentCaptor.forClass(LocalIU.class);
        verify(mockOutBuffer,times(2)).add(argument.capture());
        LocalIU iu = argument.getAllValues().get(1);
        assertEquals("componentNotify", iu.getCategory());
        assertEquals("old", iu.getPayload().get("state"));
    }
    
    @Test
    public void testNoNotifyAtNotifyOld() throws Exception
    {
        setupEnv(); 
        sendNotify("old");        
        ArgumentCaptor<LocalIU> argument = ArgumentCaptor.forClass(LocalIU.class);
        verify(mockOutBuffer,times(1)).add(argument.capture());        
    }
    
    @Test
    public void testSetUsedJoints() throws Exception
    {
        setupEnv();
        env.setUsedJoints(ImmutableList.of("joint1","joint3"));
        ArgumentCaptor<LocalIU> argument = ArgumentCaptor.forClass(LocalIU.class);
        verify(mockOutBuffer,times(2)).add(argument.capture());
        LocalIU iu = argument.getAllValues().get(1);
        assertEquals("joint1,joint3", iu.getPayload().get("joints_provided"));
        assertEquals("joint2", iu.getPayload().get("joints_not_provided"));
        assertEquals("", iu.getPayload().get("morphs_provided"));
        assertEquals("morph1,morph2,morph3", iu.getPayload().get("morphs_not_provided"));
        assertEquals("jointDataConfigReply",iu.getCategory());
    }
    
    @Test
    public void testSetUsedMorphs() throws Exception
    {
        setupEnv();
        env.setUsedMorphs(ImmutableList.of("morph1","morph3"));
        ArgumentCaptor<LocalIU> argument = ArgumentCaptor.forClass(LocalIU.class);
        verify(mockOutBuffer,times(2)).add(argument.capture());
        LocalIU iu = argument.getAllValues().get(1);
        assertEquals("", iu.getPayload().get("joints_provided"));
        assertEquals("joint1,joint2,joint3", iu.getPayload().get("joints_not_provided"));
        assertEquals("morph1,morph3", iu.getPayload().get("morphs_provided"));
        assertEquals("morph2", iu.getPayload().get("morphs_not_provided"));
        assertEquals("jointDataConfigReply",iu.getCategory());
    }
    
    @Test
    public void testSetJointData() throws Exception
    {
        setupEnv();
        env.setJointData(new ImmutableList.Builder<float[]>().build(),ImmutableMap.of("morph1",1f,"morph3",2f));
        env.setJointData(new ImmutableList.Builder<float[]>().build(),ImmutableMap.of("morph1",2f,"morph3",3f));
        
        ArgumentCaptor<LocalIU> argument = ArgumentCaptor.forClass(LocalIU.class);
        verify(mockOutBuffer,times(4)).add(argument.capture());
        
        LocalIU iu = argument.getAllValues().get(1);
        assertEquals("joint1", iu.getPayload().get("joints_provided"));
        assertEquals("joint2,joint3", iu.getPayload().get("joints_not_provided"));
        assertEquals("morph1,morph3", iu.getPayload().get("morphs_provided"));
        assertEquals("morph2", iu.getPayload().get("morphs_not_provided"));
        assertEquals("jointDataConfigReply",iu.getCategory());
        
        iu = argument.getAllValues().get(2);
        assertEquals("100.0 200.0", iu.getPayload().get("morph_data"));
        assertEquals("1 0 0 0  0 1 0 0  0 0 1 0  0 0 0 1    1 0 0 0  0 1 0 0  0 0 1 0  0 0 0 1", iu.getPayload().get("joint_data"));
        assertEquals("jointData",iu.getCategory());
        
        iu = argument.getAllValues().get(3);
        assertEquals("200.0 300.0", iu.getPayload().get("morph_data"));
        assertEquals("1 0 0 0  0 1 0 0  0 0 1 0  0 0 0 1    1 0 0 0  0 1 0 0  0 0 1 0  0 0 0 1", iu.getPayload().get("joint_data"));
        assertEquals("jointData",iu.getCategory());
    }
}
