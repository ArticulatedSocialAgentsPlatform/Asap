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

import java.util.HashMap;

import hmi.xml.*;


/**
 * Describes a float parameter that can be used in a motion unit and procedural
 * animation Also includes XML-parser information
 * 
 * @author Mark ter Maat
 * @author Herwin van Welbergen June 22, 2007
 */
public class Parameter extends XMLStructureAdapter
{
    /* Global variables */

    private String description;
    private String sid;
    private double value = 0;

    public Parameter deepCopy()
    {
        return new Parameter(sid, description, value);
    }

    /* Constructors */

    /**
     * Constructor for new empty Parameter.
     */
    public Parameter()
    {
        description = "";
        sid = "";
    }

    /**
     * Constructor for new Parameter
     * 
     * @param n
     *            sid
     * @param d
     *            description
     */
    public Parameter(String n, String d)
    {
        description = d;
        sid = n;
    }

    /**
     * Constructor for new Parameter with predefined values
     */
    public Parameter(String n, double v)
    {
        description = "";
        sid = n;
        value = v;
    }

    /**
     * Constructor for new Parameter with predefined values, description
     */
    public Parameter(String n, String des, double v)
    {
        description = des;
        sid = n;
        value = v;
    }

    /**
     * returns the description of the parameter
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * sets the description of the parameter
     * 
     * @param d
     *            the new description
     */
    public void setDescription(String d)
    {
        description = d;
    }

    /**
     * returns the sid of the parameter
     */
    public String getSid()
    {
        return sid;
    }

    /**
     * sets the sid of the parameter
     * 
     * @param n
     *            the new sid
     */
    public void setSid(String n)
    {
        sid = n;
    }

    /**
     * returns the value of the parameter
     */
    public double getValue()
    {
        return value;
    }

    /**
     * sets the description of the parameter
     * 
     * @param v
     *            the new value - this is a string but will be parsed to a
     *            double
     */
    public void setValue(String v)
    {
        try
        {
            value = (Double.parseDouble(v));
        } catch (NumberFormatException exc)
        {
            throw new IllegalArgumentException(exc);
        }
    }

    /**
     * sets the description of the parameter
     * 
     * @param v
     *            the new value
     */
    public void setValue(double v)
    {
        value = v;
    }
 
    @Override
    public int hashCode()
    {
        return sid.hashCode();        
    }
    
    @Override
    public boolean equals(Object p)
    {
        if (p instanceof Parameter)
        {
            return (((Parameter) p).sid.equals(sid));
        }
        return false;
    }

    // =========== XML parser methods =================

    @Override
    public void decodeAttributes(HashMap<String, String> attrMap,
            XMLTokenizer tokenizer)
    {
        value = getRequiredFloatAttribute("value", attrMap, tokenizer);
        description = getOptionalAttribute("description", attrMap, "");
        sid = getRequiredAttribute("sid", attrMap, tokenizer);
    }

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
        appendAttribute(buf, "sid", "" + sid);
        appendAttribute(buf, "description", "" + description);
        appendAttribute(buf, "value", "" + value);
        return buf;
    }

    @Override
    public String getXMLTag()
    {
        return "Parameter";
    }
}
