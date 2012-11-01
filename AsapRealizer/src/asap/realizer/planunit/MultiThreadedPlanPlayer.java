/*******************************************************************************
 * Copyright (C) 2009 Human Media Interaction, University of Twente, the Netherlands
 * 
 * This file is part of the Elckerlyc BML realizer.
 * 
 * Elckerlyc is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Elckerlyc is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Elckerlyc.  If not, see http://www.gnu.org/licenses/.
 ******************************************************************************/
package asap.realizer.planunit;

import hmi.util.RuntimeExceptionLoggingRunnable;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import saiba.bml.feedback.BMLWarningFeedback;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.feedback.NullFeedbackManager;
import asap.realizerport.BMLFeedbackListener;

import com.google.common.collect.ImmutableList;

/**
 * Runs a Plan in a separate thread. This is useful for TimedPlanUnits for which
 * their execution is blocking (e.g. direct speech). For rapid execution of new
 * TimedPlanUnits, the MultiThreadedPlanPlayer always has a thread available in which the
 * plan can be run. Use shutdown to close the MultiThreadedPlanPlayer completely and
 * get rid of its thread. The MultiThreadedPlanPlayer should not be reused after
 * shutdown.
 * 
 * @author Herwin
 * @param <T>
 */
public class MultiThreadedPlanPlayer<T extends TimedPlanUnit>  implements PlanPlayer
{
    private final PlanManager<T> planManager;

    private final FeedbackManager fbManager;

    private Future<?> planRunner = null;

    private final ExecutorService exec = Executors.newSingleThreadExecutor();

    private static Logger logger = LoggerFactory.getLogger(MultiThreadedPlanPlayer.class.getName());

    private BlockingQueue<Double> timeQueue = new ArrayBlockingQueue<Double>(10);
    
    public MultiThreadedPlanPlayer(FeedbackManager bbm, PlanManager<T> planManager)
    {
        fbManager = bbm;
        this.planManager = planManager;
    }

    public MultiThreadedPlanPlayer(PlanManager<T> planManager)
    {
        this(NullFeedbackManager.getInstance(), planManager);
    }

    public void setBMLBlockState(String bmlId, TimedPlanUnitState state)
    {
        planManager.setBMLBlockState(bmlId, state);
    }

    @Override
    public void stopBehaviourBlock(String bmlId, double time)
    {
        planManager.stopBehaviourBlock(bmlId, time);
    }

    @Override
    public void interruptBehaviourBlock(String bmlId, double time)
    {
        planManager.interruptBehaviourBlock(bmlId, time);
    }
    
    public int getNumberOfPlanUnits()
    {
        return planManager.getNumberOfPlanUnits();
    }

    public void stopPlanUnit(String bmlId, String id, double globalTime)
    {
        planManager.stopPlanUnit(bmlId, id, globalTime);
    }
    
    public void interruptPlanUnit(String bmlId, String id, double globalTime)
    {
        planManager.interruptPlanUnit(bmlId, id, globalTime);
    }

    public void play(double t)
    {
        logger.debug("enter play");
        try
        {
            timeQueue.put(t);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }
        if (planRunner == null || planRunner.isDone())
        {
            logger.debug("Creating new SpeechRunner");
            if (planRunner != null)
            {
                logger.debug("speechRunner.iDone(): {}", planRunner.isDone());
            }
            planRunner = exec.submit(new RuntimeExceptionLoggingRunnable(new SpeechRunner()));
        }
    }

    /**
     * Shutdown the VerbalPlanPlayer thread
     */
    @Override
    public void shutdown()
    {
        scheduleStop();
        exec.shutdown();
        try
        {
            exec.awaitTermination(1, TimeUnit.SECONDS);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            logger.warn("InterruptedException on MultiThreadedPlanPlayer shutdown:", e);
        }
    }

    public void scheduleStop()
    {
        try
        {
            timeQueue.put(-1d);
        }
        catch (InterruptedException e1)
        {
            Thread.currentThread().interrupt();
        }
        if (planRunner != null)
        {
            try
            {
                planRunner.get();
            }
            catch (InterruptedException e)
            {
                Thread.interrupted();
            }
            catch (ExecutionException e)
            {
                logger.warn("Execution Exception when trying to stop VerbalPlanPlayer thread: {}", e);
            }
        }        
    }

    /**
     * Stops all speechunits, then removes them from the plan
     * 
     * @param time
     *            stop time
     */
    public void reset(double time)
    {
        planManager.removeAllPlanUnits(time);
        logger.debug("Schedule VPP stop");
        scheduleStop();
        logger.debug("VPP cleared");
    }

    /**
     * Callback for behaviors
     */
    private void suException(TimedPlanUnit su, String message, double time)
    {
        logger.debug("suException with {}:{}", su.getBMLId(), su.getId());
        String bmlId = su.getBMLId();
        String warningText = message + "\nBehavior " + su.getBMLId() + ":" + su.getId() + " dropped.";
        warning(new BMLWarningFeedback(bmlId, "EXECUTION_EXCEPTION", warningText));
    }

    protected void playUnit(T su, double t) throws TimedPlanUnitPlayException
    {
        if (su.getState().isLurking())
        {
            su.start(t);
        }
        if (su.getState().isPlaying())
        {
            su.play(t);
        }
    }

    class SpeechRunner implements Runnable
    {
        @Override
        public void run()
        {
            logger.debug("start multithreadedplanplayer");
            ImmutableList<T> planUnitsCache;
            ArrayList<T> planUnitsRemove = new ArrayList<T>();
            double time = 0;
            
            while (true)
            {
                try
                {
                    time = timeQueue.take();
                }
                catch (InterruptedException e1)
                {
                    Thread.currentThread().interrupt();
                }
                if(time==-1)break;
                
                // synchronized copy trick
                planUnitsCache = planManager.getPlanUnits();

                planUnitsRemove.clear();
                for (T su : planUnitsCache)
                {
                    if (time >= su.getStartTime())
                    {

                        try
                        {
                            playUnit(su, time);
                        }
                        catch (TimedPlanUnitPlayException e)
                        {
                            logger.debug("TimedPlanUnitPlayException!");
                            suException(su, e.getLocalizedMessage(), time);
                            planUnitsRemove.add(su);
                        }
                    }
                }
                planManager.removePlanUnits(planUnitsRemove, time);
                planManager.removeFinishedPlanUnits();                
            }
            logger.debug("stopped verbalplanplayer");
        }
    }

    public void warning(BMLWarningFeedback e)
    {
        fbManager.warn(e);
    }
    
    public void addFeedbackListener(BMLFeedbackListener ws)
    {
        fbManager.addFeedbackListener(ws);
    }

}
