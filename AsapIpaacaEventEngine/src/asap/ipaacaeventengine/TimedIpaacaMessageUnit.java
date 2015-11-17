package asap.ipaacaeventengine;

import java.util.Map;

import ipaaca.LocalMessageIU;
import ipaaca.OutputBuffer;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.TimedEventUnit;
import asap.realizer.planunit.TimedPlanUnitPlayException;

/**
 * Sends an ipaaca event
 * @author hvanwelbergen
 *
 */
public class TimedIpaacaMessageUnit extends TimedEventUnit
{
    private final OutputBuffer outBuffer; 
    private final LocalMessageIU message;
    
    public TimedIpaacaMessageUnit(FeedbackManager bfm, BMLBlockPeg bmlPeg, String bmlId, String behId, OutputBuffer outBuffer, String category, Map<String,String> payload)
    {
        super(bfm,bmlPeg, bmlId, behId);     
        this.outBuffer = outBuffer;
        message = new LocalMessageIU(category);
        message.setPayload(payload);
    }

    @Override
    protected void startUnit(double time) throws TimedPlanUnitPlayException
    {
        feedback("start",time);
        outBuffer.add(message);
    }
}
