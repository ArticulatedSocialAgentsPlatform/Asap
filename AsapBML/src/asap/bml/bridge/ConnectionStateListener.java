package asap.bml.bridge;

import asap.bml.bridge.TCPIPToBMLRealizerAdapter.ServerState;

/** Listens to the connection state of a server of the TCPIPRealizerBridgeAPI 
 * @author reidsma
 **/
public interface ConnectionStateListener
{
    void stateChanged(ServerState state);
}