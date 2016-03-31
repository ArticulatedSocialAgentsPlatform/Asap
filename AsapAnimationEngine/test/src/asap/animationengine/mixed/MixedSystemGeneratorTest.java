/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.mixed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import hmi.animation.Hanim;
import hmi.animation.VJoint;
import hmi.physics.PhysicalHumanoid;
import hmi.physics.assembler.PhysicalHumanoidAssembler;
import hmi.physics.mixed.Branch;
import hmi.physics.mixed.MixedSystem;
import hmi.physics.ode.OdeHumanoid;
import hmi.testutil.animation.HanimBody;
import hmi.util.Resources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.odejava.HashSpace;
import org.odejava.Odejava;
import org.odejava.World;

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
    
    private List<String> requiredJoints = new ArrayList<String>();
    private List<String> desiredJoints = new ArrayList<String>();
    
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
    public void testEmpty()
    {
        MixedSystem ms = mixedSystemGenerator.generateMixedSystem("Ph1", requiredJoints, desiredJoints, human);
        assertEquals(0,ms.getBranches().size());
        assertEquals(0,ms.getPHuman().getSegments().length);
    }
    
    @Test
    public void test()
    {
        requiredJoints.add(Hanim.r_hip);
        requiredJoints.add(Hanim.r_knee);
        requiredJoints.add(Hanim.r_ankle);
        requiredJoints.add(Hanim.l_hip);
        requiredJoints.add(Hanim.l_knee);
        requiredJoints.add(Hanim.l_ankle);
        MixedSystem ms = mixedSystemGenerator.generateMixedSystem("Ph1", requiredJoints, desiredJoints, human);
        assertEquals(7,ms.getPHuman().getSegments().length);    //6+root
        assertNotNull(ms.getPHuman().getSegment(Hanim.HumanoidRoot));
        assertNotNull(ms.getPHuman().getSegment(Hanim.r_hip));
        assertNotNull(ms.getPHuman().getSegment(Hanim.r_knee));
        assertNotNull(ms.getPHuman().getSegment(Hanim.r_ankle));
        assertNotNull(ms.getPHuman().getSegment(Hanim.l_hip));
        assertNotNull(ms.getPHuman().getSegment(Hanim.l_knee));
        assertNotNull(ms.getPHuman().getSegment(Hanim.l_ankle));
        
        assertEquals(3,ms.getBranches().size());
    }
    
    @Test
    public void testTree()
    {
        requiredJoints.add(Hanim.r_hip);
        requiredJoints.add(Hanim.r_ankle);
        MixedSystem ms = mixedSystemGenerator.generateMixedSystem("Ph1", requiredJoints, desiredJoints, human);
        assertEquals(4,ms.getPHuman().getSegments().length);    //3+root
        assertNotNull(ms.getPHuman().getSegment(Hanim.r_hip));
        assertNotNull(ms.getPHuman().getSegment(Hanim.r_knee));
        assertNotNull(ms.getPHuman().getSegment(Hanim.r_ankle));        
    }
    
    @Test 
    public void testIDChain()
    {
        requiredJoints.add(Hanim.r_ankle);
        requiredJoints.add(Hanim.l_ankle);
        requiredJoints.add(Hanim.r_wrist);
        requiredJoints.add(Hanim.skullbase);
        MixedSystem ms = mixedSystemGenerator.generateMixedSystem("Ph1", requiredJoints, desiredJoints, human);
        assertEquals(1,ms.getBranches().size());
        Branch ib = ms.getBranches().get(0);
        assertEquals(Hanim.l_shoulder,ib.idBranch.getRoot().name);
        assertEquals(1,ib.idBranch.getRoot().getChildren().size());
        assertEquals(Hanim.l_elbow,ib.idBranch.getRoot().getChildren().get(0).name);
        assertEquals(Hanim.l_wrist,ib.idBranch.getRoot().getChildren().get(0).getChildren().get(0).name);
    }
}
