package asap.bmlflowvisualizer.gui;

import hmi.testutil.demotester.DefaultFestDemoTester;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Test;

import asap.bmlflowvisualizer.BMLFlowVisualizerPort;
import asap.bmlflowvisualizer.utils.BMLBlock;
import asap.realizerport.BMLFeedbackListener;
import asap.realizerport.RealizerPort;

/**
 * UI tests
 * @author hvanwelbergen
 *
 */
public class BMLFlowVisualizationTest extends DefaultFestDemoTester
{
    private static final int DELAY = 4000;
    private JFrame testFrame = createJFrame();
    private FrameFixture window = createFrameFixture(testFrame);

    @Test
    public void test() throws IOException, InterruptedException, InvocationTargetException
    {
        final BMLFlowVisualizerPort visPort = new BMLFlowVisualizerPort(new RealizerPort()
        {

            @Override
            public void addListeners(BMLFeedbackListener... listeners)
            {

            }

            @Override
            public void removeListener(BMLFeedbackListener l)
            {

            }

            @Override
            public void removeAllListeners()
            {

            }

            @Override
            public void performBML(String bmlString)
            {

            }
        } );
        
        SwingUtilities.invokeAndWait(new Runnable()
        {

            @Override
            public void run()
            {
                testFrame.add(new BMLFlowVisualization(visPort, new HashMap<String, BMLBlock>()));
                testFrame.setSize(1000,1000);
                testFrame.setVisible(true);
            }
        });
        
        
        Thread.sleep(DELAY);
        window.close();
    }

    @After
    public void cleanup()
    {
        window.cleanUp();
    }
}
