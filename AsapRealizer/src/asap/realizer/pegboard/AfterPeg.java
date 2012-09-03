package asap.realizer.pegboard;

import javax.annotation.concurrent.GuardedBy;

import net.jcip.annotations.ThreadSafe;

/**
 * AfterPegs define TimePegs that remains at a minimum time offset from a 'regular' TimePeg.<br>
 * that is: afterPeg.getGlobalTime() >= linkedPeg.getGlobalTime()+offset
 * 
 * if the link's value is TimePeg.VALUE_UNKNOWN, it is ignored.
 * 
 * @author welberge
 */
@ThreadSafe
public final class AfterPeg extends TimePeg
{
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
        if (getLocalValue() == TimePeg.VALUE_UNKNOWN) return link.getLocalValue() + offset;
        if (link.getLocalValue() == TimePeg.VALUE_UNKNOWN) return getLocalValue();
        if (getLocalValue() >= link.getLocalValue() + offset) return getLocalValue();
        return link.getLocalValue() + offset;
    }

    @Override
    public synchronized double getGlobalValue()
    {
        if (getGlobalValue() == TimePeg.VALUE_UNKNOWN) return link.getGlobalValue() + offset;
        if (link.getGlobalValue() == TimePeg.VALUE_UNKNOWN) return getGlobalValue();
        if (getGlobalValue() >= link.getGlobalValue() + offset) return getGlobalValue();
        return link.getGlobalValue() + offset;
    }

    @Override
    public synchronized void setLocalValue(double v)
    {
        super.setLocalValue(v);
        if (v != TimePeg.VALUE_UNKNOWN)
        {
            if (link.getLocalValue() == TimePeg.VALUE_UNKNOWN)
            {
                link.setLocalValue(v + offset);
            }
            else if (v + offset < link.getLocalValue())
            {
                link.setLocalValue(v + offset);
            }
        }
    }
    
    @Override
    public synchronized void setGlobalValue(double v)
    {
        super.setGlobalValue(v);
        if (v != TimePeg.VALUE_UNKNOWN)
        {
            if (link.getGlobalValue() == TimePeg.VALUE_UNKNOWN)
            {
                link.setGlobalValue(v + offset);
            }
            else if (v + offset < link.getGlobalValue())
            {
                link.setGlobalValue(v + offset);
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
