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

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asap.srnao.planunit.MoveJointNU;
import asap.srnao.planunit.NaoUnit;
import asap.srnao.planunit.RunChoregrapheClipNU;
import asap.srnao.planunit.SetJointAngleNU;

public class NaoUnitAssembler extends XMLStructureAdapter
{
    private static Logger logger = LoggerFactory
            .getLogger(NaoUnitAssembler.class.getName());

    private NaoUnit naoUnit;

    public NaoUnitAssembler()
    {
    }

    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        String type = getRequiredAttribute("type", attrMap, tokenizer);

        if (type.equals("RunChoregrapheClipNU"))
        {
            naoUnit = new RunChoregrapheClipNU();
        } else if (type.equals("SetJointAngleNU"))
        {
            naoUnit = new SetJointAngleNU();
        } else if (type.equals("MoveJointNU"))
        {
            naoUnit = new MoveJointNU();
        }
        else
        {
          logger.warn("Cannot read NaoUnit type \"{}\" in NaoBinding; omitting this NaoUnit", type);
        }
        
    }

    /**
     * @return the naoUnit
     */
    public NaoUnit getNaoUnit()
    {
        return naoUnit;
    }

   /*
    * The XML Stag for XML encoding
    */
   private static final String XMLTAG = "NaoUnit";
 
   /**
    * The XML Stag for XML encoding -- use this static method when you want to see if a given String equals
    * the xml tag for this class
    */
   public static String xmlTag() { return XMLTAG; }
 
   /**
    * The XML Stag for XML encoding -- use this method to find out the run-time xml tag of an object
    */
   @Override
   public String getXMLTag() {
      return XMLTAG;
   }
}
