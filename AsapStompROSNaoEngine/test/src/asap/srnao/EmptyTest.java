package asap.srnao;

import hmi.testutil.demotester.DefaultFestDemoTester;

import java.io.IOException;

import javax.swing.JFrame;

import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Test;

/**
 *
 */
public class EmptyTest extends DefaultFestDemoTester
{
    private static final int DELAY = 4000;
    private JFrame testFrame = createJFrame();
    private FrameFixture window = createFrameFixture(testFrame);
    
    @Test
    public void test() throws IOException, InterruptedException
    {
        Thread.sleep(DELAY);
        window.close();
    }
    
    @After
    public void cleanup()
    {
        window.cleanUp();
    }
}
