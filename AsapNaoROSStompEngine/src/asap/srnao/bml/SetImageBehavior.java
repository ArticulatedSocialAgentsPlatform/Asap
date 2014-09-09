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
 * Displays an image on canvas
 */
public class SetImageBehavior extends PictureBehaviour
{
    private String filePath;
    private String fileName;

    @Override
    public boolean satisfiesConstraint(String name, String value)
    {
        if (name.equals("filePath")) return true;
        if (name.equals("fileName")) return true;
        return super.satisfiesConstraint(name, value);
    }

    public SetImageBehavior(String bmlId, XMLTokenizer tokenizer) throws IOException
    {
        super(bmlId);
        readXML(tokenizer);
    }

    @Override
    public StringBuilder appendAttributeString(StringBuilder buf, XMLFormatting fmt)
    {
        appendAttribute(buf, "filePath", filePath.toString());
        appendAttribute(buf, "fileName", fileName.toString());
        return super.appendAttributeString(buf, fmt);
    }

    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        filePath = getRequiredAttribute("filePath", attrMap, tokenizer);
        fileName = getRequiredAttribute("fileName", attrMap, tokenizer);
        super.decodeAttributes(attrMap, tokenizer);
    }

    /*
     * The XML Stag for XML encoding
     */
    private static final String XMLTAG = "setImage";

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
        if (name.equals("filePath"))
        {
            return filePath;
        }
        if (name.equals("fileName"))
        {
            return fileName;
        }
        return super.getStringParameterValue(name);
    }

    @Override
    public boolean specifiesParameter(String name)
    {
        if (name.equals("filePath") || name.equals("fileName"))
        {
            return true;
        }
        return super.specifiesParameter(name);
    }

    @Override
    public float getFloatParameterValue(String name)
    {
        return super.getFloatParameterValue(name);
    }
}
