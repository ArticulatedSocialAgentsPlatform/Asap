package asap.animationengine.ace.lmp;

import java.util.ArrayList;
import java.util.List;

import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.OffsetPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.PegKey;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedAbstractPlanUnit;
import asap.realizer.scheduler.TimePegAndConstraint;

import com.google.common.collect.ImmutableMap;

public abstract class LMP extends TimedAbstractPlanUnit implements TimedAnimationUnit
{
    protected final PegBoard pegBoard;

    public LMP(FeedbackManager fbm, BMLBlockPeg bmlPeg, String bmlId, String behId, PegBoard pegBoard)
    {
        super(fbm, bmlPeg, bmlId, behId, true);
        this.pegBoard = pegBoard;
    }

    @Override
    public List<String> getAvailableSyncs()
    {
        List<String> syncs = new ArrayList<>();
        ImmutableMap<PegKey, TimePeg> pegs = pegBoard.getTimePegs();
        for (PegKey pk : pegs.keySet())
        {
            if (pk.getBmlId().equals(getBMLId()) && pk.getId().equals(getId()))
            {
                syncs.add(pk.getSyncId());
            }
        }
        return syncs;
    }

    protected void createPegWhenMissingOnPegBoard(String syncId)
    {
        if (pegBoard.getTimePeg(getBMLId(), getId(), syncId) == null)
        {
            TimePeg tp = new TimePeg(getBMLBlockPeg());
            pegBoard.addTimePeg(getBMLId(), getId(), syncId, tp);
        }

    }

    @Override
    public double getStartTime()
    {
        return pegBoard.getPegTime(getBMLId(), getId(), "start");
    }

    @Override
    public double getEndTime()
    {
        return pegBoard.getPegTime(getBMLId(), getId(), "end");
    }

    @Override
    public double getRelaxTime()
    {
        if (pegBoard.getPegTime(getBMLId(), getId(), "relax") != TimePeg.VALUE_UNKNOWN)
        {
            return pegBoard.getPegTime(getBMLId(), getId(), "relax");
        }
        else if (pegBoard.getPegTime(getBMLId(), getId(), "strokeEnd") != TimePeg.VALUE_UNKNOWN)
        {
            return pegBoard.getPegTime(getBMLId(), getId(), "strokeEnd");
        }
        return getEndTime();
    }

    @Override
    public TimePeg getTimePeg(String syncId)
    {
        return pegBoard.getTimePeg(getBMLId(), getId(), syncId);
    }

    @Override
    public void setTimePeg(String syncId, TimePeg peg)
    {
        pegBoard.addTimePeg(getBMLId(), getId(), syncId, peg);
    }

    // TODO: more or less duplicate with LinearStretchResolver, ProcAnimationGestureTMU
    protected void linkSynchs(List<TimePegAndConstraint> sacs)
    {
        for (TimePegAndConstraint s : sacs)
        {
            for (String syncId : getAvailableSyncs())
            {
                if (s.syncId.equals(syncId) && !syncId.equals("start") && !syncId.equals("end"))
                {
                    if (s.offset == 0)
                    {
                        setTimePeg(syncId, s.peg);
                    }
                    else
                    {
                        setTimePeg(syncId, new OffsetPeg(s.peg, -s.offset));
                    }
                }
            }
        }
    }
}
