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

import hmi.math.HermiteSpline;

import java.util.*;
import org.nfunk.jep.*;
import org.nfunk.jep.function.*;

/**
 * JEP implementation of {@link project.mathutils.HermiteSpline}
 * 
 * @author welberge
 */
public class HermiteSplineJEP extends PostfixMathCommand
{
    private HermiteSpline spline;

    /**
     * Constructor
     */
    public HermiteSplineJEP()
    {
        numberOfParameters = -1;
        spline = new HermiteSpline();
    }

    /**
     * Runs the hermite spline operation on the inStack. The parameters are
     * popped off the <code>inStack</code>, and the interpolated value is pushed
     * back to the top of <code>inStack</code>.
     */
    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void run(Stack inStack) throws ParseException
    {

        // check the stack
        checkStack(inStack);

        // get the parameter from the stack

        // check whether the argument is of the right type
        if (curNumberOfParameters < 5)
        {
            throw new ParseException("Invalid number of parameters");
        }

        int n = curNumberOfParameters - 3;
        float values[] = new float[n];
        for (int i = n - 1; i >= 0; i--)
        {
            values[i] = ((Double) inStack.pop()).floatValue();
        }
        spline.setInterpolationPoints(values);

        Object param3 = inStack.pop();
        Object param2 = inStack.pop();
        Object param1 = inStack.pop();

        Double t = (Double) param1;
        Double m0 = (Double) param2;
        Double mn = (Double) param3;
        spline.setM0(m0.floatValue());
        spline.setMn(mn.floatValue());

        inStack.push(Double.valueOf(spline.eval(t.floatValue())));
    }
}
