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
package asap.faceengine.faceunit;

import asap.motionunit.MUPlayException;

/**
 * Thrown whenever a FaceUnit fails during play
 * @author Dennis Reidsma
 */
public class FUPlayException extends MUPlayException
{
    private static final long serialVersionUID = 1L;
    private final FaceUnit fu;
    
    public FUPlayException(String str, FaceUnit f, Exception ex)
    {
        this(str,f);
        initCause(ex);
    }
    
    public FUPlayException(String str, FaceUnit f)
    {
        super(str,f);
        fu = f;
    }
    
    public final FaceUnit getFaceUnit()
    {
        return fu;
    }
}
