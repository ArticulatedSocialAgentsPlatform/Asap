package asap.timemanipulator;

public class SigmoidManipulatorTest extends AbstractTimeManipulatorTest
{

    @Override
    protected TimeManipulator getManipulator()
    {
        return new SigmoidManipulator(4,3);
    }
    
}
