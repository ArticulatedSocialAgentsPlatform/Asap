/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.procanimation;

import hmi.animation.VJoint;
import hmi.math.Quat4f;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.util.Vector;

import org.lsmp.djep.xjep.XJep;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;

/**
 * Procedural description of a joint rotation at a key time
 * 
 * @author welberge
 */
public class Keyframe extends XMLStructureAdapter
{
    private XJep parser;

    private float time = 0;

    private boolean local = true;

    private String rotFormula[];

    private Node node[] = new Node[4];

    private float q[] = new float[4];

    private float euler[] = new float[3];

    private float qw[] = new float[4];

    private float qp[] = new float[4];

    private float aa[] = new float[4];

    private VJoint joint;

    private String encoding;

    /**
     * Constructor
     * 
     * @param p
     *            parser
     */
    public Keyframe(XJep p, String enc)
    {
        parser = p;
        rotFormula = new String[4];
        q = new float[4];
        euler = new float[3];
        qw = new float[4];
        qp = new float[4];
        encoding = enc;
    }

    /**
     * Copy-constructor. Copies joint rotation and local/global settings. Links parser and joint.
     * 
     * @param kf
     *            keyframe to copy
     */
    public Keyframe(Keyframe kf)
    {
        local = kf.local;
        joint = kf.joint;
        parser = kf.parser;
        time = kf.time;
        encoding = kf.encoding;
        rotFormula = new String[4];
        q = new float[4];
        euler = new float[3];
        qw = new float[4];
        qp = new float[4];

        int rots = 4;
        if (encoding.equals("euler")) rots = 3;
        for (int i = 0; i < rots; i++)
        {
            try
            {
                setRotation(i, kf.rotFormula[i]);
            }
            catch (ParseException e)
            {
                throw new IllegalArgumentException(e);
            }
        }
    }

    /**
     * Sets the joint to rotate
     * 
     * @param j
     *            joint
     */
    public void setJoint(VJoint j)
    {
        joint = j;
    }

    public void mirror()
    {
        if (encoding.equals("quaternion"))
        {
            rotFormula[2] = "-(" + rotFormula[2] + ")";
            rotFormula[3] = "-(" + rotFormula[3] + ")";
        }
        else if (encoding.equals("axisangles"))
        {
            rotFormula[1] = "-(" + rotFormula[1] + ")";
            rotFormula[2] = "-(" + rotFormula[2] + ")";
        }
        else if (encoding.equals("euler"))
        {
            rotFormula[1] = "-(" + rotFormula[1] + ")";
            rotFormula[2] = "-(" + rotFormula[2] + ")";
        }
    }

    @Override
    public String toString()
    {
        String str = "" + time + "  " + rotFormula[0] + ";" + rotFormula[1] + ";" + rotFormula[2];
        if (encoding.equals("quaternion") || encoding.equals("axisangles"))
        {
            str = str + ";" + rotFormula[3];
        }
        return str;
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
     * Calculates the joint rotation. When a rotation is specified as world rotation, the local
     * rotation of joint to satisfy this world rotation (in the coordinate system of the human) is
     * calculated and set as goal.
     * 
     * @param goal
     *            : output rotation (quaternion)
     * @param human
     *            : humanoid joint
     * @return false on error
     */
    public boolean evaluate(float[] goal, VJoint human)
    {
        if (encoding.equals("euler"))
        {
            for (int i = 0; i < 3; i++)
            {
                if (node[i] == null)
                {
                    return false;
                }
                try
                {
                    euler[i] = ((Double) parser.evaluate(node[i])).floatValue();
                }
                catch (ParseException e)
                {
                    return false;
                }
            }
            Quat4f.setFromRollPitchYaw(q, euler[2], euler[0], euler[1]);
        }
        else if (encoding.equals("quaternion"))
        {
            for (int i = 0; i < 4; i++)
            {
                if (node[i] == null)
                {
                    return false;
                }
                try
                {
                    q[i] = ((Double) parser.evaluate(node[i])).floatValue();
                }
                catch (ParseException e)
                {
                    return false;
                }
            }
        }
        else if (encoding.equals("axisangles"))
        {
            for (int i = 0; i < 4; i++)
            {
                if (node[i] == null)
                {
                    return false;
                }
                try
                {
                    aa[i] = ((Double) parser.evaluate(node[i])).floatValue();
                }
                catch (ParseException e)
                {
                    return false;
                }
            }
            Quat4f.setFromAxisAngle4f(q, aa);
        }

        if (!local)
        {
            Quat4f.set(qw, q);
            VJoint parent = joint.getParent();
            parent.getPathRotation(human, qp);
            Quat4f.inverse(qp);
            Quat4f.mul(q, qp, qw);
        }
        Quat4f.set(goal, q);
        return true;
    }

    /*
     * =========================================== IMPLEMENTATION OF XMLSTRUCTURE INTERFACES
     * ===========================================
     */
    @Override
    public StringBuilder appendAttributeString(StringBuilder buf)
    {
        appendAttribute(buf, "time", time);
        appendAttribute(buf, "local", local);
        if (encoding.equals("euler"))
        {
            appendAttribute(buf, "value", rotFormula[0] + ";" + rotFormula[1] + ";" + rotFormula[2]);
        }
        else
        {
            appendAttribute(buf, "value", rotFormula[0] + ";" + rotFormula[1] + ";" + rotFormula[2]
                    + ";" + rotFormula[3]);
        }
        return buf;
    }

    @Override
    public boolean decodeAttribute(String attrName, String attrValue)
    {
        if (attrName.equals("time"))
        {
            time = Float.parseFloat(attrValue);
        }
        else if (attrName.equals("value"))
        {
            String str = attrValue;
            String rot[] = str.split(";");
            try
            {
                setRotation(0, rot[0]);
                setRotation(1, rot[1]);
                setRotation(2, rot[2]);
                if (rot.length > 3)
                {
                    setRotation(3, rot[3]);
                }
            }
            catch (ParseException ex)
            {
                throw new IllegalArgumentException(ex);
            }
        }
        else if (attrName.equals("local"))
        {
            local = Boolean.parseBoolean(attrValue);
        }
        return false;
    }

    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws java.io.IOException
    {

    }

    @Override
    public boolean decodeAttribute(String attrName, String attrValue, XMLTokenizer tokenizer)
    {
        return decodeAttribute(attrName, attrValue);
    }

    /**
     * Get the XML tag
     * 
     * @return the xml tag
     */
    @Override
    public String getXMLTag()
    {
        return "Keyframe";
    }

    /**
     * @return the time
     */
    public float getTime()
    {
        return time;
    }
}
