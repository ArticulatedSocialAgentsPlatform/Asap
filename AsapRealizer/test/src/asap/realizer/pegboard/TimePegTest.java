/*******************************************************************************
 *******************************************************************************/
package asap.realizer.pegboard;


/**
 * Unit tests for the TimePeg
 * @author hvanwelbergen
 * 
 */
public class TimePegTest extends AbstractTimePegTest
{

    @Override
    public TimePeg createTimePeg(BMLBlockPeg peg)
    {
        return new TimePeg(peg);
    }    
}
