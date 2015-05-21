/*******************************************************************************
 *******************************************************************************/
package asap.srnao.loader;

import hmi.environmentbase.Embodiment;
import asap.srnao.display.PictureDisplay;

/** This embodiment offers access to a PictureDisplay, which in turn allows one to load and display layered images on a canvas. */
public interface StompROSNaoEmbodiment extends Embodiment
{
    PictureDisplay getPictureDisplay();
}
