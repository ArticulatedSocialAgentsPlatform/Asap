/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.gesturebinding;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.junit.Test;

/**
 * Integration tests for HnsHandshape
 * @author hvanwelbergen
 *
 */
public class HnsHandshapeIntegrationTest
{
    @Test
    public void test() throws IOException
    {
        HnsHandshape hs = new HnsHandshape("hs1","hs2");
        assertNotNull(hs.getHNSHandShape("rest"));
        assertNotNull(hs.getHNSHandShape("ASLb"));
        assertNull(hs.getHNSHandShape("unknown"));
    }
}
