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
package asap.picture.picturebinding;

import saiba.bml.core.Behaviour;
import asap.picture.planunit.PictureUnit;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public class PictureUnitSpec extends XMLStructureAdapter
{
    public PictureUnit pictureUnit;
    private String type;
    private String specnamespace;

    /**
     * @return the type
     */
    public String getType()
    {
        return type;
    }

    /**
     * @return the specnamespace
     */
    public String getSpecnamespace()
    {
        return specnamespace;
    }

    private ArrayList<PictureUnitSpecConstraint> constraints = new ArrayList<PictureUnitSpecConstraint>();
    private HashMap<String, String> parametermap = new HashMap<String, String>();
    private HashMap<String, PictureUnitParameterDefault> parameterdefault = new HashMap<String, PictureUnitParameterDefault>();

    public boolean satisfiesConstraints(Behaviour b)
    {
        for (PictureUnitSpecConstraint c : constraints)
        {
            if (!b.satisfiesConstraint(c.name, c.value)) 
            {
            	b.satisfiesConstraint(c.name, c.value);
            	return false;
            }
        }
        return true;
    }

    public Set<String> getParameters()
    {
        return parametermap.keySet();
    }

    public void addConstraint(PictureUnitSpecConstraint c)
    {
        constraints.add(c);
    }

    public void addParameter(PictureUnitParameter p)
    {
        parametermap.put(p.src, p.dst);
    }

    public void addParameterDefault(PictureUnitParameterDefault p)
    {
        parameterdefault.put(p.name, p);
    }

    /**
     * Get motion unit parameter for BML parameter src
     */
    public String getParameter(String src)
    {
        return parametermap.get(src);
    }

    /**
     * Get motion unit parameter for BML parameter src
     */
    public Collection<PictureUnitParameterDefault> getParameterDefaults()
    {
        return parameterdefault.values();
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
        while (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            if (tag.equals(PictureUnitSpecConstraints.xmlTag()))
            {
                PictureUnitSpecConstraints pusc = new PictureUnitSpecConstraints(this);
                pusc.readXML(tokenizer);
            }
            else if (tag.equals(PictureUnitParameterMap.xmlTag()))
            {
                PictureUnitParameterMap map = new PictureUnitParameterMap(this);
                map.readXML(tokenizer);
            }
            else if (tag.equals(PictureUnitParameterDefaults.xmlTag()))
            {
                PictureUnitParameterDefaults def = new PictureUnitParameterDefaults(this);
                def.readXML(tokenizer);
            }
            else if (tag.equals(PictureUnitAssembler.xmlTag()))
            {
                PictureUnitAssembler pua = new PictureUnitAssembler();
                pua.readXML(tokenizer);
                pictureUnit = pua.getPictureUnit();
            }
        }
    }

    /*
     * The XML Stag for XML encoding
     */
    private static final String XMLTAG = "PictureUnitSpec";

    /**
     * The XML Stag for XML encoding -- use this static method when you want to see if a given String equals
     * the xml tag for this class
     */
    public static String xmlTag()
    {
        return XMLTAG;
    }

    /**
     * The XML Stag for XML encoding -- use this method to find out the run-time xml tag of an object
     */
    @Override
    public String getXMLTag()
    {
        return XMLTAG;
    }

}
