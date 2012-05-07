package asap.faceengine;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hmi.bml.core.Behaviour;
import hmi.bml.core.FaceLexemeBehaviour;
import hmi.bml.parser.Constraint;
import hmi.elckerlyc.BehaviourPlanningException;
import hmi.elckerlyc.PlannerTests;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.feedback.FeedbackManagerImpl;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.pegboard.TimePeg;
import hmi.elckerlyc.planunit.KeyPosition;
import hmi.elckerlyc.planunit.PlanManager;
import hmi.elckerlyc.scheduler.BMLBlockManager;
import hmi.elckerlyc.scheduler.TimePegAndConstraint;
import hmi.elckerlyc.util.KeyPositionMocker;
import hmi.elckerlyc.util.TimePegUtil;
import hmi.faceanimation.FaceController;
import hmi.faceanimation.converters.EmotionConverter;
import hmi.faceanimation.converters.FACSConverter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.ext.murml.MURMLFaceBehaviour;
import asap.faceengine.facebinding.FaceBinding;
import asap.faceengine.faceunit.FaceUnit;
import asap.faceengine.faceunit.KeyframeMorphFU;
import asap.faceengine.faceunit.TimedFaceUnit;

/**
 * Unit testcases for the FacePlanner
 * @author hvanwelbergen
 * 
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({BMLBlockManager.class})
public class FacePlannerTest
{
    private PlannerTests<TimedFaceUnit> plannerTests;
    private FacePlanner facePlanner;
    private static final String BMLID = "bml1";
    private final BMLBlockPeg bbPeg = new BMLBlockPeg("Peg1", 0.3);
    private BMLBlockManager mockBmlBlockManager = mock(BMLBlockManager.class);
    private FeedbackManager fbManager = new FeedbackManagerImpl(mockBmlBlockManager, "character1");
    private FaceController mockFaceController = mock(FaceController.class);
    private final PlanManager<TimedFaceUnit> planManager = new PlanManager<TimedFaceUnit>();
    private FaceBinding mockFaceBinding = mock(FaceBinding.class);
    private FaceUnit mockUnit = mock(FaceUnit.class);
    private static final float PLAN_PRECISION = 0.00001f;
            
    @Before
    public void setup()
    {
        facePlanner = new FacePlanner(fbManager, mockFaceController, null, null, mockFaceBinding, planManager);
        plannerTests = new PlannerTests<TimedFaceUnit>(facePlanner, bbPeg);
        
        
        TimedFaceUnit tmu = new TimedFaceUnit(fbManager, bbPeg, BMLID, "nod1", mockUnit);
        KeyPositionMocker.stubKeyPositions(mockUnit, new KeyPosition("start", 0, 1), new KeyPosition("end", 1, 1));
        final List<TimedFaceUnit> tmus = new ArrayList<TimedFaceUnit>();
        tmus.add(tmu);
        when(mockFaceBinding.getFaceUnit((FeedbackManager) any(), (BMLBlockPeg) any(), (Behaviour)any(), 
                (FaceController)any(),(FACSConverter)any(),(EmotionConverter)any())).thenReturn(tmus);
        when(mockUnit.getPreferedDuration()).thenReturn(3.0);
        when(mockUnit.hasValidParameters()).thenReturn(true);
    }

    public FaceLexemeBehaviour createFaceBehaviour() throws IOException
    {
        return new FaceLexemeBehaviour(BMLID, new XMLTokenizer("<faceLexeme id=\"face1\" lexeme=\"BLINK\"/>"));
    }

    @Test
    public void testResolveUnsetStart() throws BehaviourPlanningException, IOException
    {
        plannerTests.testResolveUnsetStart(createFaceBehaviour());
    }

    @Test(expected = BehaviourPlanningException.class)
    public void testResolveNonExistingSync() throws IOException, BehaviourPlanningException
    {
        plannerTests.testResolveNonExistingSync(createFaceBehaviour());
    }

    @Test
    public void testResolveStartOffset() throws IOException, BehaviourPlanningException
    {
        plannerTests.testResolveStartOffset(createFaceBehaviour());
    }
    
    @Test
    public void testResolveMURML() throws IOException, BehaviourPlanningException
    {
        String bmlString = "<murml:murmlface xmlns:murml=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\" " +
                "id=\"a1\" start=\"nod1:end\">" +
                "<definition><keyframing><phase>" +
                "<frame ftime=\"0\">" +
                "<posture>Humanoid (dB_Smile 3 70 0 0)</posture>" +
                "</frame>" +
                "<frame ftime=\"2\">" +
                "<posture>Humanoid (dB_Smile 3 80 0 0)</posture>" +
                "</frame>" +
                "</phase></keyframing></definition>"+
                "</murml:murmlface>";
        MURMLFaceBehaviour b = new MURMLFaceBehaviour(BMLID, new XMLTokenizer(bmlString));
        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        TimePeg startPeg = TimePegUtil.createTimePeg(0);
        TimePeg endPeg = new TimePeg(bbPeg);
        sacs.add(new TimePegAndConstraint("start",startPeg , new Constraint(), 0, false));
        sacs.add(new TimePegAndConstraint("end",endPeg , new Constraint(), 0, false));
        
        TimedFaceUnit tfu = facePlanner.resolveSynchs(bbPeg, b, sacs);
        assertNotNull(tfu);
        assertThat(tfu.getFaceUnit(), instanceOf(KeyframeMorphFU.class));      
        assertEquals(0, tfu.getStartTime(), PLAN_PRECISION);
        assertEquals(2, tfu.getEndTime(), PLAN_PRECISION);
    }
}
