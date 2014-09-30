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
package asap.srnao.naobinding;

import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import saiba.bml.core.Behaviour;
import asap.binding.SpecConstraints;
import asap.binding.SpecParameterDefault;
import asap.binding.SpecParameterDefaults;
import asap.binding.SpecParameterMap;
import asap.srnao.planunit.NaoUnit;

/**
 * XML parser for the NaoUnitSpec in a naobinding
 * @author Daniel
 */
public class NaoUnitSpec extends XMLStructureAdapter
{
    public NaoUnit naoUnit;
    private String type;
    private String specnamespace;
    private SpecParameterMap parametermap = new SpecParameterMap();
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

    private SpecConstraints constraints = new SpecConstraints();
    private SpecParameterDefaults parameterdefaults = new SpecParameterDefaults();

    public boolean satisfiesConstraints(Behaviour b)
    {
        return constraints.satisfiesConstraints(b);        
    }

    public Set<String> getParameters()
    {
        return parametermap.getParameters();
    }    

    
    /**
     * Get motion unit parameter for BML parameter src
     */
    public String getParameter(String src)
    {
        return parametermap.getParameter(src);
    }

    /**
     * Get motion unit parameter for BML parameter src
     */
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
        while (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            if (tag.equals(SpecConstraints.xmlTag()))
            {
                SpecConstraints nusc = new SpecConstraints();
                nusc.readXML(tokenizer);
            }
            else if (tag.equals(SpecParameterMap.xmlTag()))
            {
                parametermap.readXML(tokenizer);
            }
            else if (tag.equals(SpecParameterDefaults.xmlTag()))
            {
                parameterdefaults.readXML(tokenizer);
            }
            else if (tag.equals(NaoUnitAssembler.xmlTag()))
            {
                NaoUnitAssembler nua = new NaoUnitAssembler();
                nua.readXML(tokenizer);
                naoUnit = nua.getNaoUnit();
            }
        }
    }

    /*
     * The XML Stag for XML encoding
     */
    private static final String XMLTAG = "NaoUnitSpec";

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
