/*******************************************************************************
 *******************************************************************************/
package asap.faceengine.faceunit;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import hmi.faceanimation.FaceController;
import hmi.faceanimation.FaceInterpolator;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the KeyframeMorphFU
 * @author herwinvw
 *
 */
public class KeyframeMorphFUTest
{
    private FaceController mockFaceController = mock(FaceController.class);
    
    private KeyframeMorphFU fu;

    @Before
    public void setup()
    {
        FaceInterpolator mi = new FaceInterpolator();
        mi.readXML("<FaceInterpolator parts=\"morph1 morph2\">1 0.1 0.2\n 2 0.3 0.4</FaceInterpolator>");
        fu = new KeyframeMorphFU(mi);
        fu = fu.copy(mockFaceController, null, null);
    }
    
    @Test
    public void testPlay()
    {
        fu.play(0.5);
        verify(mockFaceController).addMorphTargets(any(String[].class), eq(new float[] { 0.2f, 0.3f}));
    }
}
