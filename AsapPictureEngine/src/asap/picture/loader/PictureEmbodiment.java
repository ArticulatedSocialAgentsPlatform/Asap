/*******************************************************************************
 *******************************************************************************/
package asap.picture.loader;

import hmi.environmentbase.Embodiment;
import asap.picture.display.PictureDisplay;

/** This embodiment offers access to a PictureDisplay, which in turn allows one to load and display layered images on a canvas. */
public interface PictureEmbodiment extends Embodiment
{
    PictureDisplay getPictureDisplay();
}
