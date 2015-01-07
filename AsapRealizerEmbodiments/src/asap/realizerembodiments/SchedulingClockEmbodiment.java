/*******************************************************************************
 *******************************************************************************/
package asap.realizerembodiments;

import hmi.environmentbase.Embodiment;
import hmi.util.Clock;

/**
 * provides access to the scheduling clock of a VH
 */
public interface SchedulingClockEmbodiment extends Embodiment
{
  Clock getSchedulingClock();
  
}