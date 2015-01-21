/*******************************************************************************
 *******************************************************************************/
package asap.timemanipulator;

/**
 * unit tests for the SigmoidManipulator
 * @author hvanwelbergen
 *
 */
public class SigmoidManipulatorTest extends AbstractTimeManipulatorTest
{

    @Override
    protected TimeManipulator getManipulator()
    {
        return new SigmoidManipulator(4,3);
    }
    
}
