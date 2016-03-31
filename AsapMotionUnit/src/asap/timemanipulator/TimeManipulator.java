/*******************************************************************************
 *******************************************************************************/
package asap.timemanipulator;

/**
 * Manipulates animation time, modifying the velocity of an IKMove
 * 
 * @author welberge
 */
public interface TimeManipulator 
{
    /**
     * Get the manipulated value of 0 &lt; t &lt; 1 Implementations should adhere
     * to the following rules: manip(0)=0 manip(1)=1 for every 0 &lt; t1 &lt; 1, 0
     * &lt; t2 &lt; 1: if t1 &lt; t2 then manip(t1)&lt;manip(t2)
     * 
     * @param t
     *            the time to be manipulated
     * @return the manipulated time
     */
    double manip(double t);
}
