package asap.ipaacattsbinding.loader;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.xml.XMLTokenizer;

import org.junit.Test;

/**
 * Unit tests for the IpaacaTTSBindingLoader
 * @author hvanwelbergen
 *
 */
public class IpaacaTTSBindingLoaderTest
{
    @Test
    public void test() throws IOException
    {
      //@formatter:off
        String bindingXML=
        "<Loader id=\"l1\" loader=\"asap.ipaacattsbinding.loader.IpaacaTTSBindingLoader\">"+
        "<PhonemeToVisemeMapping resources=\"Humanoids/shared/phoneme2viseme/\" filename=\"sampade2ikp.xml\"/>"+
        "<VisualProsodyAnalyzer type=\"OPENSMILE\"/>"+
        "</Loader>";            
        //@formatter:on
        IpaacaTTSBindingLoader loader = new IpaacaTTSBindingLoader();
        XMLTokenizer tok = new XMLTokenizer(bindingXML);
        tok.takeSTag();
        loader.readXML(tok, "id1", "id1", "id1" , new Environment[0], new Loader[0]);      
        assertNotNull(loader.getTTSBinding());        
    }
}
