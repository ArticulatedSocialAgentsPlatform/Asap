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
import java.util.HashMap;
import java.util.Set;

import hmi.xml.*;

/**
 * Maps BML behavior parameters to planunit parameters
 * @author welberge
 *
 */
class SpecParameterMap extends XMLStructureAdapter
{
    private HashMap<String, String> parametermap = new HashMap<String, String>();

    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        while (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            if (tag.equals(SpecParameter.xmlTag()))
            {
                SpecParameter mup = new SpecParameter();
                mup.readXML(tokenizer);
                parametermap.put(mup.src, mup.dst);
            }
        }
    }

    public Set<String> getParameters()
    {
        return parametermap.keySet();
    }

    public String getParameter(String src)
    {
        return parametermap.get(src);
    }
    
    private static final String XMLTAG = "parametermap";

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
