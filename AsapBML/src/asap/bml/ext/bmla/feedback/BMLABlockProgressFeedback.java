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
    private long posixTime = 0;    
    private static final String POSIXTIME_ID = BMLAInfo.BMLA_NAMESPACE+":"+"posixTime";
    
    public BMLABlockProgressFeedback()
    {
        super();
    }
    
    public BMLABlockProgressFeedback(String bmlId, String syncId, double globalTime)
    {
        this(bmlId, syncId, globalTime, System.currentTimeMillis());
    }
    
    public BMLABlockProgressFeedback(String bmlId, String syncId, double globalTime, long posixTime)
    {
        super(bmlId, syncId, globalTime);
        setPosixTime(posixTime);
    }
    
    public static BMLABlockProgressFeedback build(BMLBlockProgressFeedback fb)
    {
        BMLABlockProgressFeedback fbNew = new BMLABlockProgressFeedback();
        fbNew.readXML(fb.toXMLString());
        return fbNew;
    }
    
    private void setPosixTime(long time)
    {
        posixTime = time;
        if(posixTime>0)
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
