/*******************************************************************************
 *******************************************************************************/
package asap.ipaacaworldenvironment;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import hmi.animation.VJoint;
import hmi.math.Vec3f;
import hmi.testutil.math.Vec3fTestUtil;
import hmi.worldobjectenvironment.VJointWorldObject;
import hmi.worldobjectenvironment.WorldObject;
import hmi.worldobjectenvironment.WorldObjectManager;
import ipaaca.LocalIU;
import ipaaca.OutputBuffer;

import org.junit.Test;

/**
 * Unit tests for the IpaacaWorldEnvironment
 * @author hvanwelbergen
 *
 */
public class IpaacaWorldEnvironmentTest
{
    private OutputBuffer outBuffer = new OutputBuffer("test");
    private static final float PRECISION = 0.0001f;
    
    @Test
    public void test() throws InterruptedException
    {
        WorldObjectManager mockWoManager = mock(WorldObjectManager.class);
        new IpaacaWorldEnvironment(mockWoManager);
        
        LocalIU iuReport = new LocalIU();
        iuReport.setCategory("sceneinfo");
        iuReport.getPayload().put("cmd","reporting");
        iuReport.getPayload().put("data", "(camera 0.12 0.12 0)(ent 1 0 0 2)");
        outBuffer.add(iuReport);        
        Thread.sleep(500);
        
        verify(mockWoManager).addWorldObject(eq("camera"), any(WorldObject.class));
        verify(mockWoManager).addWorldObject(eq("ent 1"), any(WorldObject.class));
    } 
    
    @Test
    public void testPos()throws InterruptedException
    {
        WorldObjectManager woManager = new WorldObjectManager();
        new IpaacaWorldEnvironment(woManager);
        
        LocalIU iuReport = new LocalIU();
        iuReport.setCategory("sceneinfo");
        iuReport.getPayload().put("cmd","reporting");
        iuReport.getPayload().put("data", "(camera 0.12 0.12 0)(ent 1 0 0 2)");
        outBuffer.add(iuReport);        
        Thread.sleep(500);
        
        float tr[] = Vec3f.getVec3f();
        woManager.getWorldObject("ent 1").getWorldTranslation(tr);
        Vec3fTestUtil.assertVec3fEquals(new float[]{0f,0f,2f},tr, PRECISION);
        
        woManager.getWorldObject("camera").getWorldTranslation(tr);
        Vec3fTestUtil.assertVec3fEquals(new float[]{0.12f,0.12f,0f},tr, PRECISION);
    }
    
    @Test
    public void testPosExisting() throws InterruptedException
    {
        WorldObjectManager woManager = new WorldObjectManager();
        WorldObject ent1 = new VJointWorldObject(new VJoint("ent1"));
        woManager.addWorldObject("ent 1", ent1);
        
        new IpaacaWorldEnvironment(woManager);
        LocalIU iuReport = new LocalIU();
        iuReport.setCategory("sceneinfo");
        iuReport.getPayload().put("cmd","reporting");
        iuReport.getPayload().put("data", "(camera 0.12 0.12 0)(ent 1 0.1 0.2 2)");
        outBuffer.add(iuReport);        
        Thread.sleep(500);
        
        float tr[] = Vec3f.getVec3f();
        ent1.getWorldTranslation(tr);
        Vec3fTestUtil.assertVec3fEquals(new float[]{0.1f,0.2f,2f},tr, PRECISION);
    }
}
