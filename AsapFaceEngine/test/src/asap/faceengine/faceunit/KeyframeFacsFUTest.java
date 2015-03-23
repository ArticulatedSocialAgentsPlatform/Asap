/*******************************************************************************
 *******************************************************************************/
package asap.faceengine.faceunit;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import hmi.faceanimation.FaceController;
import hmi.faceanimation.FaceInterpolator;
import hmi.faceanimation.converters.FACSConverter;
import hmi.faceanimation.model.MPEG4Configuration;

import org.junit.Before;
import org.junit.Test;

import asap.motionunit.MUPlayException;

/**
 * Unit tests for the KeyframeFacsFU
 * @author herwinvw
 *
 */
public class KeyframeFacsFUTest
{
private FaceController mockFaceController = mock(FaceController.class);
    
    private KeyframeFacsFU fu;
    private FACSConverter fconv = new FACSConverter();
    
    @Before
    public void setup()
    {
        FaceInterpolator mi = new FaceInterpolator();
        mi.readXML("<FaceInterpolator parts=\"1.BOTH 2.LEFT\">1 0.1 0.2\n 2 0.3 0.4</FaceInterpolator>");
        fu = new KeyframeFacsFU(mi);
        fu = fu.copy(mockFaceController, fconv, null);
    }
    
    @Test
    public void testPlay() throws MUPlayException
    {
        fu.play(0.5);
        verify(mockFaceController).addMPEG4Configuration(any(MPEG4Configuration.class));
    }
}
