/*******************************************************************************
 *******************************************************************************/
package asap.realizer.pegboard;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * Peg that refers to the absolute time of the start of BMLBlock with id id.
 * @author welberge
 */
@ThreadSafe
public final class BMLBlockPeg
{
    public static final String GLOBAL_PEG_ID = "global";
    public static final String ANTICIPATOR_PEG_ID = "anticipators";
    public static final BMLBlockPeg GLOBALPEG = new BMLBlockPeg(GLOBAL_PEG_ID, 0);  
    
    private final String id;
    
    @GuardedBy("this")
    private double value;
    
    /**
     * @return the id
     */
    public String getId()
    {
        return id;
    }

    public BMLBlockPeg(String id, double v)
    {
        this.id = id;
        value = v;
    }
    
    /**
     * get the value of the SynchronisationPoint
     * @return the value of the SynchronisationPoint
     */
    public synchronized double getValue()
    {
        return value;
    }
    
    /**
     * Set the value of the SynchronisationPoint.
     * @param value the new value
     */
    public synchronized void setValue(double value)
    {
        this.value = value;
    }
    
    @Override
    public synchronized String toString()
    {
        return "Time peg with value "+getValue();
    }
}
