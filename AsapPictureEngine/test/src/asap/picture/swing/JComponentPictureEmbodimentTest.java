/*******************************************************************************
 *******************************************************************************/
package asap.picture.swing;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.jcomponentenvironment.JComponentEmbodiment;
import hmi.jcomponentenvironment.loader.JComponentEmbodimentLoader;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

/**
 * unit test for the JComponentPictureEmbodiment
 * @author Herwin
 * 
 */
public class JComponentPictureEmbodimentTest
{
    private JComponentEmbodimentLoader mockJel = mock(JComponentEmbodimentLoader.class);
    private JComponentEmbodiment mockEmbodiment = mock(JComponentEmbodiment.class);

    @Before
    public void setup()
    {
        when(mockJel.getEmbodiment()).thenReturn(mockEmbodiment);
    }

    @Test
    public void test() throws IOException
    {
        JComponentPictureEmbodiment jpe = new JComponentPictureEmbodiment();
        String str = "<Loader id=\"l1\"/>";
        XMLTokenizer tok = new hmi.xml.XMLTokenizer(str);
        tok.takeSTag();
        jpe.readXML(tok, "id1", "id1", "id1", new Environment[] {}, new Loader[] {mockJel});
        assertNotNull(jpe.getEmbodiment());
    }
}
