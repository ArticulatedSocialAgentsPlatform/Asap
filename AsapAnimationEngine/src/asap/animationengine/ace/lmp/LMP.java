package asap.animationengine.ace.lmp;

import java.util.ArrayList;
import java.util.List;

import saiba.bml.core.Behaviour;
import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.AfterPeg;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.BeforePeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.PegKey;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedAbstractPlanUnit;
import asap.realizer.scheduler.TimePegAndConstraint;

import com.google.common.collect.ImmutableMap;

/**
 * Local motor program skeleton implementation
 * @author hvanwelbergen
 *
 */
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

    protected void linkMCPSynchs(String mcpId)
    {
        for(String mcpSync: pegBoard.getSyncs(getBMLId(), mcpId))
        {
            for(String sync:getAvailableSyncs())
            {
                if(mcpSync.equals(sync) && !mcpSync.equals("start") && !mcpSync.equals("end"))
                {
                    setTimePeg(sync, pegBoard.getTimePeg(getBMLId(),mcpId,sync));
                }
            }
        }
    }

    protected boolean noPegsSet()
    {
        for (TimePeg tp : pegBoard.getTimePegs(getBMLId(), getId()))
        {
            if (tp.getGlobalValue() != TimePeg.VALUE_UNKNOWN)
            {
                return false;
            }
        }
        return true;
    }

    protected void setTpMinimumTime(double time)
    {

        for (TimePeg tp : pegBoard.getTimePegs(getBMLId(), getId()))
        {
            if (tp.getGlobalValue() != TimePeg.VALUE_UNKNOWN && !tp.isAbsoluteTime())
            {
                if (tp.getGlobalValue() < time)
                {
                    tp.setGlobalValue(time);
                }
            }
        }
    }

    @Override
    public void resolveSynchs(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sac) throws BehaviourPlanningException
    {
        
    }        
    
    protected abstract void setInternalStrokeTiming(double time);
    
    protected void resolveTimePegs(double time)
    {
        if(!getTimePeg("start").isAbsoluteTime())
        {
            pegBoard.setPegTime(getBMLId(), getId(), "start", getTimePeg("strokeStart").getGlobalValue() - getPreparationDuration());
        }
        pegBoard.setPegTime(getBMLId(), getId(), "end", getTimePeg("strokeEnd").getGlobalValue() + getRetractionDuration());

        setInternalStrokeTiming(time);

        if (!isPlaying())
        {
            setTpMinimumTime(time);
        }
    }
    
    protected void resolveInternal(String mcpId, BMLBlockPeg bbPeg)
    {
        linkMCPSynchs(mcpId);        
        TimePeg strokeStartPeg = getTimePeg("strokeStart");        
        TimePeg mcpStartPeg = pegBoard.getTimePeg(getBMLId(), mcpId, "start");
        TimePeg startPeg = new AfterPeg(mcpStartPeg,0,bbPeg);
        double startValue = strokeStartPeg.getGlobalValue()-getPreparationDuration();
        if(startValue>mcpStartPeg.getGlobalValue())
        {
            startPeg.setGlobalValue(startValue);
        }
        else
        {
            startPeg.setGlobalValue(mcpStartPeg.getGlobalValue());
        }
        setTimePeg("start",startPeg);
        
        TimePeg strokeEndPeg = getTimePeg("strokeEnd");      
        TimePeg mcpEndPeg = pegBoard.getTimePeg(getBMLId(), mcpId, "end");
        TimePeg endPeg = new BeforePeg(mcpEndPeg, 0, bbPeg);
        double endValue = strokeEndPeg.getGlobalValue()+getRetractionDuration();
        if(endValue<mcpEndPeg.getGlobalValue())
        {
            endPeg.setGlobalValue(endValue);
        }
        else
        {
            endPeg.setGlobalValue(mcpEndPeg.getGlobalValue());
        }
        setTimePeg("end",endPeg);
        
        resolveTimePegs(0);
    }
}
