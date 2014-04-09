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



import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.jcip.annotations.ThreadSafe;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;

/**
 * Anticipates the (may be changing through calls of updateTempo) tempo of a metronome. 
 * @author welberge
 */
@ThreadSafe
public class MetronomeAnticipator extends Anticipator
{
    public MetronomeAnticipator(String id, PegBoard pb)
    {
        super(id, pb);        
    }

    private List<TimePeg> orderedSynchs = Collections.synchronizedList(new ArrayList<TimePeg>());
    
    @Override
    public void addSynchronisationPoint(String syncRef, TimePeg sp)
    {
        super.addSynchronisationPoint(syncRef,sp);
        orderedSynchs.add(sp);
    }
    
    public void updateTempo(double tempo, double startTime)
    {
        double time = 0;
        boolean update = false;
        synchronized(orderedSynchs)
        {
            for(TimePeg p:orderedSynchs)
            {
                if(update)
                {
                    //TODO: in extreme tempo changes, this might move p.value to an earlier value as the previous value in the list. fix.
                    time+=tempo;
                    p.setGlobalValue(time);
                }
                else if(p.getGlobalValue()==TimePeg.VALUE_UNKNOWN || p.getGlobalValue()>startTime)                
                {
                    time = p.getGlobalValue();
                    update = true;
                }
            }
        }
    }
}
