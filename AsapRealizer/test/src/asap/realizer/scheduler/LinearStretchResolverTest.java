/*******************************************************************************
 *******************************************************************************/
package asap.realizer.scheduler;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import saiba.bml.core.Behaviour;
import saiba.bml.parser.Constraint;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.feedback.NullFeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.OffsetPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.StubTimedPlanUnit;
import asap.realizertestutil.util.TimePegUtil;

/**
 * Unit tests for the LinearStretchResolver
 * @author hvanwelbergen
 * 
 */
public class LinearStretchResolverTest
{
    private static final double PRECISION = 0.001;
    private static final double PREF_DURATION = 10;
    private LinearStretchResolver lsr = new LinearStretchResolver();
    private StubTimedPlanUnit tmu = new StubTimedPlanUnit(NullFeedbackManager.getInstance(), BMLBlockPeg.GLOBALPEG, "bml1", "beh1",
            PREF_DURATION);
    private Behaviour beh = mock(Behaviour.class);

    @Test
    public void testUnspecifiedStart() throws BehaviourPlanningException
    {
        List<TimePegAndConstraint> sac = new ArrayList<TimePegAndConstraint>();
        sac.add(new TimePegAndConstraint("start", TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, TimePeg.VALUE_UNKNOWN),
                new Constraint(), 0, false));
        lsr.resolveSynchs(BMLBlockPeg.GLOBALPEG, beh, sac, tmu);
        assertEquals(0, tmu.getStartTime(), PRECISION);
    }

    @Test
    public void testSpecifiedStart() throws BehaviourPlanningException
    {
        List<TimePegAndConstraint> sac = new ArrayList<TimePegAndConstraint>();
        sac.add(new TimePegAndConstraint("start", TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, 1), new Constraint(), 0, false));
        lsr.resolveSynchs(BMLBlockPeg.GLOBALPEG, beh, sac, tmu);
        assertEquals(1, tmu.getStartTime(), PRECISION);
    }

    @Test
    public void testTwoConstraintsEndUnknown() throws BehaviourPlanningException
    {

        List<TimePegAndConstraint> sac = new ArrayList<TimePegAndConstraint>();
        sac.add(new TimePegAndConstraint("start", TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, 1), new Constraint(), 0, false));
        sac.add(new TimePegAndConstraint("end", TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, TimePeg.VALUE_UNKNOWN), new Constraint(),
                0, false));
        lsr.resolveSynchs(BMLBlockPeg.GLOBALPEG, beh, sac, tmu);
        assertEquals(1, tmu.getStartTime(), PRECISION);
        assertEquals(1 + PREF_DURATION, tmu.getEndTime(), PRECISION);
    }

    @Test
    public void testTwoConstraintsStartUnknown() throws BehaviourPlanningException
    {

        List<TimePegAndConstraint> sac = new ArrayList<TimePegAndConstraint>();
        sac.add(new TimePegAndConstraint("start", TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, TimePeg.VALUE_UNKNOWN),
                new Constraint(), 0, false));
        sac.add(new TimePegAndConstraint("end", TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, 1 + PREF_DURATION), new Constraint(), 0,
                false));
        lsr.resolveSynchs(BMLBlockPeg.GLOBALPEG, beh, sac, tmu);
        assertEquals(1, tmu.getStartTime(), PRECISION);
        assertEquals(1 + PREF_DURATION, tmu.getEndTime(), PRECISION);
    }
    
    @Test
    public void testTwoConstraintsStartUnknownResolveAsOffset() throws BehaviourPlanningException
    {

        List<TimePegAndConstraint> sac = new ArrayList<TimePegAndConstraint>();
        sac.add(new TimePegAndConstraint("start", new OffsetPeg(TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG,TimePeg.VALUE_UNKNOWN),0),
                new Constraint(), 0, true));
        sac.add(new TimePegAndConstraint("end", TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, 1 + PREF_DURATION), new Constraint(), 0,
                false));
        lsr.resolveSynchs(BMLBlockPeg.GLOBALPEG, beh, sac, tmu);
        assertEquals(1, tmu.getStartTime(), PRECISION);
        assertEquals(1 + PREF_DURATION, tmu.getEndTime(), PRECISION);
        assertEquals(tmu.getTimePeg("start").getLink(),tmu.getTimePeg("end"));
    }

    @Test
    public void testOnlyEndConstraint() throws BehaviourPlanningException
    {
        List<TimePegAndConstraint> sac = new ArrayList<TimePegAndConstraint>();
        sac.add(new TimePegAndConstraint("end", TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, TimePeg.VALUE_UNKNOWN), new Constraint(),
                0, false));
        lsr.resolveSynchs(BMLBlockPeg.GLOBALPEG, beh, sac, tmu);
        assertEquals(PREF_DURATION, tmu.getEndTime(), PRECISION);
    }

    @Test
    public void testTwoConstraintsNoStartResolve() throws BehaviourPlanningException
    {
        List<TimePegAndConstraint> sac = new ArrayList<TimePegAndConstraint>();
        sac.add(new TimePegAndConstraint("relax", TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, 10), new Constraint(), 0, false));
        sac.add(new TimePegAndConstraint("end", TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, TimePeg.VALUE_UNKNOWN), new Constraint(),
                0, false));
        lsr.resolveSynchs(BMLBlockPeg.GLOBALPEG, beh, sac, tmu);
        assertEquals(10, tmu.getRelaxTime(), PRECISION);
        assertEquals(10 + PREF_DURATION * (1 - StubTimedPlanUnit.RELAX_RELATIVE_TIME), tmu.getEndTime(), PRECISION);
    }

    @Test
    public void testTwoConstraintsNegativeStart() throws BehaviourPlanningException
    {
        List<TimePegAndConstraint> sac = new ArrayList<TimePegAndConstraint>();
        sac.add(new TimePegAndConstraint("start", TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, TimePeg.VALUE_UNKNOWN),
                new Constraint(), 0, false));
        sac.add(new TimePegAndConstraint("end", TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, PREF_DURATION - 1), new Constraint(), 0,
                false));
        lsr.resolveSynchs(BMLBlockPeg.GLOBALPEG, beh, sac, tmu);
        assertEquals(-1, tmu.getStartTime(), PRECISION);
        assertEquals(PREF_DURATION - 1, tmu.getEndTime(), PRECISION);
    }

    @Test
    public void testFourConstraintsNoneSet() throws BehaviourPlanningException
    {
        List<TimePegAndConstraint> sac = new ArrayList<TimePegAndConstraint>();
        sac.add(new TimePegAndConstraint("start", TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, TimePeg.VALUE_UNKNOWN),
                new Constraint(), 0, false));
        sac.add(new TimePegAndConstraint("ready", TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, TimePeg.VALUE_UNKNOWN),
                new Constraint(), 0, false));
        sac.add(new TimePegAndConstraint("relax", TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, TimePeg.VALUE_UNKNOWN),
                new Constraint(), 0, false));
        sac.add(new TimePegAndConstraint("end", TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, TimePeg.VALUE_UNKNOWN), new Constraint(),
                0, false));
        lsr.resolveSynchs(BMLBlockPeg.GLOBALPEG, beh, sac, tmu);
        assertEquals(0, tmu.getStartTime(), PRECISION);
        assertEquals(PREF_DURATION, tmu.getEndTime(), PRECISION);
        assertEquals(StubTimedPlanUnit.READY_RELATIVE_TIME * PREF_DURATION, tmu.getTime("ready"), PRECISION);
        assertEquals(StubTimedPlanUnit.RELAX_RELATIVE_TIME * PREF_DURATION, tmu.getRelaxTime(), PRECISION);
    }

    @Test
    public void testFourConstraintsStretch() throws BehaviourPlanningException
    {
        List<TimePegAndConstraint> sac = new ArrayList<TimePegAndConstraint>();
        sac.add(new TimePegAndConstraint("start", TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, 0), new Constraint(), 0, false));
        sac.add(new TimePegAndConstraint("ready", TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, TimePeg.VALUE_UNKNOWN),
                new Constraint(), 0, false));
        sac.add(new TimePegAndConstraint("relax", TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, TimePeg.VALUE_UNKNOWN),
                new Constraint(), 0, false));
        sac.add(new TimePegAndConstraint("end", TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, 5), new Constraint(), 0, false));
        lsr.resolveSynchs(BMLBlockPeg.GLOBALPEG, beh, sac, tmu);
        assertEquals(0, tmu.getStartTime(), PRECISION);
        assertEquals(5, tmu.getEndTime(), PRECISION);
        assertEquals(StubTimedPlanUnit.READY_RELATIVE_TIME * 5, tmu.getTime("ready"), PRECISION);
        assertEquals(StubTimedPlanUnit.RELAX_RELATIVE_TIME * 5, tmu.getRelaxTime(), PRECISION);
    }
    
    @Test
    public void testFourConstraintsStretchMiddle() throws BehaviourPlanningException
    {
        List<TimePegAndConstraint> sac = new ArrayList<TimePegAndConstraint>();
        sac.add(new TimePegAndConstraint("start", TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, TimePeg.VALUE_UNKNOWN), new Constraint(), 0, false));
        sac.add(new TimePegAndConstraint("ready", TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, 10),
                new Constraint(), 0, false));
        sac.add(new TimePegAndConstraint("relax", TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, 11),
                new Constraint(), 0, false));
        sac.add(new TimePegAndConstraint("end", TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, TimePeg.VALUE_UNKNOWN), new Constraint(), 0, false));
        lsr.resolveSynchs(BMLBlockPeg.GLOBALPEG, beh, sac, tmu);
        assertEquals(11.5, tmu.getEndTime(), PRECISION);
        assertEquals(9.5, tmu.getStartTime(), PRECISION);
        assertEquals(10, tmu.getTime("ready"), PRECISION);
        assertEquals(11, tmu.getRelaxTime(), PRECISION);
    }
    
    @Test
    public void testFiveConstraintsStretchMiddle() throws BehaviourPlanningException
    {
        List<TimePegAndConstraint> sac = new ArrayList<TimePegAndConstraint>();
        sac.add(new TimePegAndConstraint("start", TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, TimePeg.VALUE_UNKNOWN), new Constraint(), 0, false));
        sac.add(new TimePegAndConstraint("stroke", TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, TimePeg.VALUE_UNKNOWN), new Constraint(), 0, false));
        sac.add(new TimePegAndConstraint("ready", TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, 10),
                new Constraint(), 0, false));        
        sac.add(new TimePegAndConstraint("relax", TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, 11),
                new Constraint(), 0, false));
        sac.add(new TimePegAndConstraint("end", TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, TimePeg.VALUE_UNKNOWN), new Constraint(), 0, false));
        lsr.resolveSynchs(BMLBlockPeg.GLOBALPEG, beh, sac, tmu);
        assertEquals(11.5, tmu.getEndTime(), PRECISION);
        assertEquals(9.5, tmu.getStartTime(), PRECISION);
        assertEquals(10, tmu.getTime("ready"), PRECISION);
        assertEquals(10.5, tmu.getTime("stroke"), PRECISION);
        assertEquals(11, tmu.getRelaxTime(), PRECISION);
    }
}
