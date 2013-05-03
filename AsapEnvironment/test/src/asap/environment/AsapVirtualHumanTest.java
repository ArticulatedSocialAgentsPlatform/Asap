package asap.environment;

import java.io.IOException;

import hmi.environmentbase.Environment;
import hmi.util.SystemClock;
import hmi.xml.XMLTokenizer;

import org.junit.Test;

/**
 * Unit tests for the AsapVirtualHuman
 * @author hvanwelbergen
 *
 */
public class AsapVirtualHumanTest
{
    private AsapEnvironment aEnv = new AsapEnvironment();
    
    @Test
    public void testEmpty() throws IOException
    {
        //@formatter:off
        String str = 
        "<AsapVirtualHuman>"+
                "<Loader id=\"realizer\" loader=\"asap.realizerembodiments.AsapRealizerEmbodiment\">" +
                "</Loader>"+
        "</AsapVirtualHuman>";
        //@formatter:on
        AsapVirtualHuman avh = new AsapVirtualHuman();
        avh.load(new XMLTokenizer(str), "id1", new Environment[]{aEnv}, new SystemClock());
    }
}
