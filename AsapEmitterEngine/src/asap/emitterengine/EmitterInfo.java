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
package asap.emitterengine;


import java.util.*;

import asap.emitterengine.bml.*;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 
 * @author Dennis Reidsma
 */
public abstract class EmitterInfo 
{
    
    public abstract String getNamespace();
    public static String namespace()
    {
      return null;
    }
    public abstract String getXMLTag();
    public static String xmlTag()
    {
      return null;
    }

    public  boolean specifiesFloatParameter(String name)
    {
      return false;
    }
    public  boolean specifiesStringParameter(String name)
    {
      return false;
    }
    
    public  ArrayList<String> getOptionalParameters()
    {
      return new ArrayList<String>();
    }
    public  ArrayList<String> getRequiredParameters()
    {
      return new ArrayList<String>();
    }
    
    public abstract Class<? extends Emitter> getEmitterClass();
    public abstract Class<? extends CreateEmitterBehaviour> getCreateEmitterBehaviour();
     
}
