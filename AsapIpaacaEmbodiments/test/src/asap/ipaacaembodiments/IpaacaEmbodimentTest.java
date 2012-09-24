package asap.ipaacaembodiments;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import hmi.animation.VJoint;
import ipaaca.LocalIU;
import ipaaca.OutputBuffer;

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
    
    private void setupEnv() throws Exception
    {
        IpaacaEmbodimentInitStub.stubInit(mockOutBuffer);
        env.initialize();        
    }
    
    @Test(timeout=500)
    public void testInitiatialize() throws Exception
    {
        setupEnv();
        assertThat(env.getAvailableJoints(), containsInAnyOrder("joint1","joint2","joint3"));
        assertThat(env.getAvailableMorphs(), containsInAnyOrder("morph1","morph2","morph3"));
    }
    
    @Test
    public void testSetUsedJoints() throws Exception
    {
        setupEnv();
        env.setUsedJoints(ImmutableList.of("joint1","joint3"));
        ArgumentCaptor<LocalIU> argument = ArgumentCaptor.forClass(LocalIU.class);
        verify(mockOutBuffer).add(argument.capture());
        LocalIU iu = argument.getValue();
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
        verify(mockOutBuffer).add(argument.capture());
        LocalIU iu = argument.getValue();
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
        env.setJointData(new ImmutableList.Builder<VJoint>().build(),ImmutableMap.of("morph1",1f,"morph3",2f));
        env.setJointData(new ImmutableList.Builder<VJoint>().build(),ImmutableMap.of("morph1",2f,"morph3",3f));
        
        ArgumentCaptor<LocalIU> argument = ArgumentCaptor.forClass(LocalIU.class);
        verify(mockOutBuffer,times(3)).add(argument.capture());
        
        LocalIU iu = argument.getAllValues().get(0);
        assertEquals("joint1", iu.getPayload().get("joints_provided"));
        assertEquals("joint2,joint3", iu.getPayload().get("joints_not_provided"));
        assertEquals("morph1,morph3", iu.getPayload().get("morphs_provided"));
        assertEquals("morph2", iu.getPayload().get("morphs_not_provided"));
        assertEquals("jointDataConfigReply",iu.getCategory());
        
        iu = argument.getAllValues().get(1);
        assertEquals("100.0 200.0", iu.getPayload().get("morph_data"));
        assertEquals("1 0 0 0  0 1 0 0  0 0 1 0  0 0 0 1    1 0 0 0  0 1 0 0  0 0 1 0  0 0 0 1", iu.getPayload().get("joint_data"));
        assertEquals("jointData",iu.getCategory());
        
        iu = argument.getAllValues().get(2);
        assertEquals("200.0 300.0", iu.getPayload().get("morph_data"));
        assertEquals("1 0 0 0  0 1 0 0  0 0 1 0  0 0 0 1    1 0 0 0  0 1 0 0  0 0 1 0  0 0 0 1", iu.getPayload().get("joint_data"));
        assertEquals("jointData",iu.getCategory());
    }
}
