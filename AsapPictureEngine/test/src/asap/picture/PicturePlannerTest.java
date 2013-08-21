package asap.picture;

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
import saiba.utils.TestUtil;
import asap.picture.bml.SetImageBehavior;
import asap.picture.picturebinding.PictureBinding;
import asap.picture.planunit.PictureUnit;
import asap.picture.planunit.TimedPictureUnit;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.feedback.FeedbackManagerImpl;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.PlanManager;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizertestutil.PlannerTests;

/**
 * Unit tests for the PicturePlanner
 * @author hvanwelbergen
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ BMLBlockManager.class })
public class PicturePlannerTest
{
    private PlannerTests<TimedPictureUnit> plannerTests;
    private static final String BMLID = "bml1";
    private BMLBlockManager mockBmlBlockManager = mock(BMLBlockManager.class);
    private FeedbackManager fbManager = new FeedbackManagerImpl(mockBmlBlockManager, "character1");
    private PictureBinding mockPictureBinding = mock(PictureBinding.class);
    private final PlanManager<TimedPictureUnit> planManager = new PlanManager<TimedPictureUnit>();
    private PicturePlanner picturePlanner;
    private final BMLBlockPeg bbPeg = new BMLBlockPeg("Peg1", 0.3);
    private PictureUnit stubPU = new StubPictureUnit();
    
    @Before
    public void setup()
    {
        picturePlanner = new PicturePlanner(fbManager, mockPictureBinding, planManager);
        plannerTests = new PlannerTests<TimedPictureUnit>(picturePlanner, bbPeg);
        final List<TimedPictureUnit> tmus = new ArrayList<TimedPictureUnit>();
        tmus.add(new TimedPictureUnit(fbManager, bbPeg, BMLID, "beh1", stubPU));
        when(mockPictureBinding.getPictureUnit(any(FeedbackManager.class), any(BMLBlockPeg.class), any(Behaviour.class))).thenReturn(tmus);        
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
}
