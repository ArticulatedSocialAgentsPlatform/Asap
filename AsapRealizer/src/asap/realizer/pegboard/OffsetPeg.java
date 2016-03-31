/*******************************************************************************
 *******************************************************************************/
package asap.realizer.pegboard;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * OffsetPegs define TimePegs that remains at a fixed time offset from a 'regular' TimePeg. 
 * 
 * An OffsetPeg can refer to another OffsetPeg as it's 'link'.
 *  
 * @author welberge
 */
@ThreadSafe
public final class OffsetPeg extends TimePeg
{
    @GuardedBy("this")
    private TimePeg link;
    
    @GuardedBy("this")
    private double offset = 0;
    
    public synchronized double getOffset()
    {
        return offset;
    }
    
    public OffsetPeg(TimePeg l, double o)
    {
        super(l.bmlBlockPeg);
        link = l;
        offset = o;
        setAbsoluteTime(l.isAbsoluteTime());        
    }
    
    public OffsetPeg(TimePeg l, double o,BMLBlockPeg bmp)
    {
        super(bmp);
        link = l;
        offset = o;
        setAbsoluteTime(l.isAbsoluteTime());        
    }
    
    public synchronized void setOffset(double o)
    {
        offset = o;        
    }
    
    public synchronized void setLink(TimePeg p)
    {
        link = p;
        setAbsoluteTime(link.isAbsoluteTime());
    }
    
    @Override
    public synchronized TimePeg getLink()
    {
        return link;
    }
    
    /**
     * get the value of the SynchronisationPoint
     * @return the value of the SynchronisationPoint
     */
    @Override
    public synchronized double getLocalValue()
    {
        if(link.getLocalValue()==TimePeg.VALUE_UNKNOWN)return TimePeg.VALUE_UNKNOWN;
        return link.getValue(bmlBlockPeg)+offset;
    }
    
    
    @Override
    public synchronized void setLocalValue(double v)
    {
        if(v == TimePeg.VALUE_UNKNOWN)
        {
            link.setLocalValue(TimePeg.VALUE_UNKNOWN);
        }
        else
        {
            link.setValue(v-offset, bmlBlockPeg);
        }        
    }
    
    @Override
    public synchronized String toString()
    {
        return "OffsetTime peg with value "+getLocalValue()+" ,offset "+offset + " global value: "+ getGlobalValue();
    }
}
