package asap.livemocapengine.inputs.loader;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Test;

import asap.environment.AsapVirtualHuman;
import asap.utils.Environment;

/**
 * Unit tests for the remote head input loader
 * @author welberge
 */
public class RemoteHeadInputLoaderTest
{
    private AsapVirtualHuman mockAsapVH = mock(AsapVirtualHuman.class);
    
    @Test
    public void test() throws IOException
    {
        String str = "<Loader id=\"id1\" loader=\"asap.livemocapengine.inputs.loader.RemoteHeadInputLoader\">" +
        		"<serverinfo host=\"localhost\" port=\"9123\"/></Loader>";
        RemoteHeadInputLoader loader = new RemoteHeadInputLoader();
        XMLTokenizer tok = new XMLTokenizer(str);
        tok.takeSTag();        
        loader.readXML(tok, "id1", mockAsapVH, new Environment[0]);
        assertNotNull(loader.getSensor());
    }
}
