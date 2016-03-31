/*******************************************************************************
 *******************************************************************************/
package asap.textengine;

import hmi.environmentbase.Embodiment;

/**
Provides a way to set a text.
*/
public interface TextEmbodiment extends Embodiment
{
  /** Set the text displayed by this embodiment  */
  void setText(String text);
}