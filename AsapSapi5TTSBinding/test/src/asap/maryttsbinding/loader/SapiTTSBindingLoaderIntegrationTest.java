/*******************************************************************************
 *******************************************************************************/
package asap.maryttsbinding.loader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.tts.TTSTiming;
import hmi.tts.Visime;
import hmi.util.OS;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import saiba.bml.core.SpeechBehaviour;
import asap.sapittsbinding.loader.SapiTTSBindingLoader;

/**
 * Integration tests for the SapiTTSBinding
 * @author hvanwelbergen
 *
 */
public class SapiTTSBindingLoaderIntegrationTest
{
    @Before
    public void setup()
    {
        Assume.assumeTrue(OS.equalsOS(OS.WINDOWS));   
    }
    
    @Test
    public void test() throws IOException, InterruptedException
    {
        //@formatter:off
        String bindingXML=
        "<Loader id=\"l1\" loader=\"asap.sapittsbinding.loader.SapiTTSBindingLoader\"/>";
        //@formatter:on        
        SapiTTSBindingLoader loader = new SapiTTSBindingLoader();
        XMLTokenizer tok = new XMLTokenizer(bindingXML);
        tok.takeSTag();
        loader.readXML(tok, "id1", "id1", "id1" , new Environment[0], new Loader[0]);      
        assertNotNull(loader.getTTSBinding());
        TTSTiming ti = loader.getTTSBinding().speak(SpeechBehaviour.class,"blah blah test 1 2 3");
        assertThat(ti.getDuration(),greaterThan(0d));
        
        boolean hasVisemes = false;
        for(Visime v: ti.getVisimes())
        {
            if(v.getNumber()!=0)
            {
                hasVisemes = true;
            }
        }
        assertTrue("Failed to set up PhonemeToVisemeMapping",hasVisemes);
        Thread.sleep(4000);
    }
}
