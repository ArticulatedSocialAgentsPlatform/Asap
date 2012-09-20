package asap.ipaacaembodiments.loader;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hmi.environmentbase.Environment;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Test;

import asap.ipaacaembodiments.IpaacaEmbodiment;

/**
 * Unit tests for the IpaacaFaceEmbodimentLoader
 * @author hvanwelbergen
 *
 */
public class IpaacaFaceEmbodimentLoaderTest
{
    private IpaacaEmbodimentLoader mockEmbodimentLoader = mock(IpaacaEmbodimentLoader.class);
    private IpaacaEmbodiment mockEmbodiment = mock(IpaacaEmbodiment.class);
    
    @Test
    public void test() throws IOException
    {
        when(mockEmbodimentLoader.getEmbodiment()).thenReturn(mockEmbodiment);
        String str = "<Loader id=\"ipaacafaceembodiment\" loader=\"asap.ipaacaembodiments.loader.IpaacaFaceEmbodimentLoader\"/>";                
        IpaacaFaceEmbodimentLoader loader = new IpaacaFaceEmbodimentLoader();
        XMLTokenizer tok = new XMLTokenizer(str);
        tok.takeSTag();
        loader.readXML(tok, "id1", "id1", "id1", new Environment[0], mockEmbodimentLoader);
        assertNotNull(loader.getEmbodiment());
    }
}
