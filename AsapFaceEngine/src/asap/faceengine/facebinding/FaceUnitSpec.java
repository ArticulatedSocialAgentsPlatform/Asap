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
package asap.faceengine.facebinding;

import hmi.bml.core.Behaviour;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import asap.faceengine.faceunit.FaceUnit;

class FaceUnitSpec extends XMLStructureAdapter
{
    public FaceUnit faceUnit;
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


    private ArrayList<FaceUnitSpecConstraint>constraints = new ArrayList<FaceUnitSpecConstraint>();
    private HashMap<String,String>parametermap = new HashMap<String,String>();
    private HashMap<String,FaceUnitParameterDefault>parameterdefault = new HashMap<String,FaceUnitParameterDefault>();
    
    
    public boolean satisfiesConstraints(Behaviour b)
    {
        for(FaceUnitSpecConstraint c:constraints)
        {
            if(!b.satisfiesConstraint(c.name, c.value))return false;
        }
        return true;
    }
    
    public Set<String> getParameters()
    {
        return parametermap.keySet();
    }
    
    public void addConstraint(FaceUnitSpecConstraint c)
    {
        constraints.add(c);
    }
    
    public void addParameter(FaceUnitParameter p)
    {
        parametermap.put(p.src, p.dst);
    }
    
    public void addParameterDefault(FaceUnitParameterDefault p)
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
    public Collection<FaceUnitParameterDefault> getParameterDefaults()
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
            if (tag.equals(FaceUnitSpecConstraints.xmlTag()))
            {
                FaceUnitSpecConstraints fusc = new FaceUnitSpecConstraints(this);
                fusc.readXML(tokenizer);                
            }
            else if (tag.equals(FBParameterMap.xmlTag())) 
            {
                FBParameterMap map = new FBParameterMap(this);
                map.readXML(tokenizer);
            }
            else if (tag.equals(FBParameterDefaults.xmlTag())) 
            {
                FBParameterDefaults def = new FBParameterDefaults(this);
                def.readXML(tokenizer);
            }
            else if (tag.equals(FaceUnitAssembler.xmlTag()))
            {
                FaceUnitAssembler fua = new FaceUnitAssembler();
                fua.readXML(tokenizer);
                faceUnit = fua.getFaceUnit();
            }
        }
    }
    
   /*
    * The XML Stag for XML encoding
    */
   private static final String XMLTAG = "FaceUnitSpec";
 
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
