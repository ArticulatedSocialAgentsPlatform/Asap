package asap.ipaacaeventengine;

import ipaaca.LocalMessageIU;

import java.util.Map;

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
    private final MessageManager messageManager; 
    private final LocalMessageIU message;
    
    public TimedIpaacaMessageUnit(FeedbackManager bfm, BMLBlockPeg bmlPeg, String bmlId, String behId, MessageManager messageManager, String category, Map<String,String> payload)
    {
        super(bfm,bmlPeg, bmlId, behId);     
        this.messageManager = messageManager;
        message = new LocalMessageIU(category);
        message.setPayload(payload);
    }

    @Override
    protected void startUnit(double time) throws TimedPlanUnitPlayException
    {
        feedback("start",time);
        messageManager.sendMessage(message);
    }
}
