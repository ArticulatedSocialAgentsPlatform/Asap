package asap.timemanipulator;

public class GammaManipulatorTest extends AbstractTimeManipulatorTest
{

    @Override
    protected TimeManipulator getManipulator()
    {
        return new GammaManipulator(1);
    }
    
}
