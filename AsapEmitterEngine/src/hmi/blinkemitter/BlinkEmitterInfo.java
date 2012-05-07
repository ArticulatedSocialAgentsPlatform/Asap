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
package hmi.blinkemitter;

import hmi.emitterengine.*;
import hmi.emitterengine.bml.*;

import java.util.*;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 
 * @author Dennis Reidsma
 */
public class BlinkEmitterInfo extends EmitterInfo
{
    
    public BlinkEmitterInfo()
    {
      optionalParameters.add("range");
      optionalParameters.add("avgwaitingtime");
    }
      
    static final String BMLTNAMESPACE = "http://hmi.ewi.utwente.nl/bmlt";
    
    public static String namespace()
    {
      return BMLTNAMESPACE;
    }
    @Override
    public String getNamespace()
    {
      return BMLTNAMESPACE;
    }
    
    static final String XMLTAG = "blinkemitter";
    
    public static String xmlTag()
    {
      return XMLTAG;
    }
    @Override
    public String getXMLTag()
    {
      return XMLTAG;
    }

    @Override
    public  boolean specifiesFloatParameter(String name)
    {
      return optionalParameters.contains(name) || requiredParameters.contains(name);
    }
    @Override
    public  boolean specifiesStringParameter(String name)
    {
      return false;
    }
    
    private  ArrayList<String> optionalParameters = new ArrayList<String>();
    private  ArrayList<String> requiredParameters = new ArrayList<String>();
    
    @Override
    public  ArrayList<String> getOptionalParameters()
    {
      return optionalParameters;
    }

    @Override
    public  ArrayList<String> getRequiredParameters()
    {
      return requiredParameters;
    }

    @Override
    public Class<? extends Emitter> getEmitterClass()
    {
      return BlinkEmitter.class;
    }
    @Override
    public Class<? extends CreateEmitterBehaviour> getCreateEmitterBehaviour()
    {
      return CreateBlinkEmitterBehaviour.class;
    }
         
}
