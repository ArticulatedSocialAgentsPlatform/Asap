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

import hmi.bml.core.Behaviour;

import hmi.util.*;
import hmi.xml.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asap.animationengine.motionunit.MotionUnit;

class MotionUnitSpec extends XMLStructureAdapter implements ParameterDefaultsHandler
{
    private final static Logger logger = LoggerFactory.getLogger(MotionUnitSpec.class.getName());

    public MotionUnit motionUnit;
    private String type;
    private String specnamespace;
    

    private ArrayList<MotionUnitSpecConstraint>constraints = new ArrayList<MotionUnitSpecConstraint>();
    private HashMap<String,String>parametermap = new HashMap<String,String>();
    private HashMap<String,MotionUnitParameterDefault>parameterdefault = new HashMap<String,MotionUnitParameterDefault>();
    
    private final Resources resources;
    
    public boolean satisfiesConstraints(Behaviour b)
    {
        for(MotionUnitSpecConstraint c:constraints)
        {
            if(!b.satisfiesConstraint(c.name, c.value))return false;
        }
        return true;
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
        return parametermap.keySet();
    }
    
    public MotionUnitSpec(Resources r)
    {
        resources = r;
    }
    
    public void addConstraint(MotionUnitSpecConstraint c)
    {
        constraints.add(c);
    }
    
    public void addParameter(MotionUnitParameter p)
    {
        parametermap.put(p.src, p.dst);
    }
    
    public void addParameterDefault(MotionUnitParameterDefault p)
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
    public Collection<MotionUnitParameterDefault> getParameterDefaults()
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
      String tag = "";
      try
      {
        while (tokenizer.atSTag())
        {
            tag = tokenizer.getTagName();
            if (tag.equals(MotionUnitSpecConstraints.xmlTag()))
            {
                MotionUnitSpecConstraints musc = new MotionUnitSpecConstraints(this);
                musc.readXML(tokenizer);                
            }
            else if (tag.equals(ParameterMap.xmlTag())) 
            {
                ParameterMap map = new ParameterMap(this);
                map.readXML(tokenizer);
            }
            else if (tag.equals(ParameterDefaults.xmlTag())) 
            {
                ParameterDefaults def = new ParameterDefaults(this);
                def.readXML(tokenizer);
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
    
   /*
    * The XML Stag for XML encoding
    */
   private static final String XMLTAG = "MotionUnitSpec";
 
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
