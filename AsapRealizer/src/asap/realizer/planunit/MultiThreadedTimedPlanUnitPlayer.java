/*******************************************************************************
 *******************************************************************************/
package asap.realizer.planunit;

import hmi.util.RuntimeExceptionLoggingRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

/**
 * Plays or stops a TimedPlanUnit at time t. This playback/stopping is ran in a separate thread.
 * The MultiThreadedTimedPlanUnitPlayer makes sure that the same TimedPlanUnit is not played/stopped twice (or more often)
 * concurrently.
 * @author welberge
 */
public class MultiThreadedTimedPlanUnitPlayer extends AbstractTimedPlanUnitPlayer
{
    private final static int NUM_THREADS = 25;
    private Map<TimedPlanUnit, CountDownLatch> playMap = new HashMap<TimedPlanUnit, CountDownLatch >();
    private final ExecutorService exec = Executors.newFixedThreadPool(NUM_THREADS);
    
    public MultiThreadedTimedPlanUnitPlayer()
    {
        playExceptions = Collections.synchronizedList(new ArrayList<TimedPlanUnitPlayException>());
        stopExceptions = Collections.synchronizedList(new ArrayList<TimedPlanUnitPlayException>());    
    }
    
    public void playUnit(TimedPlanUnit su, double t)
    {
        CountDownLatch prevLatch = playMap.get(su);
        CountDownLatch curLatch = new CountDownLatch(1);
        playMap.put(su, curLatch);
        exec.submit(new RuntimeExceptionLoggingRunnable(new TPUPlayRunner(su,t,prevLatch,curLatch)));        
    }
    
    @Override
    public void stopUnit(TimedPlanUnit su, double t)
    {
        CountDownLatch prevLatch = playMap.get(su);
        CountDownLatch curLatch = new CountDownLatch(1);
        playMap.put(su, curLatch);
        exec.submit(new RuntimeExceptionLoggingRunnable(new TPUStopRunner(su,t,prevLatch,curLatch)));            
    }
    
    class TPUStopRunner implements Runnable
    {
        private final double time;
        private final TimedPlanUnit tpu;
        private final CountDownLatch prevLatch;
        private final CountDownLatch curLatch;
        
        public TPUStopRunner(TimedPlanUnit tpu, double t, CountDownLatch prev, CountDownLatch cur)
        {
            this.tpu = tpu;
            this.time = t;
            prevLatch = prev;
            curLatch = cur;
        }
        @Override
        public void run()
        {
            if(prevLatch!=null)
            {
                try
                {
                    prevLatch.await();
                }
                catch (InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                }
            }
            try
            {
                tpu.stop(time);
            }
            catch (TimedPlanUnitPlayException e)
            {
                stopExceptions.add(e);
            }
            curLatch.countDown();
            
        }        
    }
    
    class TPUPlayRunner implements Runnable
    {
        private final double time;
        private final TimedPlanUnit tpu;
        private final CountDownLatch prevLatch;
        private final CountDownLatch curLatch;
        
        public TPUPlayRunner(TimedPlanUnit tpu, double t, CountDownLatch prev, CountDownLatch cur)
        {
            this.tpu = tpu;
            this.time = t;
            prevLatch = prev;
            curLatch = cur;
        }

        @Override
        public void run()
        {
            if(prevLatch!=null)
            {
                try
                {
                    prevLatch.await();
                }
                catch (InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                }
            }
            try
            {
                if (tpu.getState().isLurking())
                {
                    tpu.start(time);
                }
                tpu.play(time);
            }
            catch (TimedPlanUnitPlayException e)
            {
                playExceptions.add(e);
            }
            curLatch.countDown();
        }
    }

    @Override
    public ImmutableCollection<TimedPlanUnitPlayException> getPlayExceptions()
    {
        ImmutableList<TimedPlanUnitPlayException>ex;
        synchronized(playExceptions)
        {
            ex = new ImmutableList.Builder<TimedPlanUnitPlayException>().addAll(playExceptions).build();
        }
        return ex;
    }

    @Override
    public ImmutableCollection<TimedPlanUnitPlayException> getStopExceptions()
    {
        ImmutableList<TimedPlanUnitPlayException>ex;
        synchronized(stopExceptions)
        {
            ex = new ImmutableList.Builder<TimedPlanUnitPlayException>().addAll(stopExceptions).build();
        }
        return ex;
    }    
}
