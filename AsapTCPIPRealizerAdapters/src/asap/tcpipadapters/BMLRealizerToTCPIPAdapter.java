/*******************************************************************************
 *******************************************************************************/
package asap.tcpipadapters;

import hmi.xml.XMLScanException;
import hmi.xml.XMLTokenizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.channels.IllegalBlockingModeException;
import java.util.concurrent.ConcurrentLinkedQueue;

import lombok.extern.slf4j.Slf4j;
import saiba.bml.feedback.BMLWarningFeedback;
import asap.realizerport.BMLFeedbackListener;
import asap.realizerport.RealizerPort;
import asap.realizerport.util.BMLFeedbackManager;

import com.google.common.base.Charsets;

/**
 * A {@link asap.realizerport.bml.bridge.RealizerPort RealizerBridge} that uses a tcp/ip connection to provide
 * transparent access to a BML Realizer running on a remote machine. The connection is
 * "self-healing".
 * 
 * Detailed documentation can be found in the project report.
 * 
 * @author Dennis Reidsma
 */
@Slf4j
public final class BMLRealizerToTCPIPAdapter implements RealizerPort, Runnable
{
    
    private BMLFeedbackManager fbManager = new BMLFeedbackManager();
    /*
     * ========================================================================================= A
     * FEW GLOBAL STATIC PARAMETERS THAT YOU MIGHT WANT TO PLAY AROUND WITH
     * =========================================================================================
     */

    /** Sleeping time of the FeedbackRedirector if the feedback queue is empty */
    private static final long FEEDBACK_REDIRECT_WAIT_MILLI = 100;

    private static final int SOCKET_TIMEOUT = 500;

    private static final int CONNECT_RETRY_WAIT = 1000;

    // private static final int WAIT_IF_NO_FEEDBACK = 100;
    /*
     * =========================================================================================
     * EXTERNAL ACCESS: CONTROLLING THE NETWORK CONNECTION
     * =========================================================================================
     */

    /**
     * Instigates disconnection procedure. After disconnecting, client goes into a waiting state,
     * and may be asked to connect to a server again at a later time.
     */
    public void disconnect()
    {
        synchronized (connectionLock)
        {
            mustdisconnect = true;
        }
    }

    /**
     * May return before connection is actually made. However, implementation guarantees that
     * performBML request made AFTER connect request, will be sent over that connection (if the
     * connection is successful)
     */
    public void connect(ServerInfo serverInfo)
    {
        synchronized (connectionLock)
        {
            mustconnect = true;
            this.serverInfo = serverInfo;
            if (isconnected) mustdisconnect = true; // clean up old connection before making new one
                                                    // :)
        }
    }

    protected ServerInfo getServerInfo()
    {
        return serverInfo.copy();
    }

    /**
     * Instigates total shutdown. May return from this method before shutdown is completed. Shutdown
     * process will terminate all connections and threads for this client.
     */
    public void shutdown()
    {
        synchronized (connectionLock)
        {
            mustshutdown = true;
        }
    }

    /** Returns true iff there is a connection to a server active (and has not been lost). */
    public boolean isConnected()
    {
        synchronized (connectionLock)
        {
            return isconnected;
        }
    }

    /*
     * =========================================================================================
     * EXTERNAL ACCESS: REALIZERBRIDGE API
     * =========================================================================================
     */

    /** Listeners must be stored; they will get updates from the feedbackRedirectionThread */
    @Override
    public void addListeners(BMLFeedbackListener... bmlListeners)
    {
        synchronized (feedbackLock)
        {
            fbManager.addListeners(bmlListeners);
        }
    }

    @Override
    public void removeAllListeners()
    {
        synchronized (feedbackLock)
        {
            fbManager.removeAllListeners();
        }
    }
    
    @Override
    public void removeListener(BMLFeedbackListener l)
    {
        synchronized (feedbackLock)
        {
            fbManager.removeListener(l);
        }        
    }
    
    /** Add BML request to the queue and return. Sending will happen in the main networking loop. */
    @Override
    public void performBML(String bmlString)
    {
        boolean send = true;
        // if there is no connection, and no connection is being prepared, drop the request.
        synchronized (connectionLock)
        {
            if (!isconnected && !mustconnect)
            {
                send = false;
            }
        }
        if (send)
        {
            log.debug("adding BML {}", bmlString);
            bmlRequestQ.add(bmlString);
        }
        else
        {
            // log failure; send feedback
            BMLWarningFeedback feedback = new BMLWarningFeedback("", "CANNOT_SEND", "Failure to send BML: no connection to BML Realizer Server.");
            synchronized (feedbackLock)
            {
                fbManager.sendFeedback(feedback.toXMLString());
            }
        }
    }

    /*
     * ========================================================================================= THE
     * MAIN NETWORKING PART: BML REQUEST QUEUE AND STATE VARIABLES
     * =========================================================================================
     */

    /**
     * Incoming BML Requests from the application are stored here. The main networking loop will get
     * these requests and send them to the server if a connection is available.
     */
    private ConcurrentLinkedQueue<String> bmlRequestQ = new ConcurrentLinkedQueue<String>();

    /** Locking object for the states of the main networking loop. */
    private Object connectionLock = new Object();

    /** Internal state var: true if a conenction to a server is active */
    private boolean isconnected = false;

    /** Internal state var: true if a shutdown request has been given */
    private boolean mustshutdown = false;

    /** Internal state var: true if a connect request has been given */
    private boolean mustconnect = false;

    /**
     * Internal state var: The ServerInfo for the connection that must be set up (see #mustconnect).
     */
    private ServerInfo serverInfo = null;

    /** Internal state var: true if a disconnect request has been given */
    private boolean mustdisconnect = false;

    /** Waiting time for next run() loop. */
    private long nextMainLoopWait = 100;

    /*
     * ========================================================================================= THE
     * MAIN NETWORKING PART: NETWORKING CONNECTIONS
     * =========================================================================================
     */
    private InetSocketAddress bmlSendSocketAddress = null;

    private Socket bmlSendSocket = null;

    private PrintWriter bmlSendWriter = null;

    private ServerSocket feedbackReadServerSocket = null;

    private Socket feedbackReadSocket = null;

    private BufferedReader feedbackReadReader = null;

    private FeedbackReader feedbackReader = null;

    private Thread feedbackReaderThread = null;

    /*
     * ========================================================================================= THE
     * MAIN NETWORKING PART: THE FEEDBACK READING PROCESS
     * =========================================================================================
     */

    /**
     * The process that reads feedback from the network connection, and puts it in the feedback
     * queue. If conencting this reader leads to serious exceptions, a disconnect is requested.
     */
    private class FeedbackReader implements Runnable
    {
        public FeedbackReader()
        {
        }

        private boolean stop = false;

        public void stopReading()
        {
            stop = true;
        }

        public void run()
        {
            log.debug("Starting feedback reader");
            ServerInfo info = getServerInfo();
            // attempt to connect until successful or until serious error
            try
            {
                feedbackReadServerSocket = new ServerSocket(info.getFeedbackPort());
            }
            catch (IOException e)
            {
                failFeedbackConnect(e.getMessage());
                return;
            }
            log.debug("Server socket ready");
            try
            {
                feedbackReadServerSocket.setSoTimeout(SOCKET_TIMEOUT);
            }
            catch (SocketException e)
            {
                failFeedbackConnect(e.getMessage());
                return;
            }
            boolean feedbackconnected = false;
            while (!feedbackconnected)
            {
                try
                {
                    log.debug("Waiting for server to connect to feedback channel");
                    feedbackReadSocket = feedbackReadServerSocket.accept();
                    feedbackconnected = true;
                    log.debug("Making BufferedReader");
                    feedbackReadReader = new BufferedReader(new InputStreamReader(feedbackReadSocket.getInputStream(), Charsets.UTF_8));
                }
                catch (SocketTimeoutException e)
                {
                    retryFeedbackConnect("Timeout while attempting to connect to feedback channel.");
                }
                catch (IllegalBlockingModeException e)
                {
                    failFeedbackConnect(e.getMessage());
                    return;
                }
                catch (IllegalArgumentException e)
                {
                    failFeedbackConnect(e.getMessage());
                    return;
                }
                catch (IOException e)
                {
                    failFeedbackConnect(e.getMessage());
                    return;
                }
                if (!feedbackconnected)
                {
                    try
                    {
                        Thread.sleep(CONNECT_RETRY_WAIT);
                    }
                    catch (InterruptedException ex)
                    {
                        // nothing
                    }
                }
            }
            log.debug("Feedback channel open, starting to read");
            // feedback channel is connected. Keep reading from it, and processing what comes in
            
            XMLTokenizer tok = new XMLTokenizer(feedbackReadReader);
            
            while (!stop)
            {
                // logger.debug("Still reading feedback...");
                try
                {
                    /*
                     * if (tok.atEndOfDocument()) { if (feedbackReadSocket.isClosed()) {
                     * mustdisconnect=true; stop=true; continue; } System.out.println(
                     * "Waiting for new feedback to come in over network... 
                     * HERE I NEED TO TEST IF WE STRILL HAVE A SOCKET! IF THAT WAS DISAPPEARED, I NEED TO CLEAN UP CLIENT. 
                     * BECAUSE THIS IS THE ONLY PLACE WHERE I CAN FIND OUT THAT SERVER DISAPPEARED, UNTIL I TRY TO SEND BML!!"
                     * ); try { Thread.sleep(WAIT_IF_NO_FEEDBACK); } catch(InterruptedException ex)
                     * { } continue; }
                     */
                    String feedback = tok.getXMLSection();
                    log.debug("Adding feedback {}",feedback);
                    feedbackQ.add(feedback);
                    // feedbackRedirectorThread.interrupt() when new feedback was put in queue!
                    // let op SecurityException                    
                }
                catch (XMLScanException e)
                {
                    //XMLScanExceptions may occur when disconnecting/stopping
                    if(!stop)
                    {
                        throw e;
                    }
                }
                catch (IOException e)
                {
                    log.warn("Error reading feedback from server, error: {}. Disconnecting from server.", e.getMessage());
                    stop = true;
                    mustdisconnect = true;
                    mustconnect = true; // see if we can restore connection
                    nextMainLoopWait = 1;
                }
            }
            log.debug("Leaving the feedback channel reader");
        }

        private void failFeedbackConnect(String msg)
        {
            log.warn("Failed to connect feedback reader to server {}: {}.  Disconnecting from server.", getServerInfo(), msg);
            mustdisconnect = true; // don't restore connection if we get an immediate fail on
                                   // opening feedback channel...
            nextMainLoopWait = 1;
        }

        private void retryFeedbackConnect(String msg)
        {
            log.debug("Problem connecting to feedback channel: {}\n" + "Will try again in {} msec...", msg, CONNECT_RETRY_WAIT);
        }

    }

    /*
     * ========================================================================================= THE
     * MAIN NETWORKING PART: THE MAIN LOOP
     * =========================================================================================
     */

    /** The main loop! */
    public void run()
    {
        while (true)
        {
            synchronized (connectionLock)
            {
                // if nothing to do, wait a bit before next loop run.
                nextMainLoopWait = 100;
                if (mustdisconnect)
                {
                    dodisconnect(); // disconnect before connect -- sometimes connect requires
                                    // disconnect of old connection
                }
                if (mustconnect)
                {
                    doconnect();
                }
                if (mustshutdown)
                {
                    doshutdown();
                }
                if (isconnected)
                {
                    String nextBMLRequest = bmlRequestQ.poll();
                    if (nextBMLRequest != null)
                    {
                        dosendBML(nextBMLRequest);
                    }
                }
            }
            if (mustshutdown) break;
            try
            {
                Thread.sleep(nextMainLoopWait);
            }
            catch (InterruptedException ex)
            {
                // no matter -- just continue with next round :)
            }
        }
        log.debug("Client shutdown finished");
    }

    /**
     * Disconnect from server (if connected); close feedback reading thread; return when processes
     * finished. Called from the run() loop.
     */
    private void dodisconnect()
    {
        log.debug("Starting to disconnect from server");
        feedbackReader.stopReading();
        log.debug("Waiting for feedbackreader to end...");
        try
        { // wait till feedbackreader stopped
            if (feedbackReaderThread != null) feedbackReaderThread.join();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        // close the sockets etc
        log.debug("Trying to close bml sending sockets...");
        try
        {
            if (bmlSendSocket != null) bmlSendSocket.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        log.debug("Trying to close feedback reading socket...");
        try
        {
            if (feedbackReadSocket != null) feedbackReadSocket.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        try
        {
            if (feedbackReadServerSocket != null) feedbackReadServerSocket.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        mustdisconnect = false;
        isconnected = false;
        bmlRequestQ.clear();
        nextMainLoopWait = 1;
        log.debug("Disconnected from server");
    }

    /**
     * Attempt to connect to server, given ServerInfo. Start feedback reading loop. Log results. If
     * successful, isconnected is true; else, clean up everything, try again enxt round with wait =
     * 1000. Called from the run() loop.
     */
    private void doconnect()
    {
        log.debug("Connecting to server...");
        ServerInfo info = getServerInfo();
        if (info == null || info.getServerName() == null)
        {
            failConnect("No Server Info");
            return;
        }
        // first, prepare the BML Request writing socket
        bmlSendSocketAddress = new InetSocketAddress(info.getServerName(), info.getBmlPort());
        bmlSendSocket = new Socket();
        try
        {
            log.debug("Making socket");
            bmlSendSocket.connect(bmlSendSocketAddress, SOCKET_TIMEOUT);
            log.debug("Making bml writer");
            bmlSendWriter = new PrintWriter(new OutputStreamWriter(bmlSendSocket.getOutputStream(), Charsets.UTF_8),true);
        }
        catch (SocketTimeoutException e)
        {
            retryConnect("Timeout while attempting to connect.");
            return;
        }
        catch (IllegalBlockingModeException e)
        {
            failConnect(e.getMessage());
            return;
        }
        catch (IllegalArgumentException e)
        {
            failConnect(e.getMessage());
            return;
        }
        catch (IOException e)
        {
            failConnect(e.getMessage());
            return;
        }
        mustconnect = false; // success!
        isconnected = true;
        // next, prepare the feedback reading channel.
        log.debug("Preparing feedback channel");
        feedbackReader = new FeedbackReader();
        feedbackReaderThread = new Thread(feedbackReader);
        feedbackReaderThread.start();
        log.debug("Connected to server");
    }

    /** Fail and don't try again */
    private void failConnect(String msg)
    {
        log.warn("Cannot connect to server {}: {}", getServerInfo(), msg);
        mustconnect = false;
        nextMainLoopWait = 1;
    }

    /** Error connecting, prepare to retry */
    private void retryConnect(String msg)
    {
        log.debug("Error connecting to server: {}\n" + "Will try again in {} msec...", msg, CONNECT_RETRY_WAIT);
        nextMainLoopWait = CONNECT_RETRY_WAIT;
    }

    /** Disconnect. Clean up feedbackredirectionloop. Called from the run() loop. */
    private void doshutdown()
    {
        log.debug("Disconnect before shutdown");
        dodisconnect();
        log.debug("Wait till feedbackredirectorthread finished");
        try
        { // wait till feedbackRedirector stopped
            if (feedbackRedirectorThread != null) feedbackRedirectorThread.join();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        log.debug("Shutdown client almost finished");
    }

    /** Send given BML request. If fail: drop request, fire off feedback, disconnect. */
    private void dosendBML(String bmlRequest)
    {
        log.debug("Sending bml {}",bmlRequest);
        // if any next bml request, send it and set sleeptime to 1; upon error, mustdisconnect
        try
        {
            bmlSendWriter.println(bmlRequest);
        }
        catch (Exception e)
        {
            log.warn("Error sending BML; disconnecting from server");
            mustdisconnect = true;
            mustconnect = true; // see if we can restore connection...
        }
        nextMainLoopWait = 1;
    }

    /*
     * ========================================================================================= THE
     * FEEDBACK REDIRECTION PROCESS
     * =========================================================================================
     */

    /**
     * Locking object for the feedbacklisteners -- needed to avoid a situation where feedback is
     * sent out simultaneously with setting the feedbacklisteners.
     */
    private Object feedbackLock = new Object();

    /** redirects feedback that has come in over the socket to the listeners */
    private FeedbackRedirector feedbackRedirector = null;

    /** the thread under which the feedbackRedirector runs. */
    private Thread feedbackRedirectorThread = null;

    /**
     * Incoming feedback from the server are stored here. The feedbackredirector loop will get tehm
     * and send them to the BML FeedbackListeners.
     */
    private ConcurrentLinkedQueue<String> feedbackQ = new ConcurrentLinkedQueue<String>();

    /**
     * The process that reads feedback from the feedback queue, and sends it to the BML feedback
     * listeners.
     */
    private class FeedbackRedirector implements Runnable
    {
        public void run()
        {
            while (!mustshutdown) // this thread should also stop when the client shuts down, and
                                  // not before.
            {
                String nextFeedback = feedbackQ.poll();
                if (nextFeedback != null)
                {
                    synchronized (feedbackLock) // not allowed t change feedback listeners while
                                                // sending feedback
                    {
                        fbManager.sendFeedback(nextFeedback);

                    }
                }
                else
                {
                    try
                    {
                        // nothing to send, let's wait a bit :)
                        Thread.sleep(FEEDBACK_REDIRECT_WAIT_MILLI);
                    }
                    catch (InterruptedException ex)
                    {
                        // no matter -- just continue with next round :) Maybe we were woken up
                        // because new feedback is available?
                    }
                }
            }
        }
    }

    /*
     * =========================================================================================
     * CONSTRUCTION
     * =========================================================================================
     */

    /**
     * Set the state variables to appropriate values, start the main processing loop, and start the
     * processing loop that will deliver feedback messages to the BML Feedback listeners. The last
     * loop, the one reading feedback from the socket, is not started until a connection is made.
     */
    public BMLRealizerToTCPIPAdapter()
    {
        feedbackRedirector = new FeedbackRedirector();
        feedbackRedirectorThread = new Thread(feedbackRedirector);
        feedbackRedirectorThread.start();
        new Thread(this).start();
    }

    

    

}
