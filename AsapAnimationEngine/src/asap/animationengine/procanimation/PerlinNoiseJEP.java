/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.procanimation;

import hmi.math.PerlinNoise;

import java.util.Stack;

import org.nfunk.jep.ParseException;
import org.nfunk.jep.function.PostfixMathCommand;

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
