package asap.asaprealizerembodiments;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Test;

import asap.realizerembodiments.JComponentEmbodiment;
import asap.realizerembodiments.JComponentEmbodimentLoader;
import asap.realizerembodiments.JComponentEnvironment;

/**
 * Unit test for the JComponentEmbodimentLoader
 * @author hvanwelbergen
 *
 */
public class JComponentEmbodimentLoaderTest
{
    JComponentEnvironment mockjce = mock(JComponentEnvironment.class);
    
    @Test
    public void test() throws IOException
    {
        when(mockjce.getJComponentEmbodiment("component1")).thenReturn(new JComponentEmbodiment());
        JComponentEmbodimentLoader loader = new JComponentEmbodimentLoader();
        String str = "<Loader id=\"l1\"><JComponent id=\"component1\"/></Loader>";
        XMLTokenizer tok = new XMLTokenizer(str);
        tok.takeSTag();
        loader.readXML(tok, "id1", "id1", "id1" , new Environment[]{mockjce}, new Loader[]{});
        assertNotNull(loader.getEmbodiment());
    }
}
