package asap.ipaacaembodiments.loader;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import hmi.animation.VJoint;
import hmi.environmentbase.ClockDrivenCopyEnvironment;
import hmi.environmentbase.Environment;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Test;

import asap.ipaacaembodiments.IpaacaEmbodiment;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Unit tests for the IpaacaFaceEmbodimentLoader
 * @author hvanwelbergen
 *
 */
public class IpaacaFaceEmbodimentLoaderTest
{
    private IpaacaEmbodimentLoader mockEmbodimentLoader = mock(IpaacaEmbodimentLoader.class);
    private IpaacaEmbodiment mockEmbodiment = mock(IpaacaEmbodiment.class);
    
    @SuppressWarnings("unchecked")
    @Test
    public void test() throws IOException
    {
        when(mockEmbodimentLoader.getEmbodiment()).thenReturn(mockEmbodiment);
        String str = "<Loader id=\"ipaacafaceembodiment\" loader=\"asap.ipaacaembodiments.loader.IpaacaFaceEmbodimentLoader\"/>";
        ClockDrivenCopyEnvironment env = new ClockDrivenCopyEnvironment(20);
        IpaacaFaceEmbodimentLoader loader = new IpaacaFaceEmbodimentLoader();
        XMLTokenizer tok = new XMLTokenizer(str);
        tok.takeSTag();
        loader.readXML(tok, "id1", "id1", "id1", ImmutableList.of(env).toArray(new Environment[0]), mockEmbodimentLoader);
        assertNotNull(loader.getEmbodiment());
        env.time(0);
        verify(mockEmbodiment).setJointData(new ImmutableList.Builder<VJoint>().build(), (ImmutableMap<String, Float>)any());
    }
}
