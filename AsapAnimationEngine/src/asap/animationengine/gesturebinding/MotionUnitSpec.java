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

import hmi.util.Resources;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import saiba.bml.core.Behaviour;
import asap.animationengine.motionunit.AnimationUnit;
import asap.binding.SpecConstraints;
import asap.binding.SpecParameterDefault;
import asap.binding.SpecParameterDefaults;
import asap.binding.SpecParameterMap;

/**
 * XML parser for the MotionUnitSpec in a gesturebinding
 * @author Herwin
 * 
 */
@Slf4j
class MotionUnitSpec extends XMLStructureAdapter
{
    public AnimationUnit motionUnit;
    
    @Getter
    private String type;
    
    @Getter
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
            log.warn("Cannot read motion unit spec, dropping element from gesture binding. Tag: {} ", tag);
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
