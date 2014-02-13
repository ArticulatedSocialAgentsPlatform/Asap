package asap.bml.ext.bmla.feedback;

import hmi.xml.XMLTokenizer;

import java.util.HashMap;

import lombok.Getter;
import saiba.bml.feedback.BMLBlockProgressFeedback;
import asap.bml.ext.bmla.BMLAInfo;

/**
 * BMLBlockProgressFeedback with posixTime
 * @author hvanwelbergen
 */
public class BMLABlockProgressFeedback extends BMLBlockProgressFeedback
{
    @Getter
    private double posixTime = 0;    
    private static final String POSIXTIME_ID = BMLAInfo.BMLA_NAMESPACE+":"+"posixTime";
    
    public BMLABlockProgressFeedback()
    {
        super();
    }
    
    public BMLABlockProgressFeedback(String bmlId, String syncId, double globalTime, double posixTime)
    {
        super(bmlId, syncId, globalTime);
        setPosixTime(posixTime);
    }
    
    public static BMLABlockProgressFeedback build(BMLBlockProgressFeedback fb)
    {
        double posixTime = 0;
        if(fb.specifiesCustomFloatParameter(POSIXTIME_ID))
        {
            posixTime = fb.getCustomFloatParameterValue(POSIXTIME_ID);
        }
        BMLABlockProgressFeedback fbNew = new BMLABlockProgressFeedback(fb.getBmlId(), fb.getSyncId(), fb.getGlobalTime(), posixTime);
        fbNew.setCharacterId(fb.getCharacterId());
        return fbNew;
    }
    
    private void setPosixTime(double time)
    {
        posixTime = time;
        if(posixTime>0)
        {
            addCustomFloatParameterValue(POSIXTIME_ID, (float)posixTime);
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
