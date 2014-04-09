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
package asap.realizer.scheduler;

import saiba.bml.parser.Constraint;
import asap.realizer.pegboard.TimePeg;

/**
 * Links a constraint to a timepeg and a behavior sync point id
 * @author welberge
 *
 */
public final class TimePegAndConstraint
{
    public final TimePeg peg;
    public final String syncId;                       ///id of syncpoint in the behaviour this SynchronisationAndConstraint acts on
    public final Constraint constr;               ///BML constraint this TimePegAndConstraint is linked to
    public final double offset;                   ///time offset irt constraint
    public final boolean resolveAsStartOffset;    ///peg is an offset peg, set it up as such if timing is unknown (add link, offset)
    
    public TimePegAndConstraint(String syncId, TimePeg sp, Constraint c, double o, boolean resolveAsStartOffset)
    {
        this.syncId = syncId;         
        peg = sp;
        constr = c;
        offset = o;
        this.resolveAsStartOffset = resolveAsStartOffset;
    }
    
    public TimePegAndConstraint(String i, TimePeg sp, Constraint c, double o)
    {
        this(i,sp,c,o,false);
    }
    
    @Override
    public String toString()
    {
        return "Peg: "+peg+" , id: "+syncId+" constr: "+constr+" offset: "+offset; 
    }
}
