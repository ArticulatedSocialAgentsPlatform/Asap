/*******************************************************************************
 *******************************************************************************/
package asap.textengine;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import saiba.bml.core.Behaviour;
import saiba.bml.core.SpeechBehaviour;
import asap.bml.ext.bmlt.BMLTTextBehaviour;
import asap.realizer.AbstractPlanner;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.SyncAndTimePeg;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.OffsetPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.PlanManager;
import asap.realizer.scheduler.TimePegAndConstraint;

/**
 * The TextPlanner is used to plan SpeechBehaviours. The TimedPlanUnits it constructs
 * are more flexible than those constructed by the TTSPlanner.
 * @author welberge
 * 
 */
public class TextPlanner extends AbstractPlanner<TimedSpeechTextUnit>
{
    private static Logger logger = LoggerFactory.getLogger(TextPlanner.class.getName());

    private TextOutput textOutput;

    @Override
    public String toString()
    {
        return getClass().getName() + "[" + textOutput.getClass().getName() + "]";
    }

    public TextPlanner(FeedbackManager bfm, TextOutput output, PlanManager<TimedSpeechTextUnit> planManager)
    {
        super(bfm, planManager);
        textOutput = output;
    }

    public void setTextOutput(TextOutput to)
    {
        textOutput = to;
    }

    private TimedSpeechTextUnit createSpeechUnit(BMLBlockPeg bbPeg, Behaviour b)
    {
        SpeechBehaviour bSpeech = (SpeechBehaviour) b;
        TimedSpeechTextUnit bs = new TimedSpeechTextUnit(fbManager, bbPeg, bSpeech.getContent(), bSpeech.getBmlId(), bSpeech.id, textOutput);
        return bs;
    }

    private void validateSacs(Behaviour b, TimedSpeechTextUnit bs, List<TimePegAndConstraint> sacs) throws BehaviourPlanningException
    {
        for (TimePegAndConstraint sac : sacs)
        {
            if (!bs.hasSync(sac.syncId)) throw new BehaviourPlanningException(b, "Invalid synchronization constraint " + sac + " syncId "
                    + sac.syncId + " not found in speech unit");
        }
    }

    /**
     * Creates a SpeechUnit that satisfies sacs and adds it to the motion plan. All registered
     * BMLFeedbackListeners are linked to this SpeechUnit.
     */
    @Override
    public List<SyncAndTimePeg> addBehaviour(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sacs, TimedSpeechTextUnit bs)
            throws BehaviourPlanningException
    {
        
        if (bs == null)
        {
            bs = createSpeechUnit(bbPeg, b);
        }
        validateSacs(b, bs, sacs);
        linkSyncs(bs, sacs);

        ArrayList<SyncAndTimePeg> satp = constructSyncAndTimePegs(bbPeg, b, bs);

        planManager.addPlanUnit(bs);
        return satp;
    }

    

    @Override
    public TimedSpeechTextUnit resolveSynchs(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sacs)
            throws BehaviourPlanningException
    {
        TimedSpeechTextUnit bs = createSpeechUnit(bbPeg, b);
        validateSacs(b, bs, sacs);
        // sort sac
        ArrayList<TimePegAndConstraint> sortedSac = new ArrayList<TimePegAndConstraint>();
        for (String sync : bs.getAvailableSyncs())
        {
            for (TimePegAndConstraint s : sacs)
            {
                if (s.syncId.equals(sync))
                {
                    sortedSac.add(s);
                    break;
                }
            }
        }

        // link sacs synchronisation points to tmu syncpoints with the same id
        linkSyncs(bs, sortedSac);

        // determine avg stretch
        int sections = 0;
        double totalStretch = 0;
        TimePegAndConstraint sPrev = null;
        for (TimePegAndConstraint s : sortedSac)
        {
            if (s.peg.getGlobalValue() != TimePeg.VALUE_UNKNOWN)
            {
                if (sPrev != null)
                {
                    double duration = bs.getTime(s.syncId) - bs.getTime(sPrev.syncId);
                    double startKey = 0, endKey = 0;
                    for (String sync : bs.getAvailableSyncs())
                    {
                        TimePeg tp = bs.getTimePeg(sync);
                        if (tp != null)
                        {
                            if (tp.getLink() == sPrev.peg.getLink())
                            {
                                startKey = bs.getRelativeTime(sync);
                            }
                            if (tp.getLink() == s.peg.getLink())
                            {
                                endKey = bs.getRelativeTime(sync);
                            }
                        }
                    }
                    double nominalDuration = (endKey - startKey) * bs.getPreferedDuration();
                    if (nominalDuration > 0)
                    {
                        totalStretch += duration / nominalDuration;
                        sections++;
                    }
                }
                else
                {
                    sPrev = s;
                }
            }
        }

        double avgStretch = 1;
        if (sections > 0)
        {
            avgStretch = totalStretch / sections;
        }

        if (sortedSac.size() > 0)
        {
            // handle first sync unknown
            TimePegAndConstraint sacStart = sortedSac.get(0);
            if (sacStart.peg.getGlobalValue() == TimePeg.VALUE_UNKNOWN)
            {
                TimePegAndConstraint sacNext = null;
                for (TimePegAndConstraint s : sortedSac)
                {
                    if (s.peg.getGlobalValue() != TimePeg.VALUE_UNKNOWN)
                    {
                        sacNext = s;
                        break;
                    }
                }
                if (sacNext == null)
                {
                    // no other constraints set in this behavior, just start
                    // asap (taking negative offsets into account)
                    // if sacStart.resolveAsStartOffset is set, this just keeps
                    // the created temporary timepeg
                    // sacStart.peg.setValue(-sacStart.offset);
                    sacStart.peg.setLocalValue(0);
                }
                else
                {
                    double nextKey = bs.getRelativeTime(sacNext.syncId);
                    double nextTime = bs.getTime(sacNext.syncId);
                    double tStart = nextTime - nextKey * avgStretch * bs.getPreferedDuration();
                    if (sacStart.resolveAsStartOffset)
                    {
                        OffsetPeg os = (OffsetPeg) sacStart.peg;
                        os.setLink(bs.getTimePeg(sacNext.syncId));
                        os.setOffset(tStart - nextTime);
                    }
                    else
                    {
                        sacStart.peg.setGlobalValue(tStart + sacStart.offset);
                    }
                }
            }

            // handle last sync unknown
            TimePegAndConstraint sacEnd = sortedSac.get(sortedSac.size() - 1);
            if (sacEnd.peg.getGlobalValue() == TimePeg.VALUE_UNKNOWN)
            {
                TimePegAndConstraint sacPrev = null;
                for (TimePegAndConstraint s : sortedSac)
                {
                    if (s.peg.getGlobalValue() != TimePeg.VALUE_UNKNOWN)
                    {
                        sacPrev = s;
                    }
                }
                if (sacPrev == null)
                {
                    // no other constraints set in this behavior, just start asap
                    sacEnd.peg.setGlobalValue(bs.getPreferedDuration());
                }
                else
                {
                    double keyPrev = bs.getRelativeTime(sacPrev.syncId);
                    double keyNext = 1;
                    double keyCurr = bs.getRelativeTime(sacEnd.syncId);
                    double timePrev = bs.getTime(sacPrev.syncId);
                    double timeNext = timePrev + (keyNext - keyPrev) * avgStretch * bs.getPreferedDuration();
                    sacEnd.peg.setGlobalValue(timePrev + ((keyCurr - keyPrev) * (timeNext - timePrev)) / (keyNext - keyPrev)
                            + sacEnd.offset);
                    /*
                     * double prevDur = 1 - bs.getRelativeTime(sacEnd.id);
                     * sacEnd.peg.setValue(bs.getPegTime(sacPrev.id)+prevDur * avgStretch *
                     * bs.getPreferedDuration()+sacEnd.offset);
                     */
                }
            }

            // interpolate unknown inbetweens syncs
            TimePegAndConstraint sacPrev = sacStart;
            TimePegAndConstraint sacNext = null;
            for (TimePegAndConstraint s : sortedSac)
            {
                if (s.peg.getGlobalValue() == TimePeg.VALUE_UNKNOWN)
                {
                    // find next
                    for (TimePegAndConstraint s2 : sortedSac.subList(sortedSac.indexOf(s), sortedSac.size()))
                    {
                        if (s2.peg.getGlobalValue() != TimePeg.VALUE_UNKNOWN)
                        {
                            sacNext = s2;
                            break;
                        }
                    }

                    double keyPrev = bs.getRelativeTime(sacPrev.syncId);
                    double keyNext = bs.getRelativeTime(sacNext.syncId);
                    double keyCurr = bs.getRelativeTime(s.syncId);
                    double timePrev = bs.getTime(sacPrev.syncId);
                    double timeNext = bs.getTime(sacNext.syncId);
                    s.peg.setGlobalValue(timePrev + ((keyCurr - keyPrev) * (timeNext - timePrev)) / (keyNext - keyPrev) + s.offset);
                }
                sacPrev = s;
            }

            // resolve end if unknown and not a persistent behavior
            sacEnd = sortedSac.get(sortedSac.size() - 1);
            logger.debug(sacEnd.syncId);
            if (!sacEnd.syncId.equals("end") && bs.getPreferedDuration() > 0)
            {
                double keyPrev = bs.getRelativeTime(sacEnd.syncId);
                double keyEnd = bs.getRelativeTime("end");
                OffsetPeg op = new OffsetPeg(bs.getTimePeg(sacEnd.syncId), (keyEnd - keyPrev) * avgStretch * bs.getPreferedDuration());
                bs.setEnd(op);
            }
        }
        return bs;
    }

    private void linkSyncs(TimedSpeechTextUnit su, List<TimePegAndConstraint> sacs)
    {
        for (TimePegAndConstraint s : sacs)
        {
            for (String sync : su.getAvailableSyncs())
            {
                if (s.syncId.equals(sync))
                {
                    if (s.offset == 0)
                    {
                        su.setTimePeg(sync, s.peg);
                    }
                    else
                    {
                        su.setTimePeg(sync, new OffsetPeg(s.peg, -s.offset));
                    }
                }
            }
        }
    }

    @Override
    public List<Class<? extends Behaviour>> getSupportedBehaviours()
    {
        List<Class<? extends Behaviour>> list = new ArrayList<Class<? extends Behaviour>>();
        list.add(SpeechBehaviour.class);
        list.add(BMLTTextBehaviour.class);
        return list;
    }

    @Override
    public List<Class<? extends Behaviour>> getSupportedDescriptionExtensions()
    {
        List<Class<? extends Behaviour>> list = new ArrayList<Class<? extends Behaviour>>();
        return list;
    }

    @Override
    public double getRigidity(Behaviour beh)
    {
        return 0.5;
    }
}
