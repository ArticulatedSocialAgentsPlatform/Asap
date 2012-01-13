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

import org.lsmp.djep.xjep.*;
import org.nfunk.jep.*;

import hmi.xml.*;
import java.util.*;

/**
 * Contains the end effector and end effector path (rotation and/or translation)
 * of an IK motion The path can be a function of time t (0 &lt; t &lt; 1) and
 * several other variables.
 * 
 * @author welberge
 * 
 *         Updated, June 22, 2007 Support for multiple parameters added.
 * @author Mark ter Maat /
 */
public class EndEffector extends XMLStructureAdapter
{
    private Node nodeTrans[] = new Node[3];
    private Node nodeSwivel;

    private String transFormula[] = new String[3];
    private String swivelFormula = "";
    private String target = "";
    private boolean local = false;
    private XJep parser;

    /**
     * Constructor, no target set
     * 
     * @param p
     *            parsers
     */
    public EndEffector(XJep p)
    {
        parser = p;
        target = null;
    }

    /**
     * Mirrors the movement trajectory on the XY plane, switches left/right targets     
     */
    public void mirror()
    {
        setSwivel("-("+swivelFormula+")");
        setTranslation(0, "-("+transFormula[0]+")");
        if(target.startsWith("l_"))
        {
            target=target.replace("l_", "r_");
        }
        else if(target.startsWith("r_"))
        {
            target=target.replace("r_", "l_");
        }            
    }
    /**
     * Constructor
     * 
     * @param parser
     *            parser
     * @param target
     *            endeffector target name
     */
    public EndEffector(XJep parser, String target)
    {
        this.parser = parser;
        this.target = target;
    }

    /**
     * Copy the values of eff into this endeffector, links the target joint.
     * 
     * @param eff
     *            endeffector to copy
     */
    public void set(EndEffector eff)
    {
        for (int i = 0; i < 3; i++)
        {
            setTranslation(i, eff.transFormula[i]);
        }
        setSwivel(eff.swivelFormula);
        target = eff.target;
        local = eff.local;
    }

    /**
     * Get the target
     * 
     * @return the target
     */
    public String getTarget()
    {
        return target;
    }

    /**
     * Get the translation formula
     * 
     * @param i
     *            index (0=x, 1=y, 2=z)
     * @return the translation formula
     */
    public String getTranslationFormula(int i)
    {
        return transFormula[i];
    }

    /**
     * Set the translation path
     * 
     * @param i
     *            the translation axis (0=x,1=y,2=z)
     * @param translation
     *            the translation path a formula of 0<t<1 and a
     */
    public void setTranslation(int i, String translation)
    {
        transFormula[i] = translation;
        try
        {
            nodeTrans[i] = parser.simplify(parser.preprocess(parser
                    .parse(translation)));
        } catch (ParseException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Get the swivel formula
     * 
     * @return the swivel formula
     */
    public String getSwivel()
    {
        return swivelFormula;
    }

    /**
     * Set the swivel formula
     * 
     * @param s
     *            the swivel formula
     * @throws ParseException
     */
    public void setSwivel(String s)
    {
        if (s == null || s.equals(""))
        {
            nodeSwivel = null;
            return;
        }
        swivelFormula = s;
        try
        {
            nodeSwivel = parser.simplify(parser.preprocess(parser.parse(s)));
        } catch (ParseException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Evaluates the swivel angle for time 0<t<1
     * 
     * @return the swivel angle, 0 if invalid formula
     */
    public double evaluateSwivel()
    {
        if (nodeSwivel != null)
        {
            try
            {
                return (Double) parser.evaluate(nodeSwivel);
            } catch (ParseException e)
            {
                throw new IllegalArgumentException(e);
            }
        }
        return 0;
    }

    /**
     * Evaluates the translation position for the current parameter settings at
     * time t, 0<=t<=1
     */
    public void evaluateTrans(float dst[], double t)
    {
        Variable tVar = parser.getVar("t");
        tVar.setValue(t);
        for (int i = 0; i < 3; i++)
        {
            if (nodeTrans[i] != null)
            {
                try
                {
                    dst[i] = ((Double) parser.evaluate(nodeTrans[i]))
                            .floatValue();
                } catch (ParseException e)
                {
                    throw new IllegalArgumentException(e);
                }
            } else
            {
                dst[i] = 0;
            }
        }
    }

    /**
     * Evaluates the translation position for the current parameter settings and
     * time
     */
    public void evaluateTrans(float dst[])
    {
        for (int i = 0; i < 3; i++)
        {
            if (nodeTrans[i] != null)
            {
                try
                {
                    dst[i] = ((Double) parser.evaluate(nodeTrans[i]))
                            .floatValue();
                } catch (ParseException e)
                {
                    throw new IllegalArgumentException(e);
                }
            } else
            {
                dst[i] = 0;
            }
        }
    }

    /**
     * Evaluates for a single index
     * 
     * @param target
     *            x=0,y=1,z=2
     */
    public double evaluateSingleIndex(int target)
    {
        if (nodeTrans[target] != null)
        {
            try
            {
                return ((Double) parser.evaluate(nodeTrans[target]));
            } catch (ParseException e)
            {
                throw new IllegalArgumentException(e);
            }
        }
        return 0;
    }

    /**
     * Checks if local coordinates are used for this endeffector
     * 
     * @return true if local coordinates are used
     */
    public boolean isLocal()
    {
        return local;
    }

    /**
     * Set if local coordinates should be used
     * 
     * @param local
     *            new local value
     */
    public void setLocal(boolean local)
    {
        this.local = local;
    }

    public EndEffector deepCopy()
    {
        EndEffector copy = new EndEffector(parser, "" + target);
        copy.set(this);
        return copy;
    }

    /**
     * returns list with all parameters used in all formulas
     */
    @SuppressWarnings("rawtypes")
    public Vector findParameters(Vector v)
    {
        for (int i = 0; i < 3; i++)
        {
            if (nodeTrans[i] != null)
            {
                v = parser.getVarsInEquation(nodeTrans[i], v);
            }
        }
        if (nodeSwivel != null)
        {
            v = parser.getVarsInEquation(nodeSwivel, v);
        }
        return v;
    }

    // ---------------------------------------------------------------------------------------------------------
    // XML parsing part
    // ---------------------------------------------------------------------------------------------------------
    /**
     * decodes the value from an attribute value String returns true if
     * succesful, returns false for attribute names that are not recognized.
     * Might throw a RuntimeException when an attribute has been recognized, but
     * is ill formatted.
     */
    @Override
    public boolean decodeAttribute(String attrName, String attrValue)
    {
        if (attrName.equals("target"))
        {
            target = attrValue;
        } else if (attrName.equals("local"))
        {
            local = Boolean.parseBoolean(attrValue);
        } else if (attrName.equals("translation"))
        {
            String str = attrValue;
            transFormula = str.split(";");
            setTranslation(0, transFormula[0]);
            setTranslation(1, transFormula[1]);
            setTranslation(2, transFormula[2]);
        } else if (attrName.equals("swivel"))
        {
            String str = attrValue;
            setSwivel(str);
        } else
        {
            return false;
        }
        return true;
    }

    /**
     * decodes the value from an attribute value String returns true if
     * succesful, returns false for attribute names that are not recognized.
     * Might throw a RuntimeException when an attribute has been recognized, but
     * is ill formatted. Moreover, an XMLTokenizer reference is available which
     * can be queried for attributes, like getTokenLine() or getTokenCharPos(),
     * which might be helpful to produce error messages referring to
     * lines/positions within the XML document
     */
    @Override
    public boolean decodeAttribute(String attrName, String attrValue,
            XMLTokenizer tokenizer)
    {
        return decodeAttribute(attrName, attrValue);
    }

    /**
     * decodes the XML contents, i.e. the XML between the STag and ETag of the
     * encoding.
     */
    @Override
    public void decodeContent(XMLTokenizer tokenizer)
            throws java.io.IOException
    {
    }

    /**
     * Appends a String to buf that encodes the contents for the XML encoding.
     * The encoding should start on a new line, using indentation equal to tab.
     * There should be no newline after the encoding.
     */
    @Override
    public StringBuilder appendContent(StringBuilder buf, XMLFormatting fmt)
    {
        return buf;
    }

    /**
     * Appends a String to buf that encodes the attributes for the XML encoding.
     * When non empty, the attribute string should start with a space character.
     * Hint: call the appendAttribute(StringBuffer buf, String attrName, String
     * attrValue) for every relevant attribute; this takes care of the leading
     * space as well as spaces in between the attributes) The encoding should
     * preferably not add newline characters.
     */
    @Override
    public StringBuilder appendAttributeString(StringBuilder buf)
    {
        appendAttribute(buf, "local", local);
        if (transFormula[0] != null)
        {
            String str = transFormula[0] + ";" + transFormula[1] + ";"
                    + transFormula[2];
            appendAttribute(buf, "translation", str);
        }
        if (swivelFormula != null)
        {
            appendAttribute(buf, "swivel", swivelFormula);
        }
        appendAttribute(buf, "target", target);
        return buf;
    }

    @Override
    public String getXMLTag()
    {
        return "EndEffector";
    }

}
