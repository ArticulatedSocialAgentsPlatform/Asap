/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.procanimation;

import hmi.animation.VJoint;
import hmi.xml.XMLFormatting;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.util.Vector;

import org.lsmp.djep.xjep.XJep;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;

/**
 * Describes the rotation of a single joint in a formula The rotation can be a
 * function of time t (0 &lt t &lt 1) and some parameters
 * 
 * @author welberge
 * @author Mark ter Maat
 */
public class Rotation extends XMLStructureAdapter
{
    private Node node[] = new Node[3];
    private String rotFormula[] = new String[3];
    private String target = "";
    private VJoint joint;
    private XJep parser;
    private boolean local = true;

    public void mirror()
    {
        if (target.startsWith("l_"))
        {
            target = target.replace("l_", "r_");
        }
        else if (target.startsWith("r_"))
        {
            target = target.replace("r_", "l_");
        }
        try
        {
            setRotation(1, "-(" + rotFormula[1] + ")");
        }
        catch (ParseException e)
        {
            throw new IllegalArgumentException(e);
        }
        try
        {
            setRotation(2, "-(" + rotFormula[2] + ")");
        }
        catch (ParseException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Creates a new Rotation with null target
     * 
     * @param p
     *            the XJep rotation formula parser
     */
    public Rotation(XJep p)
    {
        parser = p;
        target = null;
    }

    /**
     * Constructor
     * 
     * @param p
     *            the XJep rotation formula parser
     * @param t
     *            the target joint sid
     */
    public Rotation(XJep p, String t)
    {
        parser = p;
        target = t;
    }

    /**
     * Copy the rotation formulas and target from eff into this Rotation
     * 
     * @param eff
     *            the rotation to copy
     */
    public void set(Rotation eff)
    {
        for (int i = 0; i < 3; i++)
        {
            try
            {
                setRotation(i, eff.rotFormula[i]);
            }
            catch (ParseException e)
            {
                throw new IllegalArgumentException(e);
            }
        }
        this.target = eff.target;
    }

    /**
     * Get the target joint sid
     * 
     * @return the target joint sid
     */
    public String getTarget()
    {
        return target;
    }

    /**
     * Sets the target joint sid
     * 
     * @param t
     *            the sid of the target joint
     */
    public void setTarget(String t)
    {
        target = t;
    }

    /**
     * Get the rotation formula
     * 
     * @param i
     *            index (0=x axis,1=y axis,2=z axis)
     * @return the rotation formula
     */
    public String getRotationFormula(int i)
    {
        return rotFormula[i];
    }

    /**
     * Set the rotation path
     * 
     * @param i
     *            the rotation axis (0=x,1=y,2=z)
     * @param rotation
     *            the rotation path a formula of 0 &lt t &lt 1 and amplitude a
     */
    public void setRotation(int i, String rotation) throws ParseException
    {
        rotFormula[i] = rotation;
        node[i] = parser.preprocess(parser.parse(rotation));
    }

    /**
     * @param goal
     *            : output rotation
     * @return false on error
     */
    public boolean evaluate(float[] goal)
    {
        for (int i = 0; i < 3; i++)
        {
            if (node[i] == null)
            {
                return false;
            }
            try
            {
                goal[i] = ((Double) parser.evaluate(node[i])).floatValue();
            }
            catch (ParseException e)
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Evaluates the rotation for time 0 &lt t &lt 1
     * 
     * @return the rotation
     */
    public float evaluateSingleIndex(int i)
    {
        if (node[i] != null)
        {
            try
            {
                return ((Double) parser.evaluate(node[i])).floatValue();
            }
            catch (ParseException e)
            {
                throw new IllegalArgumentException(e);
            }
        }
        return 0;
    }

    /**
     * returns list with all parameters used in all formulas
     */
    @SuppressWarnings("rawtypes")
    public Vector findParameters(Vector v)
    {
        for (int i = 0; i < 3; i++)
        {
            if (node[i] != null)
            {
                v = parser.getVarsInEquation(node[i], v);
            }
        }
        return v;
    }

    /**
     * Sets the target humanoid for this rotation, tries to find the joint to
     * rotate
     * 
     * @param h
     *            the target
     */
    public void setTarget(VJoint h)
    {
        joint = h.getPart(target);
    }

    /**
     * Sets the new joint, sets the target to the joints sid, if j!=null
     * 
     * @param j
     *            new joint
     */
    public void setJoint(VJoint j)
    {
        joint = j;
        if (j != null)
        {
            target = j.getSid();
        }
    }

    /**
     * Gets the joint the rotation works on
     * 
     * @return the joint, null if none set
     */
    public VJoint getJoint()
    {
        return joint;
    }

    /**
     * @return A clone of the Rotation. The clone is still linked to the same
     *         joint
     */
    public Rotation deepCopy() throws ParseException
    {
        Rotation copy = new Rotation(parser, "" + target);

        copy.setRotation(0, "" + getRotationFormula(0));
        copy.setRotation(1, "" + getRotationFormula(1));
        copy.setRotation(2, "" + getRotationFormula(2));

        copy.setJoint(joint); // WARNING: No deepcopy used
        copy.target = target;
        return copy;
    }

    /**
     * @return the local
     */
    public boolean isLocal()
    {
        return local;
    }

    // ---------- XML parse part ----------------------------

    @Override
    public boolean decodeAttribute(String attrName, String attrValue)
    {
        if (attrName.equals("target"))
        {
            target = attrValue;
        }
        else if (attrName.equals("local"))
        {
            local = Boolean.parseBoolean(attrValue);
        }
        else if (attrName.equals("rotation"))
        {
            String str = attrValue;
            rotFormula = str.split(";");
            try
            {
                setRotation(0, rotFormula[0]);
                setRotation(1, rotFormula[1]);
                setRotation(2, rotFormula[2]);
            }
            catch (ParseException ex)
            {
                throw new IllegalArgumentException(ex);
            }
        }
        return false;
    }

    @Override
    public boolean decodeAttribute(String attrName, String attrValue, XMLTokenizer tokenizer)
    {
        return decodeAttribute(attrName, attrValue);
    }

    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws java.io.IOException
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
        if (rotFormula[0] != null)
        {
            String str = rotFormula[0] + ";" + rotFormula[1] + ";" + rotFormula[2];
            appendAttribute(buf, "rotation", str);
        }
        appendAttribute(buf, "target", target);
        return buf;
    }

    @Override
    public String getXMLTag()
    {
        return "Rotation";
    }

}
