/*******************************************************************************
 *******************************************************************************/
package asap.picture.display;

public interface PictureDisplay
{

    /**
     * Pre-load an image from the given resource and cache it for future access
     */
    void preloadImage(String imageId, String resourcePath, String fileName);

    /**
     * Removes all current images and displays only the image img, at layer 'z'
     * @param id the id of the planunit to which this image belongs
     * @param img the image to be displayed
     */
    void setImage(String planUnitId, String imageId, float z);

    /**
     * Removes the image at a given layer 'z'
     * @param id the id of the planunit to which this image belongs
     * @param z the layer of which to remove the image
     */
    void removeImage(String planUnitId, float z);

    /**
     * Adds an image img onto layer z. THe previous image on that layer is 'remembered'; i.e.
     * when this image is removed, the previous one will appear again! This allows for 'base pose images'.
     * @param id the id of the planunit to which this image belongs
     * @param img the image to be added
     * @param layer the layer on which to add the image
     */
    void addImage(String planUnitId, String imageId, float layer);

    /**
     * Replaces the image of planunit id on layer z with a new image
     * @param id the id of the planunit
     * @param img the image file
     * @param layer the layer
     */
    void replaceImage(String planUnitId, String imageId, float layer);
}
