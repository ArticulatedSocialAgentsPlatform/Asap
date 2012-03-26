/*******************************************************************************
 * Copyright (C) 2009 Human Media Interaction, University of Twente, the Netherlands
 * 
 * This file is part of the Elckerlyc BML realizer.
 * 
 * Elckerlyc is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Elckerlyc is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Elckerlyc.  If not, see http://www.gnu.org/licenses/.
 ******************************************************************************/
package asap.animationengine.procanimation;

import hmi.animation.VJoint;
import hmi.math.Quat4f;
import hmi.xml.XMLFormatting;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import org.lsmp.djep.xjep.XJep;

/**
 * Procedurally describes joint rotation as key rotations at key times, inbetween rotations are
 * calculated through slerp interpolation.
 * 
 * @author welberge
 */
public class Keyframes extends XMLStructureAdapter
{
    private String target = "";

    private String encoding = "";

    private List<Keyframe> frames = new ArrayList<Keyframe>();

    private VJoint joint;

    // private double startT = 0;

    // private double endT = 0;

    private XJep parser;

    /**
     * Sets the target humanoid for this rotation, tries to find the joint to rotate
     * 
     * @param h
     *            the target
     */
    public void setTarget(VJoint h)
    {
        joint = h.getPart(target);
        for (Keyframe f : frames)
        {
            f.setJoint(joint);
        }

    }

    /**
     * Mirrors all joint rotations on the XY plane, switches left/right partIds
     */
    public void mirror()
    {
        for (Keyframe f : frames)
        {
            f.mirror();
        }
        if (target.startsWith("l_"))
        {
            target = target.replace("l_", "r_");
        }
        else if (target.startsWith("r_"))
        {
            target = target.replace("r_", "l_");
        }
    }

    /**
     * Removes all keyframes
     */
    public void clear()
    {
        frames.clear();

    }

    /**
     * Sets the new joint
     * 
     * @param j
     */
    public void setJoint(VJoint j)
    {
        joint = j;
        for (Keyframe f : frames)
        {
            f.setJoint(joint);
            target = joint.getSid();
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
     * Constructor, no target set
     * 
     * @param p
     *            parser
     */
    public Keyframes(XJep p)
    {
        parser = p;
        target = null;
    }

    /**
     * Constructor
     * 
     * @param p
     *            parser
     * @param t
     *            target
     */
    public Keyframes(XJep p, String t)
    {
        parser = p;
        target = t;
    }

    /**
     * Copies the KeyFrame-s of another Keyframes object into this object. Also links kfs's joint
     * and target to this keyframes.
     * 
     * @param kfs
     */
    public void set(Keyframes kfs)
    {
        joint = kfs.joint;
        target = kfs.target;
        frames.clear();
        for (Keyframe kf : kfs.frames)
        {
            Keyframe k = new Keyframe(kf);
            frames.add(k);
        }

    }

    /**
     * Creates a deep copy linked to the same parser and joint
     * 
     * @return the copy
     */
    public Keyframes deepCopy()
    {
        Keyframes copy = new Keyframes(parser, "" + target);
        copy.set(this);
        return copy;
    }

    /**
     * returns list with all parameters used in all formulas
     */
    @SuppressWarnings({ "rawtypes" })
    public Vector findParameters(Vector v)
    {
        for (Keyframe f : frames)
        {
            f.findParameters(v);
        }
        return v;
    }

    /**
     * Calculates joint rotation at time t
     * 
     * @param t
     *            : time, 0 &lt t &lt 1
     * @param goal
     *            : output rotation
     * @return false on error TODO: throw exception instead?
     */
    public boolean evaluate(double t, float[] goal, VJoint human)
    {
        Keyframe start = null;
        Keyframe end = null;
        float startT, endT;
        float[] qNext = new float[4];
        float[] qPrev = new float[4];

        for (Keyframe f : frames)
        {
            if (f.getTime() <= t)
            {
                start = f;
            }
            else
            {
                end = f;
                break;
            }
        }
        // System.out.println("Start: "+start.toString()+" end: "+end.toString());
        if (end == null && start == null)
        {
            return false;
        }
        else if (start == null)
        {
            end.evaluate(goal, human);
            return true;
        }
        else if (end == null)
        {
            start.evaluate(goal, human);
            return true;
        }
        else
        {
            endT = end.getTime();
            end.evaluate(qNext, human);
            start.evaluate(qPrev, human);
            startT = start.getTime();
        }
        Quat4f.interpolate(goal, qPrev, qNext, (float) ((t - startT) / (endT - startT)));
        return true;
    }

    /**
     * @return the target
     */
    public String getTarget()
    {
        return target;
    }

    /*
     * =========================================== IMPLEMENTATION OF XMLSTRUCTURE INTERFACES
     * ===========================================
     */
    @Override
    public StringBuilder appendContent(StringBuilder buf, XMLFormatting fmt)
    {
        for (Keyframe kf : frames)
        {
            kf.appendXML(buf, fmt);
        }
        return buf;
    }

    @Override
    public StringBuilder appendAttributeString(StringBuilder buf)
    {
        appendAttribute(buf, "target", target);
        appendAttribute(buf, "encoding", encoding);
        return buf;
    }

    @Override
    public boolean decodeAttribute(String attrName, String attrValue)
    {
        if (attrName.equals("target"))
        {
            target = attrValue;
        }
        else if (attrName.equals("encoding"))
        {
            encoding = attrValue;
        }
        return false;
    }

    class KeyframeTimeComparator implements Comparator<Keyframe>
    {
        @Override
        public int compare(Keyframe kf1, Keyframe kf2)
        {
            if (kf1.getTime() < kf2.getTime())
            {
                return -1;
            }
            if (kf1.getTime() > kf2.getTime())
            {
                return 1;
            }
            return 0;
        }

    }

    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws java.io.IOException
    {
        while (!tokenizer.atETag())
        {
            if (tokenizer.atSTag("Keyframe"))
            {
                Keyframe kf = new Keyframe(parser, encoding);
                kf.readXML(tokenizer);
                frames.add(kf);
            }
        }
        Collections.sort(frames, new KeyframeTimeComparator());
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
        return "Keyframes";
    }

    public String getEncoding()
    {
        return encoding;
    }

}
