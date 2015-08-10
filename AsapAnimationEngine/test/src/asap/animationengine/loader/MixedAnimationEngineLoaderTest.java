/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.loader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hmi.animationembodiments.MixedSkeletonEmbodiment;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.math.Vec3f;
import hmi.mixedanimationenvironment.MixedAnimationEnvironment;
import hmi.physics.PhysicalHumanoid;
import hmi.physics.PhysicalSegment;
import hmi.physics.mixed.MixedSystem;
import hmi.physicsenvironment.MixedSkeletonEmbodimentLoader;
import hmi.physicsenvironment.OdePhysicalEmbodiment;
import hmi.testutil.animation.HanimBody;
import hmi.worldobjectenvironment.WorldObjectEnvironment;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import asap.realizerembodiments.AsapRealizerEmbodiment;

/**
 * Unit tests for the MixedAnimationEngineLoader
 * @author hvanwelbergen
 */
public class MixedAnimationEngineLoaderTest
{
    private OdePhysicalEmbodiment mockPhEmbodiment = mock(OdePhysicalEmbodiment.class);
    private MixedSkeletonEmbodimentLoader mockMixedSkeletonEmbodimentLoader = mock(MixedSkeletonEmbodimentLoader.class);
    private MixedSkeletonEmbodiment mockMixedSkeletonEmbodiment = mock(MixedSkeletonEmbodiment.class);
    private PhysicalHumanoid mockPhuman = mock(PhysicalHumanoid.class);
    private AsapRealizerEmbodiment mockAsapRealizerEmbodiment = mock(AsapRealizerEmbodiment.class);
    private MixedAnimationEnvironment mockMixedAnimationEnvironment = mock(MixedAnimationEnvironment.class);
    private MixedAnimationEngineLoader loader = new MixedAnimationEngineLoader();
    private Loader[] reqLoaders = new Loader[3];
    private Environment reqEnvironments[] = new Environment[2];

    @Before
    public void setup()
    {
        when(mockPhuman.getSegments()).thenReturn(new PhysicalSegment[0]);
        ArrayList<MixedSystem> ms = new ArrayList<>();
        MixedSystem ms1 = new MixedSystem(Vec3f.getVec3f(0, -9.8f, 0), mockPhuman);
        ms1.setup();
        ms.add(ms1);

        when(mockPhEmbodiment.getId()).thenReturn("physicalembodiment");
        when(mockPhEmbodiment.getEmbodiment()).thenReturn(mockPhEmbodiment);
        when(mockPhEmbodiment.getMixedSystems()).thenReturn(ms);
        when(mockMixedSkeletonEmbodimentLoader.getId()).thenReturn("mixedskeletonembodiment");
        when(mockMixedSkeletonEmbodimentLoader.getEmbodiment()).thenReturn(mockMixedSkeletonEmbodiment);
        when(mockMixedSkeletonEmbodiment.getCurrentVJoint()).thenReturn(HanimBody.getLOA1HanimBody());
        when(mockMixedSkeletonEmbodiment.getNextVJoint()).thenReturn(HanimBody.getLOA1HanimBody());
        when(mockMixedSkeletonEmbodiment.getPreviousVJoint()).thenReturn(HanimBody.getLOA1HanimBody());
        when(mockAsapRealizerEmbodiment.getEmbodiment()).thenReturn(mockAsapRealizerEmbodiment);

        reqLoaders[0] = mockMixedSkeletonEmbodimentLoader;
        reqLoaders[1] = mockPhEmbodiment;
        reqLoaders[2] = mockAsapRealizerEmbodiment;

        reqEnvironments[0] = new WorldObjectEnvironment();
        reqEnvironments[1] = mockMixedAnimationEnvironment;
    }

    @Test
    public void test() throws IOException
    {
        //@formatter:off
        String loaderStr =
          "<Loader id=\"animationengine\""+ 
                "loader=\"asap.animationengine.loader.MixedAnimationEngineLoader\""+
                "requiredloaders=\"mixedskeletonembodiment,physicalembodiment\">"+
                "<StartPose>"+
                "<RestPose type=\"SkeletonPose\" file=\"Humanoids/armandia/restposes/looselyhangingarms.xml\"/>"+
                "</StartPose>"+
                "<GestureBinding basedir=\"\" resources=\"Humanoids/armandia/gesturebinding/\" filename=\"gesturebinding.xml\"/>"+                
          "</Loader>";          
        //@formatter:on
        XMLTokenizer tok = new XMLTokenizer(loaderStr);
        tok.takeSTag();
        loader.readXML(tok, "ma1", "billie", "billie", reqEnvironments, reqLoaders);
        assertEquals("ma1", loader.getId());
        assertNotNull(loader.getEngine());
    }

    @Test
    public void testWithHns() throws IOException
    {
        //@formatter:off
        String loaderStr =
          "<Loader id=\"animationengine\""+ 
                "loader=\"asap.animationengine.loader.MixedAnimationEngineLoader\""+
                "requiredloaders=\"mixedskeletonembodiment,physicalembodiment\">"+
            "<GestureBinding basedir=\"\" resources=\"Humanoids/armandia/gesturebinding/\" filename=\"gesturebinding.xml\"/>"+
            "<StartPose>"+
            "<RestPose type=\"SkeletonPose\" file=\"Humanoids/armandia/restposes/looselyhangingarms.xml\"/>"+
            "</StartPose>"+
            "<Hns resources=\"Humanoids/shared/hns\" filename=\"hns.xml\"/>"+
          "</Loader>";          
        //@formatter:on
        XMLTokenizer tok = new XMLTokenizer(loaderStr);
        tok.takeSTag();
        loader.readXML(tok, "ma1", "billie", "billie", reqEnvironments, reqLoaders);
        assertEquals("ma1", loader.getId());
        assertNotNull(loader.getEngine());
    }

    @Test
    public void testWithHnsHandshape() throws IOException
    {
        //@formatter:off
        String loaderStr =
          "<Loader id=\"animationengine\""+ 
                "loader=\"asap.animationengine.loader.MixedAnimationEngineLoader\""+
                "requiredloaders=\"mixedskeletonembodiment,physicalembodiment\">"+
            "<GestureBinding basedir=\"\" resources=\"Humanoids/armandia/gesturebinding/\" filename=\"gesturebinding.xml\"/>"+
            "<StartPose>"+
            "<RestPose type=\"SkeletonPose\" file=\"Humanoids/armandia/restposes/looselyhangingarms.xml\"/>"+
            "</StartPose>"+
            "<Hns resources=\"Humanoids/shared/hns\" filename=\"hns.xml\"/>"+
            "<HnsHandShape dir=\"Humanoids/shared/handshapes\" />"+
          "</Loader>";          
        //@formatter:on
        XMLTokenizer tok = new XMLTokenizer(loaderStr);
        tok.takeSTag();
        loader.readXML(tok, "ma1", "billie", "billie", reqEnvironments, reqLoaders);
        assertEquals("ma1", loader.getId());
        assertNotNull(loader.getEngine());
    }
}
