package asap.opensmile;

import java.io.IOException;

import org.junit.Test;

/**
 * Unit tests for the OpenSmileWrapper
 * @author hvanwelbergen
 *
 */
public class OpenSmileWrapperTest
{
    @Test
    public void test() throws IOException
    {
        OpenSmileWrapper.analyzeProsody("test/resource/dentist_hannah.wav");
    }
}
