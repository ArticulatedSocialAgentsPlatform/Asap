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

import hmi.math.TCBSpline;

import java.util.*;
import org.nfunk.jep.*;
import org.nfunk.jep.function.*;

/**
 * JEP implementation of {@link project.mathutils.TCBSpline} Syntax:
 * tcbspline(t,v0,vn, p0,t0, pi,ti,tensi,conti,biasi,...,pn,tn) With t = time, 0
 * &lt t &lt 1 v0 = start speed vn = end speed pi = point i ti = time stamp i,
 * ti-1 &lt ti &lt ti+1 tensi = tensity i, -1 &lt tensity &lt 1, default = 0
 * conti = continuity i, -1 &lt conti &lt 1, default = 0 biasi = bias i, -1 &lt
 * bias &lt 1, default = 0
 * 
 * @author welberge
 */
public class TCBSplineJEP extends PostfixMathCommand
{
    private TCBSpline spline;

    /**
     * Constructor
     */
    public TCBSplineJEP()
    {
        numberOfParameters = -1;
        spline = new TCBSpline();
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
        if (curNumberOfParameters < 7)
        {
            throw new ParseException("Invalid number of parameters");
        }

        int n = curNumberOfParameters - 7;

        if (n % 5 != 0)
        {
            throw new ParseException("Parameters not provide in 5's");
        }

        // N := number of points
        n /= 5;
        n += 2;

        float points[] = new float[n];
        float times[] = new float[n];
        float ten[] = new float[n];
        float c[] = new float[n];
        float b[] = new float[n];

        times[n - 1] = ((Double) inStack.pop()).floatValue();
        points[n - 1] = ((Double) inStack.pop()).floatValue();
        for (int i = n - 2; i >= 1; i--)
        {
            b[i] = ((Double) inStack.pop()).floatValue();
            c[i] = ((Double) inStack.pop()).floatValue();
            ten[i] = ((Double) inStack.pop()).floatValue();
            times[i] = ((Double) inStack.pop()).floatValue();
            points[i] = ((Double) inStack.pop()).floatValue();
        }
        times[0] = ((Double) inStack.pop()).floatValue();
        points[0] = ((Double) inStack.pop()).floatValue();

        spline.setInterpolationPoints(points);
        spline.setTension(ten);
        spline.setInterpolationTimes(times);
        spline.setBias(b);
        spline.setContinuity(c);

        Object param3 = inStack.pop();
        Object param2 = inStack.pop();
        Object param1 = inStack.pop();

        Float t = ((Double) param1).floatValue();
        Float m0 = ((Double) param2).floatValue();
        Float mn = ((Double) param3).floatValue();
        spline.setM0(m0);
        spline.setMn(mn);

        inStack.push(Double.valueOf(spline.eval(t.floatValue())));

    }
}
