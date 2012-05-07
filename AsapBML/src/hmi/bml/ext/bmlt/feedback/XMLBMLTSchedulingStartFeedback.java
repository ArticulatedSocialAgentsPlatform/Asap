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
package hmi.bml.ext.bmlt.feedback;

import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.util.HashMap;

/**
 * XML wrapper for BMLTSchedulingStartFeedback
 * 
 * @author Dennis Reidsma
 */
public final class XMLBMLTSchedulingStartFeedback extends XMLStructureAdapter
{
    public double timeStamp;
    public String id;
    public String bmlId;
    public double predictedStart;

    public XMLBMLTSchedulingStartFeedback()
    {
    }

    public XMLBMLTSchedulingStartFeedback(BMLTSchedulingStartFeedback fb)
    {
        timeStamp = fb.timeStamp;
        id = fb.id;
        bmlId = fb.bmlId;
        predictedStart = fb.predictedStart;
    }

    public BMLTSchedulingStartFeedback getBMLTPlanningStartFeedback()
    {
        return new BMLTSchedulingStartFeedback(id, bmlId, timeStamp, predictedStart);
    }

    @Override
    public StringBuilder appendAttributes(StringBuilder buf)
    {
        appendAttribute(buf, "id", id);
        appendAttribute(buf, "bmlId", bmlId);
        appendAttribute(buf, "timeStamp", timeStamp);
        appendAttribute(buf, "predictedStart", predictedStart);
        return buf;
    }

    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        id = getRequiredAttribute("id", attrMap, tokenizer);
        bmlId = getRequiredAttribute("bmlId", attrMap, tokenizer);
        timeStamp = getRequiredFloatAttribute("timeStamp", attrMap, tokenizer);
        predictedStart = getRequiredFloatAttribute("predictedStart", attrMap, tokenizer);
    }

    /*
     * The XML Stag for XML encoding
     */
    private static final String XMLTAG = "BMLTSchedulingStartFeedback";

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

}
