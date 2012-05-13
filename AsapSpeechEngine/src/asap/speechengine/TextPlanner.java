package asap.speechengine;

import saiba.bml.core.Behaviour;
import saiba.bml.core.SpeechBehaviour;
import hmi.elckerlyc.BehaviourPlanningException;
import hmi.elckerlyc.SyncAndTimePeg;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.pegboard.OffsetPeg;
import hmi.elckerlyc.pegboard.TimePeg;
import hmi.elckerlyc.planunit.PlanManager;
import hmi.elckerlyc.scheduler.TimePegAndConstraint;
import hmi.elckerlyc.AbstractPlanner;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The TextPlanner is used to plan SpeechBehaviours. The TimedPlanUnits it constructs 
 * are more flexible than those constructed by the TTSPlanner. 
 * @author welberge
 *
 */
public class TextPlanner extends AbstractPlanner<TimedTextSpeechUnit>
{
    private static Logger logger = LoggerFactory.getLogger(TextPlanner.class.getName());

    private TextOutput textOutput;

    @Override
    public String toString()
    {
        return getClass().getName() + "[" + textOutput.getClass().getName() + "]";
    }

    public TextPlanner(FeedbackManager bfm, TextOutput output, PlanManager<TimedTextSpeechUnit> planManager)
    {
        super(bfm, planManager);
        textOutput = output;        
    }

    public void setTextOutput(TextOutput to)
    {
        textOutput = to;
    }

    private TimedTextSpeechUnit createSpeechUnit(BMLBlockPeg bbPeg, Behaviour b)
    {
        SpeechBehaviour bSpeech = (SpeechBehaviour) b;
        TimedTextSpeechUnit bs = new TimedTextSpeechUnit(fbManager,bbPeg,bSpeech.getContent(), bSpeech.getBmlId(), bSpeech.id, textOutput);
        return bs;
    }

    private void validateSacs(Behaviour b, TimedTextSpeechUnit bs, List<TimePegAndConstraint> sacs) throws BehaviourPlanningException
    {
        for (TimePegAndConstraint sac : sacs)
        {
            if(!bs.hasSync(sac.syncId))throw new BehaviourPlanningException(b, 
                    "Invalid synchronization constraint "+sac+" syncId "+sac.syncId+" not found in speech unit");
        }
    }
    
    /**
     * Creates a SpeechUnit that satisfies sacs and adds it to the motion plan. All registered
     * BMLFeedbackListeners are linked to this SpeechUnit.
     */
    @Override
    public List<SyncAndTimePeg> addBehaviour(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sacs,
            TimedTextSpeechUnit bs) throws BehaviourPlanningException
    {
        ArrayList<SyncAndTimePeg> satp = new ArrayList<SyncAndTimePeg>();
        if (bs == null)
        {
            bs = createSpeechUnit(bbPeg,b);
        }
        validateSacs(b, bs, sacs);
        
        // link start and end sync
        for (TimePegAndConstraint sac : sacs)
        {
            if (sac.syncId.equals("start"))
            {
                if (sac.offset == 0)
                {
                    bs.setStart(sac.peg);
                    satp.add(new SyncAndTimePeg(b.getBmlId(), b.id, "start", sac.peg));
                }
                else
                {
                    OffsetPeg p = new OffsetPeg(sac.peg, -sac.offset);
                    bs.setStart(p);
                    satp.add(new SyncAndTimePeg(b.getBmlId(), b.id, "start", p));
                }
            }
            if (sac.syncId.equals("end"))
            {
                if (sac.offset == 0)
                {
                    bs.setEnd(sac.peg);
                    satp.add(new SyncAndTimePeg(b.getBmlId(), b.id, "end", sac.peg));
                }
                else
                {
                    OffsetPeg p = new OffsetPeg(sac.peg, -sac.offset);
                    bs.setEnd(p);
                    satp.add(new SyncAndTimePeg(b.getBmlId(), b.id, "end", p));
                }
            }
        }
        linkSyncs(bs, sacs);

        for (String sync : bs.getSyncs())
        {
            if (bs.getTimePeg(sync) != null)
            {
                satp.add(new SyncAndTimePeg(b.getBmlId(), b.id, sync, bs.getTimePeg(sync)));
            }
        }
        
        planManager.addPlanUnit(bs);
        return satp;
    }

    @Override
    public TimedTextSpeechUnit resolveSynchs(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sacs)
            throws BehaviourPlanningException
    {
        TimedTextSpeechUnit bs = createSpeechUnit(bbPeg,b);
        validateSacs(b, bs, sacs);
        // sort sac
        ArrayList<TimePegAndConstraint> sortedSac = new ArrayList<TimePegAndConstraint>();
        for (String sync : bs.getSyncs())
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
                    for (String sync : bs.getSyncs())
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
                    double timeNext = timePrev+ (keyNext-keyPrev) * avgStretch * bs.getPreferedDuration();
                    sacEnd.peg.setGlobalValue(timePrev + ((keyCurr - keyPrev) * (timeNext - timePrev)) / (keyNext - keyPrev)+sacEnd.offset);
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
                    for (TimePegAndConstraint s2 : sortedSac.subList(sortedSac.indexOf(s),
                            sortedSac.size()))
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
                    s.peg.setGlobalValue(timePrev + ((keyCurr - keyPrev) * (timeNext - timePrev)) / (keyNext - keyPrev)+s.offset);
                }
                sacPrev = s;
            }
            
            //resolve end if unknown and not a persistent behavior
            sacEnd = sortedSac.get(sortedSac.size()-1);
            logger.debug(sacEnd.syncId);
            if(!sacEnd.syncId.equals("end") && bs.getPreferedDuration()>0)
            {
                double keyPrev = bs.getRelativeTime(sacEnd.syncId);
                double keyEnd = bs.getRelativeTime("end");                
                OffsetPeg op = new OffsetPeg(bs.getTimePeg(sacEnd.syncId),(keyEnd-keyPrev)*avgStretch*bs.getPreferedDuration());                
                bs.setEnd(op);                
            }
        }
        return bs;
    }

    private void linkSyncs(TimedTextSpeechUnit su, List<TimePegAndConstraint> sacs)
    {
        for (TimePegAndConstraint s : sacs)
        {
            for (String sync : su.getSyncs())
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
