package asap.animationengine.gesturebinding;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import hmi.util.Resources;

import java.io.IOException;

import org.junit.Test;

import asap.hns.Hns;

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
        Hns hns = new Hns();
        hns.readXML(new Resources("").getReader("hns/hns.xml"));
        HnsHandshape hs = new HnsHandshape(hns,"hs1","hs2");
        assertNotNull(hs.getHNSHandShape("rest"));
        assertNotNull(hs.getHNSHandShape("ASLb"));
        assertNull(hs.getHNSHandShape("unknown"));
    }
}
