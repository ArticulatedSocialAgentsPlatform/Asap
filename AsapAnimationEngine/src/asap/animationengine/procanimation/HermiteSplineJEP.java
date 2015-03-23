/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.procanimation;

import hmi.math.HermiteSpline;

import java.util.Stack;

import org.nfunk.jep.ParseException;
import org.nfunk.jep.function.PostfixMathCommand;

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
