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
package asap.srnao.bml;

import hmi.xml.XMLFormatting;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;

/**
 * Sets a specific joint to an angle with a certain speed
 * @author davisond
 */
public class SetJointAngleBehavior extends NaoBehaviour
{
    private String jointName;
    private float angle;
    private float speed;

    @Override
    public boolean satisfiesConstraint(String name, String value)
    {
        if (name.equals("jointName")) return true;
        if (name.equals("angle")) return true;
        if (name.equals("speed")) return true;
        return super.satisfiesConstraint(name, value);
    }

    public SetJointAngleBehavior(String bmlId, XMLTokenizer tokenizer) throws IOException
    {
        super(bmlId);
        readXML(tokenizer);
    }

    @Override
    public StringBuilder appendAttributeString(StringBuilder buf, XMLFormatting fmt)
    {
        appendAttribute(buf, "jointName", jointName.toString());
        appendAttribute(buf, "angle", angle);
        appendAttribute(buf, "speed", speed);
        return super.appendAttributeString(buf, fmt);
    }

    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        jointName = getRequiredAttribute("jointName", attrMap, tokenizer);
        angle = getRequiredFloatAttribute("angle", attrMap, tokenizer);
        speed = getRequiredFloatAttribute("speed", attrMap, tokenizer);
        super.decodeAttributes(attrMap, tokenizer);
    }

    /*
     * The XML Stag for XML encoding
     */
    private static final String XMLTAG = "setJointAngle";

    /**
     * The XML Stag for XML encoding -- use this static method when you want to see if a given
     * String equals the xml tag for this class
     */
    public static String xmlTag()
    {
        return XMLTAG;
    }

    /**
     * The XML Stag for XML encoding -- use this method to find out the run-time xml tag of an
     * object
     */
    @Override
    public String getXMLTag()
    {
        return XMLTAG;
    }

    @Override
    public String getStringParameterValue(String name)
    {
        if (name.equals("jointName"))
        {
            return jointName.toString();
        }
        return super.getStringParameterValue(name);
    }

    @Override
    public float getFloatParameterValue(String name)
    {
    	if (name.equals("angle"))
        {
            return angle;
        }
    	if (name.equals("speed"))
        {
            return speed;
        }
        return super.getFloatParameterValue(name);
    }

    @Override
    public boolean specifiesParameter(String name)
    {
        if (name.equals("jointName") || name.equals("angle") || name.equals("speed"))
        {
            return true;
        }
        return super.specifiesParameter(name);
    }
}
