package asap.animationengine.mixed;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import hmi.animation.Hanim;
import hmi.animation.VJoint;
import hmi.physics.PhysicalHumanoid;
import hmi.physics.assembler.PhysicalHumanoidAssembler;
import hmi.physics.mixed.MixedSystem;
import hmi.physics.ode.OdeHumanoid;
import hmi.testutil.animation.HanimBody;
import hmi.util.Resources;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.odejava.HashSpace;
import org.odejava.Odejava;
import org.odejava.World;

import asap.animationengine.mixed.MixedSystemGenerator;

/**
 * Unit test cases for the MixedSystemGenerator
 * @author welberge
 *
 */
public class MixedSystemGeneratorTest
{
    private PhysicalHumanoid fullBodyPh;
    private MixedSystemGenerator mixedSystemGenerator;
    private VJoint human;
    
    @Before
    public void setup() throws IOException
    {
        Odejava.init();        
        fullBodyPh = new OdeHumanoid("Armandia", new World(), new HashSpace());
        human = HanimBody.getLOA1HanimBody();
        PhysicalHumanoidAssembler pha = new PhysicalHumanoidAssembler(human, fullBodyPh);
        Resources res = new Resources("");
        pha.readXML(res.getReader("mixedsystemtest/armandia_ph.xml"));
        float g[]={0,-9.8f,0};
        mixedSystemGenerator = new MixedSystemGenerator(fullBodyPh,g);
    }

    @After
    public void tearDown()
    {
        Odejava.close();
    }
    
    @Test
    public void test()
    {
        List<String> requiredJoints = new ArrayList<String>();
        requiredJoints.add(Hanim.r_hip);
        requiredJoints.add(Hanim.r_knee);
        requiredJoints.add(Hanim.r_ankle);
        requiredJoints.add(Hanim.l_hip);
        requiredJoints.add(Hanim.l_knee);
        requiredJoints.add(Hanim.l_ankle);
        List<String> desiredJoints = new ArrayList<String>();
        MixedSystem ms = mixedSystemGenerator.generateMixedSystem("Ph1", requiredJoints, desiredJoints, human);
        assertEquals(7,ms.getPHuman().getSegments().length);    //6+root
        assertNotNull(ms.getPHuman().getSegment(Hanim.HumanoidRoot));
        assertNotNull(ms.getPHuman().getSegment(Hanim.r_hip));
        assertNotNull(ms.getPHuman().getSegment(Hanim.r_knee));
        assertNotNull(ms.getPHuman().getSegment(Hanim.r_ankle));
        assertNotNull(ms.getPHuman().getSegment(Hanim.l_hip));
        assertNotNull(ms.getPHuman().getSegment(Hanim.l_knee));
        assertNotNull(ms.getPHuman().getSegment(Hanim.l_ankle));
    }
}
