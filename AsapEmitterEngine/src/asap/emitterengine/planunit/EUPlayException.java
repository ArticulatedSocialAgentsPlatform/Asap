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
package asap.emitterengine.planunit;


import asap.realizer.PlayException;

/**
 * Thrown whenever an EmitterUnit fails during play
 * @author Dennis Reidsma
 */
public class EUPlayException extends PlayException
{
    private static final long serialVersionUID = 1423L;
    private final EmitterUnit eu;
    
    public EUPlayException(String str, EmitterUnit e, Exception ex)
    {
        this(str,e);
        initCause(ex);
    }
    
    public EUPlayException(String str, EmitterUnit e)
    {
        super(str);
        eu = e;
    }
    
    public final EmitterUnit getEmitterUnit()
    {
        return eu;
    }
}