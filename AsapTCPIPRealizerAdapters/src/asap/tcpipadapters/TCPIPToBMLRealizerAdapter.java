/*******************************************************************************
 *******************************************************************************/
package asap.tcpipadapters;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asap.realizerport.BMLFeedbackListener;
import asap.realizerport.RealizerPort;

import com.google.common.base.Charsets;

/**
 * Takes a {@link asap.realizerport.bml.bridge.RealizerPort RealizerBridge}, and exposes access to it through a
 * tcp/ip connection. The connection is "self-healing".
 * 
 * Detailed documentation can be found in the project report.
 * 
 * @author Dennis Reidsma
 */
@Slf4j
public final class TCPIPToBMLRealizerAdapter implements Runnable, BMLFeedbackListener
{

    private static Logger logger = LoggerFactory.getLogger(TCPIPToBMLRealizerAdapter.class.getName());

    /*
     * ========================================================================================= A
     * FEW GLOBAL STATIC PARAMETERS THAT YOU MIGHT WANT TO PLAY AROUND WITH
     * =========================================================================================
     */

    /** Sleeping time of the BmlRequestRedirector if the bml queue is empty */
    private static final long BML_REDIRECT_WAIT_MILLI = 100;

    private static final int SOCKET_TIMEOUT = 5000; // wait 5 secs for client, then timeout to allow
                                                    // bml reading process to terminate if shutdown

    private static final int CONNECT_RETRY_WAIT = 1000;

    // private static final int WAIT_IF_NO_BML = 100;
    private static final int WAIT_AFTER_DROPPED_CLIENT = 3000;

    /*
     * =========================================================================================
     * EXTERNAL ACCESS: CONTROLLING THE NETWORK CONNECTION
     * =========================================================================================
     */

    /**
     * Instigates total shutdown. May return from this method before shutdown is completed. Shutdown
     * process will terminate all connections and threads for this server.
     */
    public void shutdown()
    {
        synchronized (connectionLock)
        {
            mustshutdown = true;
        }
    }

    /** Returns true iff there is a connection to a client active (and has not been lost). */
    public boolean isConnectedToClient()
    {
        synchronized (connectionLock)
        {
            return isconnected;
        }
    }

    public ServerState getStatus()
    {
        return serverState;
    }

    /**
     * Serverstate
     */
    public enum ServerState
    {
        WAITING, CONNECTING, CONNECTED, DISCONNECTING, NOT_RUNNING
    };

    private ServerState serverState = ServerState.NOT_RUNNING;

    private List<ConnectionStateListener> connectionStateListeners = new ArrayList<ConnectionStateListener>();

    public void addConnectionStateListener(ConnectionStateListener l)
    {
        synchronized (connectionStateListeners)
        {
            connectionStateListeners.add(l);
            l.stateChanged(serverState);
        }
    }

    private void setServerState(ServerState state)
    {
        synchronized (connectionStateListeners)
        {
            if (state == serverState) return;
            if (serverState == ServerState.WAITING && state == ServerState.DISCONNECTING) return; // drop
                                                                                                  // client
                                                                                                  // due
                                                                                                  // to
                                                                                                  // time
                                                                                                  // out,
                                                                                                  // not
                                                                                                  // worth
                                                                                                  // mentioning
            serverState = state;
            for (ConnectionStateListener l : connectionStateListeners)
            {
                l.stateChanged(state);
            }
        }
    }

    /*
     * =========================================================================================
     * FEEDBACKLISTENER API
     * =========================================================================================
     */

    @Override
    public void feedback(String feedback)
    {
        queueFeedback(feedback);        
    }
    
    private void queueFeedback(String fb)
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
            logger.debug("Putting feedback on queue: {}", fb);
            feedbackQ.add(fb);
        }
        else
        {
            // log failure; drop feedback
            logger.debug("Dropped feedback, as no client is connected!");
        }
    }

    /*
     * ========================================================================================= THE
     * MAIN NETWORKING PART: FEEDBACK QUEUE AND STATE VARIABLES
     * =========================================================================================
     */

    /**
     * Incoming Feedback from the RealizerBridge is stored here. The main networking loop will get
     * the feedback and send them to the client if a connection is available.
     */
    private ConcurrentLinkedQueue<String> feedbackQ = new ConcurrentLinkedQueue<String>();

    /** Locking object for the states of the main networking loop. */
    private Object connectionLock = new Object();

    /** Internal state var: true if a conenction to a server is active */
    private boolean isconnected = false;

    /** Internal state var: true if a shutdown request has been given */
    private boolean mustshutdown = false;

    /**
     * Internal state var: true if a connection failed and the feedback connection needs to be
     * cleaned up
     */
    private boolean mustdisconnect = false;

    /**
     * Internal state var: true if someone connected on the bmlrequest port, and a feedback
     * connection needs to be established
     */
    private boolean mustconnect = false;

    /** Waiting time for next run() loop. */
    private long nextMainLoopWait = 100;

    /*
     * ========================================================================================= THE
     * MAIN NETWORKING PART: NETWORKING CONNECTIONS
     * =========================================================================================
     */
    private InetSocketAddress feedbackSendSocketAddress = null;

    private Socket feedbackSendSocket = null;

    private PrintWriter feedbackSendWriter = null;

    private ServerSocket bmlReadServerSocket = null;

    private Socket bmlReadSocket = null;

    private BufferedReader bmlReadReader = null;

    private BMLReader bmlReader = null;

    private Thread bmlReaderThread = null;

    private int requestPort = 7500;

    private int feedbackPort = 7501;

    public int getRequestPort()
    {
        return requestPort;
    }

    public int getFeedbackPort()
    {
        return feedbackPort;
    }

    /**
     * Stop bml reading sockets preparatory to completely shutting down, or preparatory to
     * connecting new client
     */
    public void stopBmlReadSockets()
    {
        try
        {
            logger.debug("Stop BML Read sockets: Close bml read socket");
            if (bmlReadSocket != null) bmlReadSocket.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        try
        {
            logger.debug("Stop BML Read sockets: Close server socket");
            if (bmlReadServerSocket != null) bmlReadServerSocket.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    /** Stop feedback sending socket when to dropping client */
    public void stopFeedbackWriteSockets()
    {
        try
        {
            if (feedbackSendSocket != null) feedbackSendSocket.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    /*
     * ========================================================================================= THE
     * MAIN NETWORKING PART: THE BML READING PROCESS
     * =========================================================================================
     */

    /**
     * The process that reads bml from the network connection, and puts it in the bml queue. If
     * connecting this reader leads to serious exceptions, a disconnect is requested. The reader
     * continues listening for new connections until the server is shut down.
     */
    private class BMLReader implements Runnable
    {
        public BMLReader()
        {
        }

        private boolean stop = false;

        boolean bmlconnected = false;

        public void stopReading()
        {
            stop = true;
        }

        public void run()
        {
            logger.debug("Starting BML Reader");
            while (!stop)
            { // keep reading for new clients until stop (shutdown?)
                while (!bmlconnected && !stop)
                {
                    logger.debug("Opening server socket");
                    // start listening
                    try
                    {
                        bmlReadServerSocket = new ServerSocket(requestPort);
                    }
                    catch (IOException e)
                    {
                        failBmlConnect(e.getMessage());
                        return;
                    }
                    logger.debug("Server socket opened");
                    try
                    {
                        bmlReadServerSocket.setSoTimeout(0);// don't time out
                    }
                    catch (SocketException e)
                    {
                        failBmlConnect(e.getMessage());
                        return;
                    }

                    try
                    {
                        bmlReadServerSocket.setSoTimeout(SOCKET_TIMEOUT);
                    }
                    catch (SocketException e)
                    {
                        failBmlConnect(e.getMessage());
                        return;
                    }

                    try
                    {
                        setServerState(ServerState.WAITING);
                        logger.debug("Waiting for client to connect");
                        bmlReadSocket = bmlReadServerSocket.accept();
                        bmlconnected = true;
                        setServerState(ServerState.CONNECTING);
                        logger.debug("Incoming client; preparing reader");
                        bmlReadReader = new BufferedReader(new InputStreamReader(bmlReadSocket.getInputStream(),Charsets.UTF_8));
                        logger.debug("Client connected, starting lo listen for BML at port " + requestPort);
                    }
                    catch (SocketTimeoutException e)
                    {
                        dropClient("Timeout while accepting incoming client. going back to listen.");
                        continue;
                    }
                    catch (IllegalBlockingModeException e)
                    {
                        failBmlConnect(e.getMessage());
                        return;
                    }
                    catch (IllegalArgumentException e)
                    {
                        failBmlConnect(e.getMessage());
                        return;
                    }
                    catch (IOException e)
                    {
                        dropClient(e.getMessage());
                        continue;
                    }
                    // bml channel is connected. Keep reading from it, and processing what comes in.
                    // also, try to open feedback sending channel!
                    mustconnect = true;
                    XMLTokenizer tok = new XMLTokenizer(bmlReadReader);

                    while (bmlconnected && !stop)
                    {
                        logger.debug("Connected -- keep trying to read");
                        try
                        {
                            /*
                             * if (tok.atEndOfDocument()) { if (bmlReadSocket.isClosed()) {
                             * logger.debug("Bml reading socket died"); mustdisconnect=true;
                             * stop=true; continue; }
                             * logger.debug("Waiting for new BML to come in over network..."); try {
                             * Thread.sleep(WAIT_IF_NO_BML); } catch(InterruptedException ex) { }
                             * continue; }
                             */
                            if (!tok.atSTag("bml"))
                            {
                                dropClient("Client sent wrong format data over bml channel, dropping client. BML tag: ");
                            }
                            else
                            {
                                String bmlRequest = tok.getXMLSection();
                                log.debug("adding bml "+bmlRequest);                                
                                bmlQ.add(bmlRequest);
                                logger.debug("Gotten BML request, putting it on queue");
                                try
                                { // speed up handling of the BML a bit by waking up redirector if
                                  // necessary
                                    bmlRedirectorThread.interrupt();
                                }
                                catch (Exception e)
                                {
                                    logger.warn("Exception in BMLReader", e);
                                }
                            }
                        }
                        catch (IOException e)
                        {
                            dropClient("Error while reading bml from client. " + e.getMessage());
                        }
                    } // while connected
                } // while ! connected
            } // while ! stop
            logger.debug("Closing sockets and readers in BML reader");
            stopBmlReadSockets();
            logger.debug("Leaving BML Reader");
        }

        private void failBmlConnect(String msg)
        {
            logger.warn("Failed to start listening to clients: {}.  Shutting server down.", msg);
            mustshutdown = true;
            nextMainLoopWait = 1;
        }

        private void dropClient(String msg)
        {
            logger.info("Dropping client: {}.", msg);
            stopBmlReadSockets();
            bmlconnected = false;
            mustdisconnect = true;
            // note, sometimes the socket is slow in being released. After a drop client, it's best
            // to not accept connections for a bit.
            try
            {
                Thread.sleep(WAIT_AFTER_DROPPED_CLIENT);
            }
            catch (InterruptedException ex)
            {
            }
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
        setServerState(ServerState.WAITING);
        while (true)
        {
            synchronized (connectionLock)
            {
                // logger.debug("Next state run of server");
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
                    String nextFeedback = feedbackQ.poll();
                    if (nextFeedback != null)
                    {
                        dosendFeedback(nextFeedback);
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
        logger.debug("Server shutdown finished");
    }

    /** Disconnect feedback connection to client. Called from the run() loop. */
    private void dodisconnect()
    {
        setServerState(ServerState.DISCONNECTING);
        // bmlReader.dropClient("Cleaning up client connection"); no! dropclient is done elsewhere
        // beore setting mjustdisconnect to true
        // close the sockets etc
        logger.debug("Closing feedback sender");
        stopFeedbackWriteSockets();
        logger.debug("Feedback sender closed");
        mustdisconnect = false;
        isconnected = false;
        feedbackQ.clear();
        nextMainLoopWait = 1;
        if (!mustshutdown) setServerState(ServerState.WAITING);
    }

    /**
     * Attempt to connect to client feedback channel. If successful, isconnected is true; else, try
     * again next round with wait = 1000. Called from the run() loop.
     */
    private void doconnect()
    {
        setServerState(ServerState.CONNECTING);
        logger.debug("Connecting feedback sender");
        // first, prepare the feedback writing socket. Note that the bml socket has been
        // established, otherwise we would not be connecting here!
        feedbackSendSocketAddress = new InetSocketAddress(((InetSocketAddress) bmlReadSocket.getRemoteSocketAddress()).getAddress(),
                feedbackPort);
        feedbackSendSocket = new Socket();
        try
        {
            feedbackSendSocket.connect(feedbackSendSocketAddress, SOCKET_TIMEOUT);            
            feedbackSendWriter = new PrintWriter(new OutputStreamWriter(feedbackSendSocket.getOutputStream(),Charsets.UTF_8), true);
        }
        catch (SocketTimeoutException e)
        {
            retryConnect("Timeout while attempting to connect.");
            return;
        }
        catch (IllegalBlockingModeException e)
        {
            bmlReader.dropClient("IllegalBlockingModeException; "+e.getMessage()+"\n"+ Arrays.toString(e.getStackTrace()));
            return;
        }
        catch (IllegalArgumentException e)
        {
            bmlReader.dropClient("IllegalArgumentException: "+e.getMessage()+"\n"+ Arrays.toString(e.getStackTrace()));
            return;
        }
        catch (IOException e)
        {
            bmlReader.dropClient("IOException: "+e.getMessage()+"\n"+ Arrays.toString(e.getStackTrace()));
            return;
        }
        logger.debug("Feedback sender connected");
        mustconnect = false; // success!
        isconnected = true;
        setServerState(ServerState.CONNECTED);
    }

    /** Error connecting, prepare to retry */
    private void retryConnect(String msg)
    {
        logger.debug("Error connecting to client feedback channel: {}\n" + "Will try again in msec...", msg, CONNECT_RETRY_WAIT);
        nextMainLoopWait = CONNECT_RETRY_WAIT;
    }

    /** Disconnect. Clean up bmlredirectionloop. Called from the run() loop. */
    private void doshutdown()
    {
        setServerState(ServerState.DISCONNECTING);
        logger.debug("Enter shutdown...");
        bmlReader.stopReading();
        stopBmlReadSockets();// deze blokkeert, want De XML toknizer heeft de reader op slot gezet
                             // en dan gaat de close() staan wachten. Dus ik ben bang dat ik hier de
                             // bml reader met meer geweld om zeep moet helpen. Thread kill?
        dodisconnect();
        logger.debug("Stopping bmlReaderThread...");
        try
        { // wait till bmlRedirector stopped
            bmlReaderThread.join();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        logger.debug("Stopping bmlRedirectorThread...");
        try
        { // wait till bmlRedirector stopped
            bmlRedirectorThread.join();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        logger.debug("Shutdown almost done.");
        setServerState(ServerState.NOT_RUNNING);
    }

    /** Send given feedbacki. If fail: drop request, drop client. */
    private void dosendFeedback(String feedback)
    {
        // if any next feedback, send it and set sleeptime to 1; upon error, dodisconnect
        try
        {
            logger.debug("Sending feedback :{}",feedback);
            feedbackSendWriter.println(feedback);
        }
        catch (Exception e)
        {
            logger.warn("Error sending feedback; dropping client");
            mustdisconnect = true;
            bmlReader.dropClient("feedbackchannel broken");
        }
        nextMainLoopWait = 1;
    }

    /*
     * ========================================================================================= THE
     * BML REDIRECTION PROCESS
     * =========================================================================================
     */

    /** redirects bml that has come in over the socket to the realizerbridge */
    private BMLRedirector bmlRedirector = null;

    /** the thread under which the bmlRedirector runs. */
    private Thread bmlRedirectorThread = null;

    /** The realizerbridge that will handle incoming bml */
    private RealizerPort realizerBridge = null;

    /**
     * Incoming bml from the client are stored here. The bmlredirector loop will get them and send
     * them to the BML realizer.
     */
    private ConcurrentLinkedQueue<String> bmlQ = new ConcurrentLinkedQueue<String>();

    /** The process that reads bml from the bml queue, and sends it to the BML realizer. */
    private class BMLRedirector implements Runnable
    {
        public void run()
        {
            while (!mustshutdown) // this thread should also stop when the server shuts down, and
                                  // not before.
            {
                // logger.debug("Is there new BML in the queue?");
                String nextBml = bmlQ.poll();
                log.debug("realizing bml {}", nextBml);
                if (nextBml != null)
                {
                    try
                    {
                        logger.debug("New BML in queue, send to realizer.");
                        realizerBridge.performBML(nextBml);
                    }
                    catch (Exception ex)
                    { // failing realizer means the application is down. shutdown server.
                        logger.warn("Error sending BML to realizer -- shutting down server! {}", ex);
                        mustshutdown = true;
                        nextMainLoopWait = 1;
                    }
                }
                else
                {
                    try
                    {
                        // nothing to send, let's wait a bit :)
                        Thread.sleep(BML_REDIRECT_WAIT_MILLI);
                    }
                    catch (InterruptedException ex)
                    {
                        Thread.interrupted();
                        // no matter -- just continue with next round :) Maybe we were woken up
                        // because new bml is available?
                    }
                }
            }
            logger.debug("Shutdown BML Redirection queue");
        }

    }

    /*
     * =========================================================================================
     * CONSTRUCTION
     * =========================================================================================
     */

    /**
     * Set the state variables to appropriate values, start the main processing loop, and start the
     * processing loop that will deliver bml messages to the Realizer. Also, start the loop that
     * waits for new clients to connect.
     */
    public TCPIPToBMLRealizerAdapter(RealizerPort bridge, int requestPort, int feedbackPort)
    {
        if (bridge == null)
        {
            throw new IllegalArgumentException("Server cannot have null realizer");
        }
        realizerBridge = bridge;
        realizerBridge.addListeners(this);
        this.requestPort = requestPort;
        this.feedbackPort = feedbackPort;

        bmlRedirector = new BMLRedirector();
        bmlRedirectorThread = new Thread(bmlRedirector);
        bmlRedirectorThread.start();
        // start main loop
        new Thread(this).start();
        // start waiting-for-client-Thread
        bmlReader = new BMLReader();
        bmlReaderThread = new Thread(bmlReader);
        bmlReaderThread.start();
    }

    

}
