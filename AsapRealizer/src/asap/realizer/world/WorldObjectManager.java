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
package asap.realizer.world;

import java.util.HashMap;

/**
 * Keeps track of WorldObjects
 * @author Herwin
 *
 */
public class WorldObjectManager
{
    private HashMap<String,WorldObject> worldObjectMap;
    
    
    public WorldObjectManager()
    {
        worldObjectMap = new HashMap<String,WorldObject>();        
    }
    
    public void addWorldObject(String id, WorldObject obj)
    {
        worldObjectMap.put(id, obj);        
    }
    
    public WorldObject getWorldObject(String id)
    {
        return worldObjectMap.get(id);
    }
}
