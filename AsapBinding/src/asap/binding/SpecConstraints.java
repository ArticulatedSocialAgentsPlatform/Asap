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
package asap.binding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import saiba.bml.core.Behaviour;

import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

/**
 * An XML element to group SpecConstraint-s
 * @author welberge
 *
 */
public class SpecConstraints extends XMLStructureAdapter
{
    private List<SpecConstraint> constraints = new ArrayList<SpecConstraint>(); 
    
    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        while (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            if (tag.equals(SpecConstraint.xmlTag()))
            {
                SpecConstraint c = new SpecConstraint();
                c.readXML(tokenizer);
                constraints.add(c);
            }
            else
            {
                throw new XMLScanException("Unknown XML element "+tag+" in constraints");
            }
        }
    }

    public boolean satisfiesConstraints(Behaviour b)
    {
        for (SpecConstraint c : constraints)
        {
            if (c.namespace!=null)
            {
                if (!b.satisfiesConstraint(c.namespace+":"+c.name, c.value)) return false;
            }
            else
            {
                if (!b.satisfiesConstraint(c.name, c.value)) return false;
            }
        }
        return true;
    }
    
    public static final String XMLTAG = "constraints";

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
