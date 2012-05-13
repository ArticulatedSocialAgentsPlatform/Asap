package hmi.bml.bridge;

import saiba.bml.bridge.RealizerPort;
import saiba.bml.feedback.BMLListener;
import hmi.util.RuntimeExceptionLoggingRunnable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author welberge,reidsma
 */
public class LinkedBlockingQueuePipe implements RealizerPort
{
    private final RealizerPort outBridge;

    private final LinkedBlockingQueue<String> bmlRequestQueue = new LinkedBlockingQueue<String>();

    private final ExecutorService exec = Executors.newSingleThreadExecutor();

    private Future<?> bmlPerformRunner = null;

    private final Logger logger = LoggerFactory.getLogger(LinkedBlockingQueuePipe.class.getName());

    public LinkedBlockingQueuePipe(RealizerPort out)
    {
        outBridge = out;
    }

    @Override
    public void addListeners(BMLListener... listeners)
    {
        outBridge.addListeners(listeners);
    }

    @Override
    public void removeAllListeners()
    {
        outBridge.removeAllListeners();
    }

    @Override
    public void performBML(String bmlString)
    {
        if (bmlPerformRunner == null || bmlPerformRunner.isDone())
        {
            logger.debug("Creating new BMLRunner prev bmlPerformRunner: {} prevInterrupted: {}", bmlPerformRunner,
                    bmlPerformRunner != null ? bmlPerformRunner.isDone() : false);
            bmlPerformRunner = exec.submit(new RuntimeExceptionLoggingRunnable(new BMLPerformRunner()));
        }
        try
        {
            bmlRequestQueue.put(bmlString);
        }
        catch (InterruptedException e)
        {
            Thread.interrupted();
        }
    }

    private class BMLPerformRunner implements Runnable
    {
        /** take requests from queue and schedule them */
        @Override
        public void run()
        {
            String blockContent = null;
            while (true)
            {
                try
                {
                    blockContent = bmlRequestQueue.poll(1000, TimeUnit.MILLISECONDS);
                }
                catch (InterruptedException e)
                {
                    logger.debug("interrupted");
                    Thread.interrupted();
                }
                if (blockContent == null) continue;
                logger.debug("Sending block {}", blockContent);
                try
                {
                    outBridge.performBML(blockContent);
                }
                catch (Exception ex)
                {
                    logger.warn("Error performing BML: {}", ex);
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    public void stopRunning()
    {
        exec.shutdown();
    }
}
