package asap.hns;

import java.io.IOException;

import hmi.util.Resources;

import org.junit.Test;

/**
 * Hns integration test: checks if a large example hns file can be parsed
 * @author hvanwelbergen
 *
 */
public class HnsIntegrationTest
{
    @Test
    public void test() throws IOException
    {
        Hns hns = new Hns();
        hns.readXML(new Resources("").getReader("billie_hns.xml"));
    }
}
