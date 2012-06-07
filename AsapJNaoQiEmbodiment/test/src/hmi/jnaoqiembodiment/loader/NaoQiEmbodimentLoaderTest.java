package hmi.jnaoqiembodiment.loader;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.io.IOException;

import hmi.xml.XMLTokenizer;

import org.junit.Test;

import asap.environment.AsapVirtualHuman;
import asap.utils.Environment;

/**
 * Unit test for the NaoQiEmbodimentLoader
 * @author welberge
 *
 */
public class NaoQiEmbodimentLoaderTest
{
    private AsapVirtualHuman mockAsapVH = mock(AsapVirtualHuman.class);
    
    @Test
    public void test() throws IOException
    {
        String str = "<Loader id=\"naoqiembodiment\" loader=\"hmi.jnaoqiembodiment.loader.NaoQiEmbodimentLoader\">" +
                "<naoqi ip=\"localhost\" port=\"10\"/>"+ 
        		"</Loader>";
        NaoQiEmbodimentLoader loader = new NaoQiEmbodimentLoader();
        XMLTokenizer tok = new XMLTokenizer(str);
        tok.takeSTag();        
        loader.readXML(tok, "id1", mockAsapVH, new Environment[0]);
        assertNotNull(loader.getEmbodiment());
    }
}
