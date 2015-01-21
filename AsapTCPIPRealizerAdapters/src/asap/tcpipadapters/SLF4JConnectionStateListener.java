/*******************************************************************************
 *******************************************************************************/
package asap.tcpipadapters;

import lombok.extern.slf4j.Slf4j;
import asap.tcpipadapters.TCPIPToBMLRealizerAdapter.ServerState;

/**
 * Listens to the connection state of a server of the TCPIPRealizerBridgeAPI
 * @author reidsma
 **/
@Slf4j
public class SLF4JConnectionStateListener implements ConnectionStateListener
{
    public void stateChanged(ServerState state)
    {
        log.info("State change: " + state);
    }
}
