/*******************************************************************************
 *******************************************************************************/
package asap.picture;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import saiba.bml.core.Behaviour;
import saiba.bml.parser.Constraint;
import saiba.utils.TestUtil;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.SyncAndTimePeg;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.feedback.FeedbackManagerImpl;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.PlanManager;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizer.scheduler.TimePegAndConstraint;
import asap.realizertestutil.PlannerTests;
import asap.srnao.PicturePlanner;
import asap.srnao.bml.SetImageBehavior;
import asap.srnao.naobinding.NaoBinding;
import asap.srnao.planunit.NaoUnit;
import asap.srnao.planunit.TimedNaoUnit;

/**
 * Unit tests for the PicturePlanner
 * @author hvanwelbergen
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ BMLBlockManager.class })
public class PicturePlannerTest
{
    private PlannerTests<TimedNaoUnit> plannerTests;
    private static final String BMLID = "bml1";
    private BMLBlockManager mockBmlBlockManager = mock(BMLBlockManager.class);
    private FeedbackManager fbManager = new FeedbackManagerImpl(mockBmlBlockManager, "character1");
    private NaoBinding mockPictureBinding = mock(NaoBinding.class);
    private final PlanManager<TimedNaoUnit> planManager = new PlanManager<TimedNaoUnit>();
    private PicturePlanner picturePlanner;
    private final BMLBlockPeg bbPeg = new BMLBlockPeg("Peg1", 0.3);
    private NaoUnit stubPU = new StubPictureUnit();
    private static final float PLAN_PRECISION = 0.00001f;
    
    @Before
    public void setup()
    {
        picturePlanner = new PicturePlanner(fbManager, mockPictureBinding, planManager);
        plannerTests = new PlannerTests<TimedNaoUnit>(picturePlanner, bbPeg);
        final List<TimedNaoUnit> tmus = new ArrayList<TimedNaoUnit>();
        tmus.add(new TimedNaoUnit(fbManager, bbPeg, BMLID, "beh1", stubPU));
        when(mockPictureBinding.getNaoUnit(any(FeedbackManager.class), any(BMLBlockPeg.class), any(Behaviour.class))).thenReturn(tmus);        
    }
    
    public SetImageBehavior createSetImageBehaviorBehaviour() throws IOException
    {
        String str = "<bmlp:setImage xmlns:bmlp=\"http://hmi.ewi.utwente.nl/pictureengine\" " + "filePath=\"\" fileName=\"\""
                + TestUtil.getDefNS() + " id=\"beh1\"/>";
        return new SetImageBehavior(BMLID, new XMLTokenizer(str));
    }
    
    @Test
    public void testResolveUnsetStart() throws BehaviourPlanningException, IOException
    {
        plannerTests.testResolveUnsetStart(createSetImageBehaviorBehaviour());
    }
    
    @Test(expected = BehaviourPlanningException.class)
    public void testResolveNonExistingSync() throws IOException, BehaviourPlanningException
    {
        plannerTests.testResolveNonExistingSync(createSetImageBehaviorBehaviour());
    }

    @Test
    public void testResolveStartOffset() throws IOException, BehaviourPlanningException
    {
        plannerTests.testResolveStartOffset(createSetImageBehaviorBehaviour());
    }
    
    @Test
    public void testAdd() throws IOException, BehaviourPlanningException
    {
        SetImageBehavior beh = createSetImageBehaviorBehaviour();
        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        TimedNaoUnit tpu = picturePlanner.resolveSynchs(bbPeg, beh, sacs); 
        List<SyncAndTimePeg> syncAndPegs = picturePlanner.addBehaviour(BMLBlockPeg.GLOBALPEG, beh, sacs, tpu);
        assertEquals(2, syncAndPegs.size());
        assertEquals("start", syncAndPegs.get(0).sync);
        assertEquals("end", syncAndPegs.get(1).sync);
        assertEquals(TimePeg.VALUE_UNKNOWN, syncAndPegs.get(0).peg.getGlobalValue(), PLAN_PRECISION);
        assertEquals(TimePeg.VALUE_UNKNOWN, syncAndPegs.get(1).peg.getGlobalValue(), PLAN_PRECISION);
    }
    
    @Test
    public void testAddWithStartConstraint() throws IOException, BehaviourPlanningException
    {
        SetImageBehavior beh = createSetImageBehaviorBehaviour();
        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        sacs.add(new TimePegAndConstraint("start", new TimePeg(BMLBlockPeg.GLOBALPEG), new Constraint(), 0));
        TimedNaoUnit tpu = picturePlanner.resolveSynchs(bbPeg, beh, sacs); 
        List<SyncAndTimePeg> syncAndPegs = picturePlanner.addBehaviour(BMLBlockPeg.GLOBALPEG, beh, sacs, tpu);
        assertEquals(2, syncAndPegs.size());
        assertEquals("start", syncAndPegs.get(0).sync);
        assertEquals("end", syncAndPegs.get(1).sync);
        assertEquals(0, syncAndPegs.get(0).peg.getGlobalValue(), PLAN_PRECISION);
        assertEquals(TimePeg.VALUE_UNKNOWN, syncAndPegs.get(1).peg.getGlobalValue(), PLAN_PRECISION);
    }
}
