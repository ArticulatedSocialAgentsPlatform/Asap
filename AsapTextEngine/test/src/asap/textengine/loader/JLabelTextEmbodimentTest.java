/*******************************************************************************
 *******************************************************************************/
package asap.textengine.loader;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.jcomponentenvironment.JComponentEmbodiment;
import hmi.jcomponentenvironment.loader.JComponentEmbodimentLoader;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import javax.swing.JLabel;

import org.junit.Test;

import asap.textengine.JLabelTextEmbodiment;

/**
 * Unit tests for the JLabelTextEmbodiment
 * @author hvanwelbergen
 *
 */
public class JLabelTextEmbodimentTest
{
    private JComponentEmbodiment mockComponentEmbodiment = mock(JComponentEmbodiment.class);
    private JComponentEmbodimentLoader mockJComponenentEmbodimentLoader = mock(JComponentEmbodimentLoader.class);
    
    @Test
    public void test() throws IOException, InterruptedException
    {
        when(mockJComponenentEmbodimentLoader.getEmbodiment()).thenReturn(mockComponentEmbodiment);
        String loaderStr = "<Loader id=\"pictureengine\" loader=\"asap.textengine.loader.JLabelTextEmbodiment\"/>";
        JLabelTextEmbodiment loader = new JLabelTextEmbodiment();
        XMLTokenizer tok = new XMLTokenizer(loaderStr);
        tok.takeSTag();
        loader.readXML(tok, "pa1", "billie", "billie", new Environment[0], new Loader[]{mockJComponenentEmbodimentLoader});
        Thread.sleep(500);
        verify(mockComponentEmbodiment).addJComponent(any(JLabel.class));
    }
}
