package asap.realizer.bridge;

import asap.bml.bridge.LinkedBlockingQueuePipe;
import asap.realizer.ElckerlycRealizer;
import saiba.bml.bridge.RealizerPort;
import saiba.bml.feedback.BMLListener;


/**
 * Facade that hooks up a LinkedBlockingQueueBridge to a ElckerlycRealizerBridge.
 * Just convenience class for this often used combination of bridges.
 * @author welberge
 *
 */
public class MultiThreadedElckerlycRealizerBridge implements RealizerPort
{
    private final LinkedBlockingQueuePipe queueBridge;
    private final ElckerlycRealizerPipe elrBridge;
    public MultiThreadedElckerlycRealizerBridge(ElckerlycRealizer realizer)
    {
        elrBridge = new ElckerlycRealizerPipe(realizer);
        queueBridge = new LinkedBlockingQueuePipe(elrBridge);
    }
    
    
    @Override
    public void performBML(String bmlString)
    {
        queueBridge.performBML(bmlString);        
    }


    @Override
    public void addListeners(BMLListener ... listeners)
    {
        queueBridge.addListeners(listeners);            
    }

    public void stopRunning()
    {
        queueBridge.stopRunning();
    }

    @Override
    public void removeAllListeners()
    {
        queueBridge.removeAllListeners();        
    }
}
