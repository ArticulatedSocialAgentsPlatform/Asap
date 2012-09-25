package asap.ipaacaembodiments.xmlhumanoid;

import static hmi.testutil.math.Vec3fTestUtil.assertVec3fEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import hmi.util.Resources;

import org.junit.Test;

/**
 * Unit tests for the ACE XMLHumanoid parser
 * @author hvanwelbergen
 *
 */
public class XMLHumanoidTest
{
    private static final float PRECISION = 0.001f;
    @Test
    public void test()
    {
        //@formatter:off
        String str =
        "<Humanoid name=\"Billie\" version=\"1.0\">"+
        "  <Limbs>"+
        "    <Limb name=\"Torso\"      type=\"Torso_Limb\"  start_joint=\"HumanoidRoot\"   end_joint=\"vc1\"   prefix=\"t_\"    is_start_limb=\"1\"/>"+
        "    <Limb name=\"RLeg\"       type=\"Leg_Limb\"    start_joint=\"rl_hip\"      end_joint=\"rl_toe2\" prefix=\"rl_\"   is_start_limb=\"0\"/>"+
        "    <Limb name=\"LLeg\"       type=\"Leg_Limb\"    start_joint=\"ll_hip\"      end_joint=\"ll_toe2\" prefix=\"ll_\"   is_start_limb=\"0\"/>"+
        "    <Limb name=\"LArm\"       type=\"Arm_Limb\"    start_joint=\"l_clavicle\"     end_joint=\"l_wrist\" prefix=\"l_\"   is_start_limb=\"0\"/>" +
        "  </Limbs>"+
        "  <Joint name=\"HumanoidRoot\" alias=\"BipKevin\" type=\"Euler_JointXYZ\" num_childs=\"1\">"+ 
        "    <Axis1 angle=\"0\" llimit=\"-180\"   ulimit=\"180\"/>"+
        "    <Axis2 angle=\"0\" llimit=\"-180\"   ulimit=\"180\"/>"+
        "    <Axis3 angle=\"0\" llimit=\"-180\"   ulimit=\"180\"/>"+
        "    <Segment name=\"\" type=\"Euler_Link\" index=\"0\"  translation=\"-0.00614319 0 7.71492\">"+
        "      <Joint name=\"vl1\" alias=\"BipKevin Spine1\" type=\"Euler_JointXYZ\" num_childs=\"1\">"+
        "        <Axis1 angle=\"0\" llimit=\"-180\"   ulimit=\"180\"/>"+
        "        <Axis2 angle=\"0\" llimit=\"-180\"   ulimit=\"180\"/>"+
        "        <Axis3 angle=\"0\" llimit=\"-180\"   ulimit=\"180\"/>"+
        "        <Segment name=\"\" type=\"Euler_Link\" index=\"0\"  translation=\"-0.0061432 0 7.71493\">"+
        "          <Joint name=\"vt6\" alias=\"BipKevin Spine2\" type=\"Euler_JointXYZ\" num_childs=\"1\">"+
        "            <Axis1 angle=\"0\" llimit=\"-180\"   ulimit=\"180\"/>"+
        "            <Axis2 angle=\"0\" llimit=\"-180\"   ulimit=\"180\"/>"+
        "            <Axis3 angle=\"0\" llimit=\"-180\"   ulimit=\"180\"/>"+
        "            <Segment name=\"\" type=\"Euler_Link\" index=\"0\"  translation=\"-0.00614319 0 7.71492\">"+        
        "              <Joint name=\"vc7\" alias=\"BipKevin Spine3\" type=\"Euler_JointXYZ\" num_childs=\"1\">"+
        "                <Axis1 angle=\"1\" llimit=\"-180\"   ulimit=\"180\"/>"+
        "                <Axis2 angle=\"2\" llimit=\"-170\"   ulimit=\"170\"/>"+
        "                <Axis3 angle=\"3\" llimit=\"-160\"   ulimit=\"160\"/>"+
        "                <Segment name=\"\" type=\"Euler_Link\" index=\"0\"  translation=\"-1.09271 0 7.71405\">"+
        "                  <Joint name=\"vc1\" alias=\"BipKevin Neck\" type=\"Euler_JointXYZ\" num_childs=\"0\">"+
        "                    <Axis1 angle=\"0\" llimit=\"-180\"   ulimit=\"180\"/>"+
        "                    <Axis2 angle=\"0\" llimit=\"-180\"   ulimit=\"180\"/>"+
        "                    <Axis3 angle=\"0\" llimit=\"-180\"   ulimit=\"180\"/>"+
        "                  </Joint>"+
        "                </Segment>"+
        "              </Joint>"+
        "            </Segment>"+
        "          </Joint>"+
        "        </Segment> "+
        "      </Joint>"+
        "    </Segment>"+
        "  </Joint>"+        
        "</Humanoid>";
        //@formatter:on
        XMLHumanoid humanoid = new XMLHumanoid();
        humanoid.readXML(str);
        Joint vc7 = humanoid.getJoint("vc7");
        assertNotNull(vc7);
        assertEquals("BipKevin Spine3",vc7.getAlias());
        assertEquals("Euler_JointXYZ",vc7.getType());
        assertVec3fEquals((float)Math.toRadians(1f),(float)Math.toRadians(2f),(float)Math.toRadians(3f),vc7.getAngles(), PRECISION);
        assertVec3fEquals((float)Math.toRadians(-180f),(float)Math.toRadians(-170f),(float)Math.toRadians(-160f),vc7.getLlimits(), PRECISION);
        assertVec3fEquals((float)Math.toRadians(180f),(float)Math.toRadians(170f),(float)Math.toRadians(160f),vc7.getUlimits(), PRECISION);        
        
        Joint vc7_2 = humanoid.getJoint("BipKevin Spine3");
        assertEquals(vc7,vc7_2);
    }
    
    @Test
    public void testBillie() throws IOException
    {
        XMLHumanoid humanoid = new XMLHumanoid();
        humanoid.readXML(new Resources("").getReader("billie_skeleton.xml"));
        Joint rHip = humanoid.getJoint("rl_hip");
        assertVec3fEquals((float)Math.toRadians(177.24f),(float)Math.toRadians(2.30484f),(float)Math.toRadians(0.647487f),rHip.getAngles(), PRECISION);
    }
}
