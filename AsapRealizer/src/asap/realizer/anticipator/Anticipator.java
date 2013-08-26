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
package asap.realizer.anticipator;



import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.jcip.annotations.ThreadSafe;
import asap.realizer.pegboard.TimePeg;

import com.google.common.collect.ImmutableList;

/**
 * An Anticipator manages a set of TimePegs that can be used to synchronize behavior to.
 * Anticipators typically store predictions of time events on the basis of observed interlocutor
 * behavior in their TimePegs. This predictions are often incremental, and increase in precision over
 * time. 
 * @author welberge
 */
@ThreadSafe
public class Anticipator
{
    private Map<String,TimePeg> pegs;
    
    /**
     * Get an ImmutableList copy of the time pegs handled by this anticipator  
     */
    public synchronized Collection<TimePeg> getTimePegs()
    {
        return ImmutableList.copyOf(pegs.values());
    }
    
    public synchronized TimePeg getSynchronisationPoint(String syncRef)
    {
        return pegs.get(syncRef);
    }
    
    public synchronized void addSynchronisationPoint(String syncRef, TimePeg sp)
    {
        pegs.put(syncRef, sp);
    }
    
    public synchronized void setSynchronisationPoint(String syncRef, double time)
    {
        TimePeg sp = pegs.get(syncRef);
        sp.setGlobalValue(time);
    }
    
    public Anticipator()
    {
        pegs = new ConcurrentHashMap<String,TimePeg>();
    }
}
