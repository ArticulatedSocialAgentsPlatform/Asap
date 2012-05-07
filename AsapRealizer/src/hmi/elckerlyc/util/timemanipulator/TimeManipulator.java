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
package hmi.elckerlyc.util.timemanipulator;

/**
 * Manipulates animation time, modifying the velocity of an IKMove
 * 
 * @author welberge
 */
public interface TimeManipulator 
{
    /**
     * Get the manipulated value of 0 &lt t &lt 1 Implementations should adhere
     * to the following rules: manip(0)=0 manip(1)=1 for every 0 &lt t1 &lt 1, 0
     * &lt t2 &lt 1: if t1 &lt t2 then manip(t1)<manip(t2)
     * 
     * @param t
     *            the time to be manipulated
     * @return the manipulated time
     */
    double manip(double t);
}
