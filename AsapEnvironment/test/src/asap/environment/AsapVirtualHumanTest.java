package asap.environment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.util.SystemClock;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import lombok.Getter;

import org.junit.Test;

import asap.realizer.Engine;
import asap.realizerembodiments.EngineLoader;

class TestLoader implements Loader
{
    @Getter
    private String id;

    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
            Loader... requiredLoaders) throws IOException
    {
        this.id = loaderId;
    }

    @Override
    public void unload()
    {

    }
}

class TestEngine implements EngineLoader
{
    public static final Engine mockEngine = mock(Engine.class); 
    
    @Getter
    private String id;

    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
            Loader... requiredLoaders) throws IOException
    {
        this.id = loaderId;
    }

    @Override
    public void unload()
    {

    }

    @Override
    public Engine getEngine()
    {
        return mockEngine;
    }
}

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
        avh.load(new XMLTokenizer(str), "id1", new Environment[] { aEnv }, new SystemClock());
    }

    @Test
    public void testLoader() throws IOException
    {
        //@formatter:off
        String str = 
        "<AsapVirtualHuman>"+
                "<Loader id=\"realizer\" loader=\"asap.realizerembodiments.AsapRealizerEmbodiment\">" +
                "</Loader>"+
                "<Loader id=\"loader1\" loader=\"asap.environment.TestLoader\">" +
                "</Loader>"+
        "</AsapVirtualHuman>";
        //@formatter:on
        AsapVirtualHuman avh = new AsapVirtualHuman();
        avh.load(new XMLTokenizer(str), "id1", new Environment[] { aEnv }, new SystemClock());
        assertNotNull(avh.getLoaders().get("loader1"));

    }

    @Test
    public void testEngine() throws IOException
    {
        //@formatter:off
        String str = 
        "<AsapVirtualHuman>"+
                "<Loader id=\"realizer\" loader=\"asap.realizerembodiments.AsapRealizerEmbodiment\">" +
                "</Loader>"+
                "<Loader id=\"engine1\" loader=\"asap.environment.TestEngine\">" +
                "</Loader>"+
        "</AsapVirtualHuman>";
        //@formatter:on
        AsapVirtualHuman avh = new AsapVirtualHuman();
        avh.load(new XMLTokenizer(str), "id1", new Environment[] { aEnv }, new SystemClock());
        assertNotNull(avh.getLoaders().get("engine1"));
        assertEquals(TestEngine.mockEngine, avh.getEngines().get(0));
    }
}
