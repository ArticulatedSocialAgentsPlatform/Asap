/*******************************************************************************
 *******************************************************************************/
package asap.realizer.planunit;

import hmi.xml.XMLFormatting;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.util.HashMap;

import saiba.bml.BMLGestureSync;

import com.google.common.base.Objects;
/**
 * KeyPosition, used to store time and weight of motion unit keys. The time of
 * the keypositions indicates where it is located within a motion unit. 0 &lt;
 * time &lt;1 0 &lt; weight &lt;1 KeyPosition can be compared on time and weight.
 * Time is more important. If time is equal, the smallest KeyPosition is the one
 * with the smallest weight.
 * 
 * @author welberge
 */
public class KeyPosition extends XMLStructureAdapter implements
        Comparable<KeyPosition>
{
    public double time; // /0 &lt time &lt1
    public double weight; // /0 &lt weight &lt1
    public String id = "";

    public KeyPosition()
    {

    }

    public KeyPosition(String id, double t)
    {
        this(id,t,1);
    }
    
    public KeyPosition(String id, double t, double w)
    {
        this.id = id;
        time = t;
        weight = w;
    }

    /**
     * Get String representation of the key
     * 
     * @return the String representation
     */
    @Override
    public String toString()
    {
        return id + " " + time + " " + weight;
    }

    private int bmlIdCompareTo(String otherId)
    {
        BMLGestureSync bs = BMLGestureSync.get(id);
        BMLGestureSync bsOther = BMLGestureSync.get(otherId);
        if(bs!=null && bsOther!=null)
        {
            if(bs.ordinal()<bsOther.ordinal())
            {
                return -1;
            }
            else if(bs.ordinal()>bsOther.ordinal())
            {
                return 1;
            }
        }
        return 0;
    }
    
    /**
     * Comparable interface Compares this object with the specified object for
     * order. Returns a negative integer, zero, or a positive integer as this
     * object is less than, equal to, or greater than the specified object.
     * KeyPosition are compared on time, id and weight. Time is more important.
     * If time is equal, the smallest KeyPosition is the one with the smallest
     * weight.
     */
    @Override
    public int compareTo(KeyPosition o)
    {
        if (time < o.time)
        {
            return -1;
        }
        if (time > o.time)
        {
            return 1;
        }
        int idcompare = bmlIdCompareTo(o.id);
        if (idcompare!=0)return idcompare;
            
        if (weight < o.weight)
        {
            return -1;
        }
        if (weight > o.weight)
        {
            return 1;
        }
        return 0;
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(weight,time);        
    }
    
    @Override
    public boolean equals(Object o)
    {
        if(o instanceof KeyPosition)
        {
            KeyPosition pos = (KeyPosition)o;
            return pos.weight == weight && pos.time == time && pos.id.equals(id);
        }
        return false;
    }
    /**
     * Keypositions are equal if they have equal ids
     */
    public boolean isEqual(KeyPosition o)
    {
        return id.equals(o.id);
    }

    /**
     * Creates a deep copy of this instance
     * 
     * @return a copy of this instance
     */
    public KeyPosition deepCopy()
    {
        KeyPosition copy = new KeyPosition();
        copy.time = time;
        copy.weight = weight;
        copy.id = id;
        return copy;
    }

    // XML parser part
    @Override
    public boolean decodeAttribute(String attrName, String attrValue,
            XMLTokenizer tokenizer)
    {
        return decodeAttribute(attrName, attrValue);
    }

    @Override
    public void decodeContent(XMLTokenizer tokenizer)
            throws java.io.IOException
    {

    }

    @Override
    public StringBuilder appendContent(StringBuilder buf, XMLFormatting fmt)
    {
        return buf;
    }

    @Override
    public StringBuilder appendAttributeString(StringBuilder buf)
    {
        appendAttribute(buf, "time", time);
        appendAttribute(buf, "weight", weight);
        appendAttribute(buf, "id", id);
        return buf;
    }

    @Override
    public void decodeAttributes(HashMap<String, String> attrMap,
            XMLTokenizer tokenizer)
    {
        time = getRequiredDoubleAttribute("time", attrMap, tokenizer);
        weight = getOptionalDoubleAttribute("weight", attrMap, 1);
        id = getRequiredAttribute("id", attrMap, tokenizer);
    }

    @Override
    public String getXMLTag()
    {
        return "KeyPosition";
    }
}
