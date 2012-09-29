package asap.ipaacaembodiments.loader;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import hmi.environmentbase.Environment;
import hmi.xml.XMLTokenizer;
import ipaaca.InputBuffer;
import ipaaca.OutputBuffer;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.ipaacaembodiments.IpaacaEmbodiment;
import asap.ipaacaembodiments.IpaacaEmbodimentInitStub;

/**
 * Unit tests for the IpaacaEmbodimentLoader
 * @author hvanwelbergen
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(IpaacaEmbodiment.class)
public class IpaacaEmbodimentLoaderTest
{
    IpaacaEmbodimentInitStub initStub = new IpaacaEmbodimentInitStub();
    
    @Before
    public void setupEnv() throws Exception
    {
        initStub.stubInit(mock(OutputBuffer.class),mock(InputBuffer.class));
    }
    
    @Test
    public void test() throws IOException
    {
        String str = "<Loader id=\"ipaacaembodiment\" loader=\"asap.ipaacaembodiments.loader.IpaacaEmbodimentLoader\"/>";                
        IpaacaEmbodimentLoader loader = new IpaacaEmbodimentLoader();
        XMLTokenizer tok = new XMLTokenizer(str);
        tok.takeSTag();
        loader.readXML(tok, "id1", "id1", "id1", new Environment[0]);
        assertNotNull(loader.getEmbodiment());
    }
}
