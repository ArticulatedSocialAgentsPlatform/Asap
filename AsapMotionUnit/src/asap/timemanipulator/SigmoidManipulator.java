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

import hmi.math.MathUtils;

/**
 * Provides slow-fast-slow interpolation (=a bubble-shaped velocity profile)
 * manip(t)=0.5 . (1+tanh(a.x^p) - 0.5 a determines the steepness p determines
 * the lenght of the accelatory phase plausible values: a=3,p=4
 * 
 * @author welberge
 */
public class SigmoidManipulator implements TimeManipulator
{
    private double a, p;

    /**
     * Constructor
     * 
     * @param steepness
     * @param acclength
     *            length of the accelatory phase
     */
    public SigmoidManipulator(double steepness, double acclength)
    {
        a = steepness;
        p = acclength;
    }

    /**
     * manipulates t
     * 
     * @return manip(t)=0.5 . (1+tanh(a.x^p) - 0.5
     */
    public double manip(double t)
    {
        return 0.5 * (1 + MathUtils.tanh(a * (Math.pow(t, p) - 0.5)));
    }

}
