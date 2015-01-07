/*******************************************************************************
 *******************************************************************************/
package asap.timemanipulator;

/**
 * Unit tests for LinearInterpolator
 * @author hvanwelbergen
 *
 */
public class LinearInterpolatorTest extends AbstractTimeManipulatorTest
{

    @Override
    protected TimeManipulator getManipulator()
    {
        return new LinearManipulator();
    }

}
