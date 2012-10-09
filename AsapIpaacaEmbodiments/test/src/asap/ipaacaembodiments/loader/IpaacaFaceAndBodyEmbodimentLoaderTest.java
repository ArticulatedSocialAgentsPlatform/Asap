package asap.ipaacaembodiments.loader;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import hmi.environmentbase.ClockDrivenCopyEnvironment;
import hmi.environmentbase.Environment;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import asap.ipaacaembodiments.IpaacaEmbodiment;

/**
 * Loads an IpaacaFaceAndBodyEmbodiment
 * @author hvanwelbergen
 *
 */
public class IpaacaFaceAndBodyEmbodimentLoaderTest
{
    private IpaacaEmbodimentLoader mockEmbodimentLoader = mock(IpaacaEmbodimentLoader.class);
    private IpaacaEmbodiment mockEmbodiment = mock(IpaacaEmbodiment.class);
    
    @SuppressWarnings("unchecked")
    @Test
    public void test() throws IOException
    {
        when(mockEmbodimentLoader.getEmbodiment()).thenReturn(mockEmbodiment);
        String str = "<Loader id=\"ipaacabodyandfaceembodiment\" loader=\"asap.ipaacaembodiments.loader.IpaacaFaceAndBodyEmbodimentLoader\">"+
        "<renaming renamingFile=\"billierenaming.txt\"/>"+
        "</Loader>";
        ClockDrivenCopyEnvironment env = new ClockDrivenCopyEnvironment(20);
        IpaacaFaceAndBodyEmbodimentLoader loader = new IpaacaFaceAndBodyEmbodimentLoader();
        XMLTokenizer tok = new XMLTokenizer(str);
        tok.takeSTag();
        loader.readXML(tok, "id1", "id1", "id1", ImmutableList.of(env).toArray(new Environment[0]), mockEmbodimentLoader);
        assertNotNull(loader.getEmbodiment());
        env.time(0);
        verify(mockEmbodiment).setJointData(any(ImmutableList.class), any(ImmutableMap.class));
    }
}
