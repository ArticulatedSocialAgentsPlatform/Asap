/*******************************************************************************
 *******************************************************************************/
package asap.timemanipulator;

/**
 * Unit tests for the GammaManipulator
 * @author hvanwelbergen
 *
 */
public class GammaManipulatorTest extends AbstractTimeManipulatorTest
{

    @Override
    protected TimeManipulator getManipulator()
    {
        return new GammaManipulator(1);
    }
    
}
