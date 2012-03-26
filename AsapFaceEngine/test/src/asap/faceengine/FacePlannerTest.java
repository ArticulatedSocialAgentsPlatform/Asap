package asap.faceengine;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hmi.bml.core.Behaviour;
import hmi.bml.core.FaceBehaviour;
import hmi.elckerlyc.BehaviourPlanningException;
import hmi.elckerlyc.PlannerTests;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.feedback.FeedbackManagerImpl;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.planunit.KeyPosition;
import hmi.elckerlyc.planunit.PlanManager;
import hmi.elckerlyc.scheduler.BMLBlockManager;
import hmi.elckerlyc.util.KeyPositionMocker;
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

import asap.faceengine.facebinding.FaceBinding;
import asap.faceengine.faceunit.FaceUnit;
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
    
    @Before
    public void setup()
    {
        facePlanner = new FacePlanner(fbManager, mockFaceController, null, null, mockFaceBinding, planManager);
        plannerTests = new PlannerTests<TimedFaceUnit>(facePlanner, bbPeg);
        
        
        TimedFaceUnit tmu = new TimedFaceUnit(fbManager, bbPeg, BMLID, "nod1", mockUnit);
        KeyPositionMocker.stubKeyPositions(mockUnit, new KeyPosition("start", 0, 1), new KeyPosition("end", 1, 1));
        final List<TimedFaceUnit> tmus = new ArrayList<TimedFaceUnit>();
        tmus.add(tmu);        
        //getFaceUnit(fbManager, bbPeg, b, faceController, facsConverter, emotionConverter)
        when(mockFaceBinding.getFaceUnit((FeedbackManager) any(), (BMLBlockPeg) any(), (Behaviour)any(), 
                (FaceController)any(),(FACSConverter)any(),(EmotionConverter)any())).thenReturn(tmus);
        when(mockUnit.getPreferedDuration()).thenReturn(3.0);
        when(mockUnit.hasValidParameters()).thenReturn(true);
    }

    public FaceBehaviour createFaceBehaviour() throws IOException
    {
        return new FaceBehaviour(BMLID, new XMLTokenizer("<face id=\"face1\" type=\"BLINK\"/>"));
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
}
