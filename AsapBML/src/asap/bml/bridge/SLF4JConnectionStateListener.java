package asap.bml.bridge;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asap.bml.bridge.TCPIPToBMLRealizerAdapter.ServerState;


/** 
 * Listens to the connection state of a server of the TCPIPRealizerBridgeAPI 
 * @author reidsma
 **/
public class SLF4JConnectionStateListener implements ConnectionStateListener
{
      private static Logger logger = LoggerFactory
            .getLogger(BMLRealizerToTCPIPAdapter.class.getName());
            
  public void stateChanged(ServerState state)
  {
    logger.info("State change: "+state);
  }
}