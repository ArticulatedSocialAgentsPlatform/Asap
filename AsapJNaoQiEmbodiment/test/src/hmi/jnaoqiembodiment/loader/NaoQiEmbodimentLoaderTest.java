/*******************************************************************************
 *******************************************************************************/
package hmi.jnaoqiembodiment.loader;

import static org.junit.Assert.assertNotNull;
import hmi.environmentbase.Environment;
import hmi.util.OS;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Assume;
import org.junit.Test;

/**
 * Unit test for the NaoQiEmbodimentLoader
 * @author welberge
 * 
 */
public class NaoQiEmbodimentLoaderTest
{

    @Test
    public void test() throws IOException
    {
        // FIXME: make the NaoQiEmbodiment work in windows
        Assume.assumeTrue(OS.equalsOS(OS.WINDOWS));
        Assume.assumeTrue(System.getProperty("sun.arch.data.model")=="32"); 
        String str = "<Loader id=\"naoqiembodiment\" loader=\"hmi.jnaoqiembodiment.loader.NaoQiEmbodimentLoader\">"
                + "<naoqi ip=\"localhost\" port=\"10\"/>" + "</Loader>";
        NaoQiEmbodimentLoader loader = new NaoQiEmbodimentLoader();
        XMLTokenizer tok = new XMLTokenizer(str);
        tok.takeSTag();
        loader.readXML(tok, "id1", "id1", "id1", new Environment[0]);
        assertNotNull(loader.getEmbodiment());
    }
}
