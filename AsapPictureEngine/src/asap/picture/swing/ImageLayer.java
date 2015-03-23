/*******************************************************************************
 *******************************************************************************/
package asap.picture.swing;

import java.awt.Image;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * This class implements a single layer which can contain multiple images.
 * The class will ensure that only one image (the most recently added) will be active at a time
 * @author Daniel
 * 
 */
public class ImageLayer
{

    private Map<String, Image> imageStore;
    private Vector<String> imageIndex;

    public ImageLayer()
    {
        imageIndex = new Vector<String>();
        imageStore = new HashMap<String, Image>();
    }

    /**
     * Adds an image to this layer, making it active
     * @param id a unique id by which this image is represented
     * @param img the image file
     */
    public int addImage(String id, Image img)
    {
        imageIndex.add(id);
        imageStore.put(id, img);
        return imageIndex.size();
    }

    /**
     * Removes an image from this layer, even if it is not active at the moment
     * @param id the id of the image to be removed
     */
    public int removeImage(String id)
    {
        imageIndex.removeElement(id);
        imageStore.remove(id);
        return imageIndex.size();
    }

    /**
     * Replaces an image in this layer with a new image.
     * If the id is not found in this layer, nothing is changed
     * @param id The id of the image
     * @param img The actual image file
     */
    public void replaceImage(String id, Image img)
    {
        if (imageIndex.contains(id))
        {
            imageStore.put(id, img);
        }
    }

    /**
     * Returns the active image of this layer, or null if there is no active image at this moment
     * @return the active image, or null if this layer is empty
     */
    public Image getActiveImage()
    {
        if(imageIndex.size()==0) return null;
        return imageStore.get(imageIndex.lastElement());
    }
}
