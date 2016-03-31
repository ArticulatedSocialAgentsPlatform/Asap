package asap.rsbworldenvironment;

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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import rsb.Factory;
import rsb.Informer;
import rsb.InitializeException;
import rsb.RSBException;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import asap.rsbworldenvironment.Rsbworldenvironment.RSBWorldObject;
import asap.rsbworldenvironment.Rsbworldenvironment.RSBWorldObjects;

import com.google.common.primitives.Floats;

/**
 * Unit tests for the RsbWorldEnvironment
 * @author hvanwelbergen
 *
 */
public class RsbWorldEnvironmentTest
{
    private WorldObjectManager mockWoManager = mock(WorldObjectManager.class);
    private WorldObjectManager woManager = new WorldObjectManager();
    private Factory factory = Factory.getInstance();
    private Informer<RSBWorldObjects> informer;
    private static final float PRECISION = 0.0001f;
    
    @Before
    public void before() throws InitializeException
    {
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(RSBWorldObjects.getDefaultInstance()));
        informer = factory.createInformer(RsbWorldEnvironment.SCENEINFO_SCOPE);
        informer.activate();
    }
    
    @After
    public void after() throws RSBException, InterruptedException
    {
        informer.deactivate();
    }
    
    @Test
    public void test() throws InterruptedException, RSBException
    {
        new RsbWorldEnvironment(mockWoManager);
        informer.send(RSBWorldObjects.newBuilder()
                     .addWorldObjects(RSBWorldObject.newBuilder().setObjectId("camera").addAllPosition(Floats.asList(1,1,1)).build())
                     .addWorldObjects(RSBWorldObject.newBuilder().setObjectId("ent 2").addAllPosition(Floats.asList(1,1,1)).build())
                     .build());
        Thread.sleep(500);
        verify(mockWoManager).addWorldObject(eq("camera"), any(WorldObject.class));
        verify(mockWoManager).addWorldObject(eq("ent 2"), any(WorldObject.class));
    }
    
    @Test
    public void testPos()throws InterruptedException, RSBException
    {
        new RsbWorldEnvironment(woManager);
        informer.send(RSBWorldObjects.newBuilder()
                .addWorldObjects(RSBWorldObject.newBuilder().setObjectId("camera").addAllPosition(Floats.asList(0.12f, 0.12f, 0f)).build())
                .addWorldObjects(RSBWorldObject.newBuilder().setObjectId("ent 2").addAllPosition(Floats.asList(0,0,2)).build())
                .build());
        Thread.sleep(500);
        float tr[] = Vec3f.getVec3f();
        woManager.getWorldObject("ent 2").getWorldTranslation(tr);
        Vec3fTestUtil.assertVec3fEquals(new float[]{0f,0f,2f},tr, PRECISION);
        
        woManager.getWorldObject("camera").getWorldTranslation(tr);
        Vec3fTestUtil.assertVec3fEquals(new float[]{0.12f, 0.12f, 0f},tr, PRECISION);
    }
    
    @Test
    public void testPosExisting() throws InterruptedException, RSBException
    {
        WorldObject ent1 = new VJointWorldObject(new VJoint("ent1"));
        woManager.addWorldObject("ent 1", ent1);
        new RsbWorldEnvironment(woManager);
        
        informer.send(RSBWorldObjects.newBuilder()
                .addWorldObjects(RSBWorldObject.newBuilder().setObjectId("ent 1").addAllPosition(Floats.asList(0.1f,0.2f,2f)).build())
                .addWorldObjects(RSBWorldObject.newBuilder().setObjectId("ent 2").addAllPosition(Floats.asList(0,0,2)).build())
                .build());
        
        Thread.sleep(500);        
        float tr[] = Vec3f.getVec3f();
        ent1.getWorldTranslation(tr);
        Vec3fTestUtil.assertVec3fEquals(new float[]{0.1f,0.2f,2f},tr, PRECISION);
    }
}
