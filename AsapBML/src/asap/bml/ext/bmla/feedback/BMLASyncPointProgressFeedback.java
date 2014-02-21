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
    private long posixTime = 0;
    private static final String POSIXTIME_ID = BMLAInfo.BMLA_NAMESPACE + ":" + "posixTime";

    public BMLASyncPointProgressFeedback()
    {
        super();
    }

    public BMLASyncPointProgressFeedback(String bmlId, String behaviorId, String syncId, double time, double globalTime, long posixTime)
    {
        super(bmlId, behaviorId, syncId, time, globalTime);
        setPosixTime(posixTime);
    }

    public static BMLASyncPointProgressFeedback build(BMLSyncPointProgressFeedback fb)
    {
        long posixTime = 0;
        if (fb.specifiesCustomStringParameter(POSIXTIME_ID))
        {
            posixTime = Long.parseLong(fb.getCustomStringParameterValue(POSIXTIME_ID));
        }
        BMLASyncPointProgressFeedback fbNew = new BMLASyncPointProgressFeedback(fb.getBMLId(), fb.getBehaviourId(), fb.getSyncId(),
                fb.getTime(), fb.getGlobalTime(), posixTime);
        fbNew.setCharacterId(fb.getCharacterId());
        return fbNew;
    }

    public void setPosixTime(long time)
    {
        posixTime = time;
        if (posixTime > 0)
        {
            addCustomStringParameterValue(POSIXTIME_ID, ""+posixTime);
        }
    }

    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        super.decodeAttributes(attrMap, tokenizer);
        if (specifiesCustomStringParameter(POSIXTIME_ID))
        {
            setPosixTime(Long.parseLong(getCustomStringParameterValue(POSIXTIME_ID)));
        }
    }

}
