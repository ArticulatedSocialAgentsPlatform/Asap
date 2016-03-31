/*******************************************************************************
 *******************************************************************************/
package asap.livemocapengine.inputs.loader;

import static org.junit.Assert.assertNotNull;
import hmi.environmentbase.Environment;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Test;

/**
 * Unit tests for the remote head input loader
 * @author welberge
 */
public class RemoteHeadInputLoaderTest
{

    @Test
    public void test() throws IOException
    {
        String str = "<Loader id=\"id1\" loader=\"asap.livemocapengine.inputs.loader.RemoteHeadInputLoader\">"
                + "<serverinfo host=\"localhost\" port=\"9123\"/></Loader>";
        RemoteHeadInputLoader loader = new RemoteHeadInputLoader();
        XMLTokenizer tok = new XMLTokenizer(str);
        tok.takeSTag();
        loader.readXML(tok, "id1", "id1", "id1", new Environment[0]);
        assertNotNull(loader.getSensor());
    }
}
