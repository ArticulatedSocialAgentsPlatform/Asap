/*******************************************************************************
 *******************************************************************************/
package asap.ipaacaembodiments.loader;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import hmi.animation.VJoint;
import hmi.environmentbase.ClockDrivenCopyEnvironment;
import hmi.environmentbase.Environment;
import hmi.xml.XMLScanException;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import asap.ipaacaembodiments.IpaacaEmbodiment;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Loads an IpaacaFaceAndBodyEmbodiment
 * @author hvanwelbergen
 *
 */
public class IpaacaFaceAndBodyEmbodimentLoaderTest
{
    private IpaacaEmbodimentLoader mockEmbodimentLoader = mock(IpaacaEmbodimentLoader.class);
    private IpaacaEmbodiment mockEmbodiment = mock(IpaacaEmbodiment.class);
    
    @Before
    public void setup()
    {
        when(mockEmbodimentLoader.getEmbodiment()).thenReturn(mockEmbodiment);
        when(mockEmbodiment.getRootJointCopy(anyString())).thenReturn(new VJoint("BipKevin","BipKevin"));
        when(mockEmbodiment.getAvailableJoints()).thenReturn(ImmutableList.of("BipKevin"));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void test() throws IOException
    {
        String str = "<Loader id=\"ipaacabodyandfaceembodiment\" loader=\"asap.ipaacaembodiments.loader.IpaacaFaceAndBodyEmbodimentLoader\">"+
        "<renaming skeletonRenamingFile=\"billieskeletonrenaming.xml\" morphRenamingFile=\"billiemorphsrenaming.xml\"/>"+
        "</Loader>";
        ClockDrivenCopyEnvironment env = new ClockDrivenCopyEnvironment(20);
        IpaacaFaceAndBodyEmbodimentLoader loader = new IpaacaFaceAndBodyEmbodimentLoader();
        XMLTokenizer tok = new XMLTokenizer(str);
        tok.takeSTag();
        loader.readXML(tok, "id1", "id1", "id1", ImmutableList.of(env).toArray(new Environment[0]), mockEmbodimentLoader);
        assertNotNull(loader.getEmbodiment());
        env.time(0);
        verify(mockEmbodiment).setJointData(any(ImmutableList.class), any(ImmutableMap.class));
    }
    
    @Test(expected=XMLScanException.class)
    public void testNonExistingSkeletonRenamingFile() throws IOException
    {
        String str = "<Loader id=\"ipaacabodyandfaceembodiment\" loader=\"asap.ipaacaembodiments.loader.IpaacaFaceAndBodyEmbodimentLoader\">"+
        "<renaming skeletonRenamingFile=\"nonexistantskel.xml\"/>"+
        "</Loader>";
        ClockDrivenCopyEnvironment env = new ClockDrivenCopyEnvironment(20);
        IpaacaFaceAndBodyEmbodimentLoader loader = new IpaacaFaceAndBodyEmbodimentLoader();
        XMLTokenizer tok = new XMLTokenizer(str);
        tok.takeSTag();
        loader.readXML(tok, "id1", "id1", "id1", ImmutableList.of(env).toArray(new Environment[0]), mockEmbodimentLoader);        
    }
    
    @Test(expected=XMLScanException.class)
    public void testNonExistingMorphRenamingFile() throws IOException
    {
        String str = "<Loader id=\"ipaacabodyandfaceembodiment\" loader=\"asap.ipaacaembodiments.loader.IpaacaFaceAndBodyEmbodimentLoader\">"+
        "<renaming skeletonRenamingFile=\"billieskeletonrenaming.xml\" morphRenamingFile=\"nonexistantskel.xml\"/>"+
        "</Loader>";
        ClockDrivenCopyEnvironment env = new ClockDrivenCopyEnvironment(20);
        IpaacaFaceAndBodyEmbodimentLoader loader = new IpaacaFaceAndBodyEmbodimentLoader();
        XMLTokenizer tok = new XMLTokenizer(str);
        tok.takeSTag();
        loader.readXML(tok, "id1", "id1", "id1", ImmutableList.of(env).toArray(new Environment[0]), mockEmbodimentLoader);        
    }
}
