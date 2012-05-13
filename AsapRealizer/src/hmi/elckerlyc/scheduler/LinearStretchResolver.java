package hmi.elckerlyc.scheduler;

import saiba.bml.core.Behaviour;
import hmi.elckerlyc.BehaviourPlanningException;
import hmi.elckerlyc.SyncPointNotFoundException;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.pegboard.OffsetPeg;
import hmi.elckerlyc.pegboard.TimePeg;
import hmi.elckerlyc.planunit.TimedPlanUnit;

import java.util.ArrayList;
import java.util.List;


/**
 * Resolves the timing of a TimedPlanUnit, by satisfying the constraints using uniform stretching
 * @author welberge
 */
public class LinearStretchResolver implements UniModalResolver
{
    // link synchpoints in sac to tmu
    private void linkSynchs(TimedPlanUnit tmu, List<TimePegAndConstraint> sacs)
    {
        for (TimePegAndConstraint s : sacs)
        {
            for (String syncId : tmu.getAvailableSyncs())
            {
                if (s.syncId.equals(syncId))
                {
                    if (s.offset == 0)
                    {
                        tmu.setTimePeg(syncId, s.peg);
                    }
                    else
                    {
                        tmu.setTimePeg(syncId, new OffsetPeg(s.peg, -s.offset));
                    }
                }
            }
        }
    }

    private void validateSyncs(TimedPlanUnit pu, Behaviour b, List<TimePegAndConstraint> sac) throws BehaviourPlanningException
    {
        for (TimePegAndConstraint s : sac)
        {
            if (!pu.getAvailableSyncs().contains(s.syncId))
            {
                throw new BehaviourPlanningException(b, "Sync id " + s.syncId + " not available for TimedPlanUnit " + pu);
            }
        }
    }

    @Override
    public void resolveSynchs(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sac, TimedPlanUnit pu)
            throws BehaviourPlanningException
    {
        validateSyncs(pu, b, sac);
        // sort sac
        ArrayList<TimePegAndConstraint> sortedSac = new ArrayList<TimePegAndConstraint>();
        for (String syncId : pu.getAvailableSyncs())
        {
            for (TimePegAndConstraint s : sac)
            {
                if (s.syncId.equals(syncId))
                {
                    sortedSac.add(s);
                    break;
                }
            }
        }

        linkSynchs(pu, sortedSac);

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
                    double duration = pu.getTime(s.syncId) - pu.getTime(sPrev.syncId);
                    double startKey = 0, endKey = 0;

                    for (String syncId : pu.getAvailableSyncs())
                    {
                        if (pu.getTimePeg(syncId) == null) continue;

                        if (pu.getTimePeg(syncId).getLink() == sPrev.peg.getLink())
                        {
                            try
                            {
                                startKey = pu.getRelativeTime(syncId);
                            }
                            catch (SyncPointNotFoundException e)
                            {
                                throw new BehaviourPlanningException(b, "RelativeSyncNotFoundException", e);
                            }
                        }
                        if (pu.getTimePeg(syncId).getLink() == s.peg.getLink())
                        {
                            try
                            {
                                startKey = pu.getRelativeTime(syncId);
                            }
                            catch (SyncPointNotFoundException e)
                            {
                                throw new BehaviourPlanningException(b, "RelativeSyncNotFoundException", e);
                            }
                        }
                    }
                    double nominalDuration = (endKey - startKey) * pu.getPreferedDuration();
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
                    double nextKey;
                    try
                    {
                        nextKey = pu.getRelativeTime(sacNext.syncId);
                    }
                    catch (SyncPointNotFoundException e)
                    {
                        throw new BehaviourPlanningException(b, "RelativeSyncNotFoundException", e);
                    }
                    double nextTime = pu.getTime(sacNext.syncId);
                    double tStart = nextTime - nextKey * avgStretch * pu.getPreferedDuration();
                    if (sacStart.resolveAsStartOffset)
                    {
                        OffsetPeg os = (OffsetPeg) sacStart.peg;
                        os.setLink(pu.getTimePeg(sacNext.syncId));
                        os.setOffset(tStart - nextTime);
                    }
                    else
                    {
                        // TODO: test this, it never happens with the smartbody
                        // scheduler, it is always asked to resolve start
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
                    // no other constraints set in this behavior, just start
                    // asap
                    // TODO: test this, it never happens with the smartbody
                    // scheduler, it is always asked to resolve start
                    sacEnd.peg.setGlobalValue(pu.getPreferedDuration());
                }
                else
                {
                    double keyPrev;
                    try
                    {
                        keyPrev = pu.getRelativeTime(sacPrev.syncId);
                    }
                    catch (SyncPointNotFoundException e)
                    {
                        throw new BehaviourPlanningException(b, "RelativeSyncNotFoundException", e);
                    }
                    double keyNext = 1;
                    double keyCurr;
                    try
                    {
                        keyCurr = pu.getRelativeTime(sacEnd.syncId);
                    }
                    catch (SyncPointNotFoundException e)
                    {
                        throw new BehaviourPlanningException(b, "RelativeSyncNotFoundException", e);
                    }
                    double timePrev = pu.getTime(sacPrev.syncId);
                    double timeNext = timePrev + (keyNext - keyPrev) * avgStretch * pu.getPreferedDuration();
                    sacEnd.peg.setGlobalValue(timePrev + ((keyCurr - keyPrev) * (timeNext - timePrev)) / (keyNext - keyPrev)
                            + sacEnd.offset);
                    /*
                     * double prevDur = 1 - tmu.getKeyPosition(sacPrev.id).time;
                     * sacEnd.peg.setValue(tmu.getPegTime(sacPrev.id)+prevDur * avgStretch *
                     * tmu.mu.getPreferedDuration()+sacEnd.offset);
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

                    double keyPrev;
                    try
                    {
                        keyPrev = pu.getRelativeTime(sacPrev.syncId);
                    }
                    catch (SyncPointNotFoundException e)
                    {
                        throw new BehaviourPlanningException(b, "RelativeSyncNotFoundException", e);
                    }
                    double keyNext;
                    try
                    {
                        keyNext = pu.getRelativeTime(sacNext.syncId);
                    }
                    catch (SyncPointNotFoundException e)
                    {
                        throw new BehaviourPlanningException(b, "RelativeSyncNotFoundException", e);
                    }
                    double keyCurr;
                    try
                    {
                        keyCurr = pu.getRelativeTime(s.syncId);
                    }
                    catch (SyncPointNotFoundException e)
                    {
                        throw new BehaviourPlanningException(b, "RelativeSyncNotFoundException", e);
                    }
                    double timePrev = pu.getTime(sacPrev.syncId);
                    double timeNext = pu.getTime(sacNext.syncId);
                    s.peg.setGlobalValue(timePrev + ((keyCurr - keyPrev) * (timeNext - timePrev)) / (keyNext - keyPrev) + s.offset);
                }
                sacPrev = s;
            }

            // resolve end if unknown and not a persistent behavior
            sacEnd = sortedSac.get(sortedSac.size() - 1);
            // System.out.println(sacEnd.id);
            if (!sacEnd.syncId.equals("end") && pu.getPreferedDuration() > 0)
            {
                double keyPrev;
                try
                {
                    keyPrev = pu.getRelativeTime(sacEnd.syncId);
                }
                catch (SyncPointNotFoundException e)
                {
                    throw new BehaviourPlanningException(b, "RelativeSyncNotFoundException", e);
                }
                double keyEnd;
                try
                {
                    keyEnd = pu.getRelativeTime("end");
                }
                catch (SyncPointNotFoundException e)
                {
                    throw new BehaviourPlanningException(b, "RelativeSyncNotFoundException", e);
                }
                OffsetPeg op = new OffsetPeg(pu.getTimePeg(sacEnd.syncId), (keyEnd - keyPrev) * avgStretch * pu.getPreferedDuration());
                pu.setTimePeg("end", op);
            }
        }
    }

}
