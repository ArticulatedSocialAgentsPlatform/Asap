/*******************************************************************************
 *******************************************************************************/
package asap.environment;

import hmi.environmentbase.Environment;
import hmi.util.Clock;
import hmi.util.ClockListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import net.jcip.annotations.GuardedBy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asap.realizer.Engine;

/**
 * 
 * @author Dennis Reidsma
 */
public class AsapEnvironment implements Environment, ClockListener
{
    @Getter
    @Setter
    String id = "asapenvironment";

    private Logger logger = LoggerFactory.getLogger(AsapEnvironment.class.getName());

    /** All loaded virtualHumans */
    protected HashMap<String, AsapVirtualHuman> virtualHumans = new HashMap<String, AsapVirtualHuman>();

    /** All environments */
    protected Environment[] environments = new Environment[0];
    /** all engines */
    protected ArrayList<Engine> engines = new ArrayList<Engine>();

    protected Clock schedulingClock;

    @GuardedBy("itself")
    protected List<Runnable> asapRunners = Collections.synchronizedList(new ArrayList<Runnable>());

    protected Object shutdownSync = new Object();

    protected volatile boolean shutdownPrepared = false;

    /**
     * @param schedulingClock
     *            Typically, the PhysicsSchedulingClock(physicsClock).
     *            If no physics, typically use the renderClock. If no renderClock, use a new clock
     */
    public void init(ArrayList<Environment> environments, Clock schedulingClock)
    {
        logger.debug("Initializing AsapEnvironment");
        synchronized (shutdownSync)
        {
            if (shutdownPrepared) return;
            if (!environments.contains(this)) environments.add(this);
            this.environments = new Environment[environments.size()];
            for (int i = 0; i < environments.size(); i++)
            {
                this.environments[i] = environments.get(i);
            }

            this.schedulingClock = schedulingClock;
        }
    }

    /**
     * run the engines at time tick.
     * Somewhere else, AsapEnvironment is registered at a clock to get this call back. If a physicsEnvironment
     * is in use, this is typically as a PrePhysicsCopyListener.
     * At physics time, after simulation, but just before physics is copied to the body, we run the engines here!
     * if no physics is used, AsapEnvronment should simply be registered at any clock that will drive the Engines
     * (rendering clock, separate animation clock, ...)
     */
    @Override
    public void time(double currentTime)
    {
        synchronized (shutdownSync)
        {
            if (shutdownPrepared) return;
            for (Engine e : engines)
            {
                e.play(currentTime);
            }
        }
    }

    @Override
    public void initTime(double currentTime)
    {
        synchronized (shutdownSync)
        {
            if (shutdownPrepared) return;
            for (Engine e : engines)
            {
                e.play(currentTime);
            }
        }
    }

    public AsapVirtualHuman loadVirtualHuman(String id, String resources, String fileName, String name) throws IOException
    {
        synchronized (shutdownSync)
        {
            if (shutdownPrepared) return null;
            AsapVirtualHuman avh = new AsapVirtualHuman();
            avh.setVhId(id);
            avh.load(resources, fileName, name, environments, schedulingClock);
            // no need to add anywhere; this will be done by the loaders...
            return avh;
        }
    }

    public AsapVirtualHuman loadVirtualHuman(String resources, String fileName, String name) throws IOException
    {
        return loadVirtualHuman("", resources, fileName, name);
    }

    public void addVirtualHuman(AsapVirtualHuman avh)
    {
        synchronized (shutdownSync)
        {
            if (shutdownPrepared) return;
            if (virtualHumans.containsKey(id)) throw new RuntimeException("Duplicate id for virtual human!");
            synchronized (virtualHumans)
            {
                virtualHumans.put(avh.getVhId(), avh);
                engines.addAll(avh.getEngines());
            }
        }
    }

    public void removeVirtualHuman(AsapVirtualHuman avh)
    {
        synchronized (shutdownSync)
        {
            if (shutdownPrepared) return;
            synchronized (virtualHumans)
            {
                virtualHumans.remove(avh.getVhId());
                engines.removeAll(avh.getEngines());
            }
        }
    }

    private void unloadAllVirtualHumans()
    {
        synchronized (shutdownSync)
        {
            if (shutdownPrepared) return;
            ArrayList<AsapVirtualHuman> vhList = new ArrayList<AsapVirtualHuman>();
            vhList.addAll(virtualHumans.values());
            for (AsapVirtualHuman avh : vhList)
            {
                logger.info("Disposing humanoid {}", avh.getName());
                avh.unload();
            }
        }
    }

    /** send clearBML to all vh's, reset all engines */
    protected void reset()
    {
        synchronized (shutdownSync)
        {
            if (shutdownPrepared) return;
            for (AsapVirtualHuman vh : virtualHumans.values())
            {
                vh.getRealizerPort().performBML(
                        "<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" " + "id=\"clear\" composition=\"REPLACE\"></bml>");
            }
            /*
             * For some reason the first execution of the physics thread after resetting the clock to 0 is quite late (100ms or so).
             * This screws up the timing of (ballistic) TTS if some speech is to start at t=0.
             * Therefore all players are executed once here at t=0.
             */
            for (Engine e : engines)
            {
                e.play(0);
            }
        }
    }

    @Override
    public void requestShutdown()
    {
        synchronized (shutdownSync)
        {
            if (shutdownPrepared) return;
            logger.info("Prepare shutdown of AsapEnvironment...");
            logger.info("Unload all Virtual Humans...");
            unloadAllVirtualHumans();
            logger.debug("Virtual Humans unloaded");

            logger.info("Prepare shutdown of all environments...");
            for (Environment e : environments)
            {
                logger.debug("Shutting down " + e.getClass());
                if (e != this) e.requestShutdown();
            }
            shutdownPrepared = true;
        }
    }

    @Override
    public boolean isShutdown()
    {
        synchronized (shutdownSync)
        {
            for (Environment e : environments)
            {
                if (e != this) if (!e.isShutdown()) return false;
            }
            return true;
        }
    }

}
