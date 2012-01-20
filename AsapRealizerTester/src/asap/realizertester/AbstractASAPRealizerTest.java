package asap.realizertester;


import hmi.realizertester.AbstractElckerlycRealizerTest;

import java.io.IOException;
import org.junit.Test;

public abstract class AbstractASAPRealizerTest extends AbstractElckerlycRealizerTest
{
    @Test
    public void testChunk() throws InterruptedException, IOException
    {
        String bmlString1 = readTestFile("asap/chunking/firstchunk.xml");
        String bmlString2 = readTestFile("asap/chunking/secondchunk.xml");
        
        realizerPort.performBML(bmlString1);
        realizerPort.performBML(bmlString2);
        
        waitForBMLEndFeedback("bml1");
        waitForBMLEndFeedback("bml2");
        
        
    }
}
