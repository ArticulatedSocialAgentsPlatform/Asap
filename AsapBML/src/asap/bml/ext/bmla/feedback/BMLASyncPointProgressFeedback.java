package asap.bml.ext.bmla.feedback;

import hmi.xml.XMLTokenizer;

import java.util.HashMap;

import lombok.Getter;
import saiba.bml.feedback.BMLSyncPointProgressFeedback;
import asap.bml.ext.bmla.BMLAInfo;

/**
 * Extension of BMLSyncPointProgressFeedback to allow posix times in feedback
 * @author hvanwelbergen
 * 
 */
public class BMLASyncPointProgressFeedback extends BMLSyncPointProgressFeedback
{
    @Getter
    private double posixTime = 0;
    private static final String POSIXTIME_ID = BMLAInfo.BMLA_NAMESPACE + ":" + "posixTime";

    public BMLASyncPointProgressFeedback()
    {
        super();
    }

    public BMLASyncPointProgressFeedback(String bmlId, String behaviorId, String syncId, double time, double globalTime, double posixTime)
    {
        super(bmlId, behaviorId, syncId, time, globalTime);
        setPosixTime(posixTime);
    }

    public static BMLASyncPointProgressFeedback build(BMLSyncPointProgressFeedback fb)
    {
        double posixTime = 0;
        if (fb.specifiesCustomFloatParameter(POSIXTIME_ID))
        {
            posixTime = fb.getCustomFloatParameterValue(POSIXTIME_ID);
        }
        BMLASyncPointProgressFeedback fbNew = new BMLASyncPointProgressFeedback(fb.getBMLId(), fb.getBehaviourId(), fb.getSyncId(),
                fb.getTime(), fb.getGlobalTime(), posixTime);
        fbNew.setCharacterId(fb.getCharacterId());
        return fbNew;
    }

    private void setPosixTime(double time)
    {
        posixTime = time;
        if (posixTime > 0)
        {
            addCustomFloatParameterValue(POSIXTIME_ID, (float) posixTime);
        }
    }

    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        super.decodeAttributes(attrMap, tokenizer);
        if (specifiesCustomFloatParameter(POSIXTIME_ID))
        {
            setPosixTime(getCustomFloatParameterValue(POSIXTIME_ID));
        }
    }

}
