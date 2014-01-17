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

import lombok.Getter;
import net.jcip.annotations.ThreadSafe;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;

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
    
    private final PegBoard pegBoard;
    
    @Getter
    private final String id;
    
    public Anticipator(String id, PegBoard pb)
    {
        this.pegBoard = pb;
        this.id = id;
    }
    
    /**
     * Get an ImmutableList copy of the time pegs handled by this anticipator  
     */
    public synchronized Collection<TimePeg> getTimePegs()
    {
        return pegBoard.getTimePegs(BMLBlockPeg.ANTICIPATOR_PEG_ID, id);        
    }
    
    public synchronized TimePeg getSynchronisationPoint(String syncId)
    {
        return pegBoard.getTimePeg(BMLBlockPeg.ANTICIPATOR_PEG_ID,id, syncId);        
    }
    
    public synchronized void addSynchronisationPoint(String syncId, TimePeg sp)
    {
        pegBoard.addTimePeg(BMLBlockPeg.ANTICIPATOR_PEG_ID,id,syncId, sp);        
    }
    
    public synchronized void setSynchronisationPoint(String syncId, double time)
    {
        pegBoard.setPegTime(BMLBlockPeg.ANTICIPATOR_PEG_ID,id,syncId, time);
    }
}
