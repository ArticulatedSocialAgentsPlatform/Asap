/*******************************************************************************
 *******************************************************************************/
package asap.realizer.parametervaluechange;

import static asap.realizertestutil.util.TimePegUtil.createTimePeg;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import saiba.bml.parser.Constraint;
import asap.bml.ext.bmla.BMLAInfo;
import asap.bml.ext.bmla.BMLAParameterValueChangeBehaviour;
import asap.realizer.BehaviorNotFoundException;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.SyncAndTimePeg;
import asap.realizer.SyncPointNotFoundException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.feedback.FeedbackManagerImpl;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.ParameterException;
import asap.realizer.planunit.PlanManager;
import asap.realizer.planunit.PlanUnitFloatParameterNotFoundException;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizer.scheduler.BMLScheduler;
import asap.realizer.scheduler.TimePegAndConstraint;
import asap.realizertestutil.PlannerTests;

import com.google.common.collect.ImmutableSet;

/**
 * Unit testcases for the ParameterValueChangePlanner
 * @author Herwin
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({BMLScheduler.class,BMLBlockManager.class})
public class ParameterValueChangePlannerTest
{
    private static final double TIMING_PRECISION = 0.0001;
    private BMLScheduler mockScheduler = mock(BMLScheduler.class);
    private ParameterValueChangePlanner pvcp;
    private BMLBlockManager mockBmlBlockManager = mock(BMLBlockManager.class);
    private FeedbackManager fbManager = new FeedbackManagerImpl(mockBmlBlockManager,"character1");
    private PlanManager<TimedParameterValueChangeUnit> planManager = new PlanManager<TimedParameterValueChangeUnit>();
    private PlannerTests<TimedParameterValueChangeUnit> plannerTests;
    
    
    private BMLAParameterValueChangeBehaviour createBehavior(String paramId, String target, float initialValue, float targetValue,
            String type, String other) throws IOException
    {
        String str = "<parametervaluechange xmlns=\""+BMLAInfo.BMLA_NAMESPACE+"\" paramId=\"" + paramId + "\"" + " target=\"" + target
                + "\" " +other+ " id=\"paramchange1\">" + "<trajectory  initialValue=\"" + initialValue + "\"" + " targetValue=\"" + targetValue
                + "\"" + " type=\"" + type + "\"/>" + "</parametervaluechange>";
        return new BMLAParameterValueChangeBehaviour("bml1", new XMLTokenizer(str));
    }

    private BMLAParameterValueChangeBehaviour createBehavior(String paramId, String target, float initialValue, float targetValue,
            String type) throws IOException
    {
        return createBehavior(paramId, target, initialValue, targetValue,type,"");
    }
    
    @Before
    public void setup()
    {
        pvcp = new ParameterValueChangePlanner(fbManager, mockScheduler, new TrajectoryBinding(), planManager);
        plannerTests = new PlannerTests<TimedParameterValueChangeUnit>(pvcp,BMLBlockPeg.GLOBALPEG);
    }

    @Test
    public void testResolveUnsetStart() throws BehaviourPlanningException, IOException
    {
        plannerTests.testResolveUnsetStart(createBehavior("param1", "bml1:beh1", 0, 100, "linear"));
    }

    @Test(expected=BehaviourPlanningException.class)
    public void testResolveNonExistingSync() throws IOException, BehaviourPlanningException
    {
        plannerTests.testResolveNonExistingSync(createBehavior("param1", "bml1:beh1", 0, 100, "linear"));
    }

    @Test
    public void testResolveStartOffset() throws IOException, BehaviourPlanningException
    {
        plannerTests.testResolveStartOffset(createBehavior("param1", "bml1:beh1", 0, 100, "linear"));
    }

    @Test
    public void testResolve() throws BehaviourPlanningException, SyncPointNotFoundException, TimedPlanUnitPlayException,
            ParameterException, BehaviorNotFoundException, IOException
    {
        BMLAParameterValueChangeBehaviour beh = createBehavior("param1", "bml1:beh1", 0, 100, "linear");
        ArrayList<TimePegAndConstraint> sac = new ArrayList<TimePegAndConstraint>();
        sac.add(new TimePegAndConstraint("start", createTimePeg(1), new Constraint(), 0));
        sac.add(new TimePegAndConstraint("end", createTimePeg(2), new Constraint(), 0));
        TimedParameterValueChangeUnit tpu = pvcp.resolveSynchs(BMLBlockPeg.GLOBALPEG, beh, sac);

        assertEquals("paramchange1", tpu.getId());
        assertEquals("bml1", tpu.getBMLId());

        assertEquals(1, tpu.getStartTime(), TIMING_PRECISION);
        assertEquals(2, tpu.getTime("stroke"), TIMING_PRECISION);
        when(mockBmlBlockManager.getSyncsPassed("bml1", "beh1")).thenReturn(new ImmutableSet.Builder<String>().build());

        tpu.playUnit(1.5f);
        verify(mockScheduler, times(1)).setParameterValue("bml1", "beh1", "param1", 50f);
    }

    @Test(expected = BehaviourPlanningException.class)
    public void testResolveInvalidType() throws BehaviourPlanningException, SyncPointNotFoundException, TimedPlanUnitPlayException,
            ParameterException, BehaviorNotFoundException, IOException
    {
        BMLAParameterValueChangeBehaviour beh = createBehavior("param1", "bml1:beh1", 0, 100, "invalidtraj");
        ArrayList<TimePegAndConstraint> sac = new ArrayList<TimePegAndConstraint>();

        sac.add(new TimePegAndConstraint("start", createTimePeg(1), new Constraint(), 0));
        pvcp.resolveSynchs(BMLBlockPeg.GLOBALPEG, beh, sac);
    }

    @Test(expected = BehaviourPlanningException.class)
    public void testResolveInvalidSyncs() throws BehaviourPlanningException, SyncPointNotFoundException, TimedPlanUnitPlayException,
            PlanUnitFloatParameterNotFoundException, BehaviorNotFoundException, IOException
    {
        BMLAParameterValueChangeBehaviour beh = createBehavior("param1", "bml1:beh1", 0, 100, "invalidtraj");
        ArrayList<TimePegAndConstraint> sac = new ArrayList<TimePegAndConstraint>();
        sac.add(new TimePegAndConstraint("ready", createTimePeg(1), new Constraint(), 0));
        pvcp.resolveSynchs(BMLBlockPeg.GLOBALPEG, beh, sac);
    }

    @Test
    public void testAdd() throws BehaviourPlanningException, IOException
    {
        BMLAParameterValueChangeBehaviour beh = createBehavior("param1", "bml1:beh1", 0, 100, "linear");
        ArrayList<TimePegAndConstraint> sac = new ArrayList<TimePegAndConstraint>();

        TimePeg tpStart = createTimePeg(1);
        sac.add(new TimePegAndConstraint("start", tpStart, new Constraint(), 0));
        TimedParameterValueChangeUnit tpu = pvcp.resolveSynchs(BMLBlockPeg.GLOBALPEG, beh, sac);

        List<SyncAndTimePeg> satp = pvcp.addBehaviour(BMLBlockPeg.GLOBALPEG, beh, sac, tpu);
        assertEquals(2, satp.size());
        assertEquals("start", satp.get(0).sync);
        assertEquals("end", satp.get(1).sync);
        assertEquals("bml1", satp.get(0).bmlId);
        assertEquals("bml1", satp.get(1).bmlId);
        assertEquals(tpStart, satp.get(0).peg);
        assertEquals(tpStart, satp.get(1).peg);

        assertThat(planManager.getBehaviours("bml1"), IsIterableContainingInOrder.contains("paramchange1"));
    }
    
    @Test
    public void testAdd2Constraints() throws BehaviourPlanningException, IOException
    {
        BMLAParameterValueChangeBehaviour beh = createBehavior("param1", "bml1:beh1", 0, 100, "linear");
        ArrayList<TimePegAndConstraint> sac = new ArrayList<TimePegAndConstraint>();

        TimePeg tpStart = createTimePeg(1);
        TimePeg tpEnd = createTimePeg(2);
        sac.add(new TimePegAndConstraint("start", tpStart, new Constraint(), 0));
        sac.add(new TimePegAndConstraint("end", tpEnd, new Constraint(), 0));
        TimedParameterValueChangeUnit tpu = pvcp.resolveSynchs(BMLBlockPeg.GLOBALPEG, beh, sac);

        List<SyncAndTimePeg> satp = pvcp.addBehaviour(BMLBlockPeg.GLOBALPEG, beh, sac, tpu);
        assertEquals(2, satp.size());
        assertEquals("start", satp.get(0).sync);
        assertEquals("end", satp.get(1).sync);
        assertEquals("bml1", satp.get(0).bmlId);
        assertEquals("bml1", satp.get(1).bmlId);
        assertEquals(tpStart, satp.get(0).peg);
        assertEquals(tpEnd, satp.get(1).peg);

        assertThat(planManager.getBehaviours("bml1"), IsIterableContainingInOrder.contains("paramchange1"));
    }
}
