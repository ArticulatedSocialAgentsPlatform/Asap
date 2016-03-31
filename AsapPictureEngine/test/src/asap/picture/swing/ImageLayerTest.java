/*******************************************************************************
 *******************************************************************************/
package asap.picture.swing;

import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Unit tests for the ImageLayer
 * @author hvanwelbergen
 *
 */
public class ImageLayerTest
{
    @Test
    public void testEmpty()
    {
        ImageLayer layer = new ImageLayer();
        assertNull(layer.getActiveImage());
    }
}
