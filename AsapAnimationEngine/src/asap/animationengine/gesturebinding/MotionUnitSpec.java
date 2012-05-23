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
package asap.animationengine.gesturebinding;

import java.io.*;
import java.util.*;

import lombok.Getter;

import saiba.bml.core.Behaviour;

import hmi.util.*;
import hmi.xml.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asap.animationengine.motionunit.AnimationUnit;

/**
 * XML parser for the MotionUnitSpec in a gesturebinding
 * @author Herwin
 * 
 */
class MotionUnitSpec extends XMLStructureAdapter
{
    private final static Logger logger = LoggerFactory.getLogger(MotionUnitSpec.class.getName());

    public AnimationUnit motionUnit;
    private String type;
    private String specnamespace;

    @Getter
    private SpecConstraints constraints = new SpecConstraints();
    private SpecParameterMap parameterMap = new SpecParameterMap();
    private SpecParameterDefaults parameterdefaults = new SpecParameterDefaults();

    private final Resources resources;

    public boolean satisfiesConstraints(Behaviour b)
    {
        return constraints.satisfiesConstraints(b);
    }

    /**
     * @return the specnamespace
     */
    public String getSpecnamespace()
    {
        return specnamespace;
    }

    /**
     * @return the type
     */
    public String getType()
    {
        return type;
    }

    public Set<String> getParameters()
    {
        return parameterMap.getParameters();
    }

    public MotionUnitSpec(Resources r)
    {
        resources = r;
    }

    /**
     * Get motion unit parameter for BML parameter src
     */
    public String getParameter(String src)
    {
        return parameterMap.getParameter(src);
    }

    public Collection<SpecParameterDefault> getParameterDefaults()
    {
        return parameterdefaults.getParameterDefaults();
    }

    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        type = getRequiredAttribute("type", attrMap, tokenizer);
        specnamespace = getOptionalAttribute("namespace", attrMap, null);
    }

    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        String tag = "";
        try
        {
            while (tokenizer.atSTag())
            {
                tag = tokenizer.getTagName();
                if (tag.equals(SpecConstraints.xmlTag()))
                {
                    constraints.readXML(tokenizer);
                }
                else if (tag.equals(SpecParameterMap.xmlTag()))
                {
                    parameterMap.readXML(tokenizer);
                }
                else if (tag.equals(SpecParameterDefaults.xmlTag()))
                {
                    parameterdefaults.readXML(tokenizer);
                }
                else if (tag.equals(MotionUnitAssembler.xmlTag()))
                {
                    MotionUnitAssembler mua = new MotionUnitAssembler(resources);
                    mua.readXML(tokenizer);
                    motionUnit = mua.getMotionUnit();
                }
            }
        }
        catch (RuntimeException ex)
        {
            logger.warn("Cannot read motion unit spec, dropping element from gesture binding. Tag: {} ", tag);
            motionUnit = null;
        }
    }

    private static final String XMLTAG = "MotionUnitSpec";

    public static String xmlTag()
    {
        return XMLTAG;
    }

    @Override
    public String getXMLTag()
    {
        return XMLTAG;
    }

}
