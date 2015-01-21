/*******************************************************************************
 *******************************************************************************/
package asap.realizerembodiments;

import hmi.environmentbase.Loader;
import asap.realizer.Engine;

/**

*/
public interface EngineLoader extends Loader
{
  
  /** Return the Engine that was constructed from the XML specification */
  Engine getEngine();
  
}