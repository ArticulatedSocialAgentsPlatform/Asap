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
package asap.animationengine.procanimation;

import hmi.math.PerlinNoise;

import java.util.*;
import org.nfunk.jep.function.*;
import org.nfunk.jep.*;

/**
 * JEP implementation of {@link project.mathutils.PerlinNoise}
 * 
 * @author welberge
 */
public class PerlinNoiseJEP extends PostfixMathCommand
{
    private PerlinNoise pnoise;

    /**
     * Constructor
     */
    public PerlinNoiseJEP()
    {
        numberOfParameters = 2;
        pnoise = new PerlinNoise(0, 1);
    }

    /**
     * Runs the perlin noise operation on the inStack. The parameter is popped
     * off the <code>inStack</code>, and the noise value is pushed back to the
     * top of <code>inStack</code>.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void run(@SuppressWarnings("rawtypes") Stack inStack) throws ParseException
    {

        // check the stack
        checkStack(inStack);

        // get the parameter from the stack
        Object param2 = inStack.pop();
        Object param1 = inStack.pop();

        // check whether the argument is of the right type
        if (param1 instanceof Double && param2 instanceof Double)
        {
            // calculate the result
            Double p2 = (Double) param2;
            float r = pnoise.noise(((Double) param1).floatValue(), p2
                    .intValue());
            // push the result on the inStack
            inStack.push(Double.valueOf(r));
        } else
        {
            throw new ParseException("Invalid parameter type");
        }
    }
}
