/*******************************************************************************
 *******************************************************************************/
package asap.realizer.pegboard;

import javax.annotation.concurrent.GuardedBy;

import net.jcip.annotations.ThreadSafe;

/**
 * AfterPegs define TimePegs that remains at a minimum time offset from a 'regular' TimePeg.<br>
 * that is: afterPeg.getGlobalTime() >= linkedPeg.getGlobalTime()+offset
 * 
 * if the link's value is TimePeg.VALUE_UNKNOWN, it is ignored.
 * 
 * 
 * @author welberge
 */
@ThreadSafe
public final class AfterPeg extends TimePeg
{
    // XXX Should an AfterPeg be (or OffsetPeg) able to move a link that has an absolute time? Currently it does...

    @GuardedBy("this")
    private TimePeg link;

    @GuardedBy("this")
    private double offset = 0;

    public AfterPeg(double o, BMLBlockPeg bmp)
    {
        super(bmp);
        offset = o;
    }

    public synchronized void setOffset(double o)
    {
        offset = o;
    }

    public AfterPeg(TimePeg l, double o)
    {
        super(l.bmlBlockPeg);
        link = l;
        offset = o;
    }

    public AfterPeg(TimePeg l, double o, BMLBlockPeg bmp)
    {
        super(bmp);
        link = l;
        offset = o;
    }

    public synchronized void setLink(TimePeg p)
    {
        link = p;
    }

    @Override
    public synchronized TimePeg getLink()
    {
        return link;
    }

    @Override
    public synchronized double getLocalValue()
    {
        if (super.getLocalValue() == TimePeg.VALUE_UNKNOWN) return TimePeg.VALUE_UNKNOWN;
        if (link.getLocalValue() == TimePeg.VALUE_UNKNOWN) return super.getLocalValue();
        if (super.getLocalValue() >= link.getValue(bmlBlockPeg) + offset) return super.getLocalValue();
        return link.getLocalValue() + offset;
    }

    @Override
    public synchronized void setLocalValue(double v)
    {
        super.setLocalValue(v);
        if (v != TimePeg.VALUE_UNKNOWN)
        {
            if (link.getLocalValue() != TimePeg.VALUE_UNKNOWN)
            {
                if (v < link.getValue(bmlBlockPeg) + offset)
                {
                    link.setValue(v - offset,bmlBlockPeg);
                }
            }
        }
    }

    @Override
    public synchronized String toString()
    {
        return "AfterTimePeg with value " + getLocalValue() + " ,offset " + offset + " global value: " + getGlobalValue() + "link: "
                + link.toString();
    }
}
