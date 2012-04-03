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
package asap.timemanipulator;

/**
 * manip(t)=t^gamma gamma &lt; 1 gives a fast-to-slow interpolation gamma &gt; 1
 * gives a slow-to-fast interpolation
 * 
 * @author welberge
 */
public class GammaManipulator implements TimeManipulator
{
    private double gamma;

    /**
     * Constructor
     * 
     * @param g
     *            the gamma value
     */
    public GammaManipulator(double g)
    {
        gamma = g;
    }

    /**
     * manipulates t
     * 
     * @param t
     *            : time to manipulate
     * @return t^gamma
     */
    public double manip(double t)
    {
        return Math.pow(t, gamma);
    }
}
