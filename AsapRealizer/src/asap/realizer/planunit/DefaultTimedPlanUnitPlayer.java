/*******************************************************************************
 *******************************************************************************/
package asap.realizer.planunit;


import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Plays a TimedPlanUnit at time t. Not threadsafe.
 * @author welberge
 */
public class DefaultTimedPlanUnitPlayer extends AbstractTimedPlanUnitPlayer
{
    public DefaultTimedPlanUnitPlayer()
    {
        playExceptions = Lists.newArrayList();
        stopExceptions = Lists.newArrayList();
    }
    
    public void playUnit(TimedPlanUnit su, double t)
    {
        try
        {
            if (su.getState().isLurking())
            {
                su.start(t);
            }       
            su.play(t);
        }
        catch (TimedPlanUnitPlayException e)
        {
            playExceptions.add(e);
        }
    }

    @Override
    public ImmutableCollection<TimedPlanUnitPlayException> getPlayExceptions()
    {
        return new ImmutableList.Builder<TimedPlanUnitPlayException>().addAll(playExceptions).build();
    }
    
    @Override
    public void stopUnit(TimedPlanUnit su, double t)
    {
        try
        {
            su.stop(t);
        }
        catch (TimedPlanUnitPlayException e)
        {
            stopExceptions.add(e);
        }        
    }

    @Override
    public ImmutableCollection<TimedPlanUnitPlayException> getStopExceptions()
    {
        return new ImmutableList.Builder<TimedPlanUnitPlayException>().addAll(stopExceptions).build(); 
    }    
}
