/*******************************************************************************
 *******************************************************************************/
package asap.realizer.scheduler;

import java.util.ArrayList;
import java.util.List;

import saiba.bml.core.Behaviour;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.SyncPointNotFoundException;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.OffsetPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedPlanUnit;


/**
 * Resolves the timing of a TimedPlanUnit, by satisfying the constraints using uniform stretching
 * @author welberge
 */
public class LinearStretchResolver implements UniModalResolver
{
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
        
        ArrayList<TimePegAndConstraint> sortedSac = sortSacs(sac, pu);

        pu.linkSynchs(sortedSac);

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
                                throw new BehaviourPlanningException(b, "RelativeSyncNotFoundException "+syncId, e);
                            }
                        }
                        if (pu.getTimePeg(syncId).getLink() == s.peg.getLink())
                        {
                            try
                            {
                                endKey = pu.getRelativeTime(syncId);
                            }
                            catch (SyncPointNotFoundException e)
                            {
                                throw new BehaviourPlanningException(b, "RelativeSyncNotFoundException "+syncId, e);
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
                    try
                    {
                        sacStart.peg.setLocalValue(pu.getRelativeTime(sacStart.syncId)*pu.getPreferedDuration());
                    }
                    catch (SyncPointNotFoundException e)
                    {
                        throw new BehaviourPlanningException(b, "RelativeSyncNotFoundException "+sacStart.syncId, e);
                    }
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
                        throw new BehaviourPlanningException(b, "RelativeSyncNotFoundException "+sacNext.syncId, e);
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
                        throw new BehaviourPlanningException(b, "RelativeSyncNotFoundException "+sacPrev.syncId, e);
                    }
                    double keyNext = 1;
                    double keyCurr;
                    try
                    {
                        keyCurr = pu.getRelativeTime(sacEnd.syncId);
                    }
                    catch (SyncPointNotFoundException e)
                    {
                        throw new BehaviourPlanningException(b, "RelativeSyncNotFoundException "+sacEnd.syncId, e);
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
                        throw new BehaviourPlanningException(b, "RelativeSyncNotFoundException "+sacPrev.syncId, e);
                    }
                    double keyNext;
                    try
                    {
                        keyNext = pu.getRelativeTime(sacNext.syncId);
                    }
                    catch (SyncPointNotFoundException e)
                    {
                        throw new BehaviourPlanningException(b, "RelativeSyncNotFoundException "+sacNext.syncId, e);
                    }
                    double keyCurr;
                    try
                    {
                        keyCurr = pu.getRelativeTime(s.syncId);
                    }
                    catch (SyncPointNotFoundException e)
                    {
                        throw new BehaviourPlanningException(b, "RelativeSyncNotFoundException "+s.syncId, e);
                    }
                    double timePrev = pu.getTime(sacPrev.syncId);
                    double timeNext = pu.getTime(sacNext.syncId);
                    s.peg.setGlobalValue(timePrev + ((keyCurr - keyPrev) * (timeNext - timePrev)) / (keyNext - keyPrev) + s.offset);
                }
                sacPrev = s;
            }

            // resolve end if unknown and not a persistent behavior
            sacEnd = sortedSac.get(sortedSac.size() - 1);
            
            /*
            if (!sacEnd.syncId.equals("end") && pu.getPreferedDuration() > 0 && resolveEndAsOffset)
            {
                double keyPrev;
                try
                {
                    keyPrev = pu.getRelativeTime(sacEnd.syncId);
                }
                catch (SyncPointNotFoundException e)
                {
                    throw new BehaviourPlanningException(b, "RelativeSyncNotFoundException "+sacEnd.syncId, e);
                }
                double keyEnd;
                try
                {
                    keyEnd = pu.getRelativeTime("end");
                }
                catch (SyncPointNotFoundException e)
                {
                    throw new BehaviourPlanningException(b, "RelativeSyncNotFoundException end", e);
                }
                OffsetPeg op = new OffsetPeg(pu.getTimePeg(sacEnd.syncId), (keyEnd - keyPrev) * avgStretch * pu.getPreferedDuration());
                pu.setTimePeg("end", op);
            }
            */
        }
    }

    private ArrayList<TimePegAndConstraint> sortSacs(List<TimePegAndConstraint> sac, TimedPlanUnit pu)
    {
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
        return sortedSac;
    }

}
