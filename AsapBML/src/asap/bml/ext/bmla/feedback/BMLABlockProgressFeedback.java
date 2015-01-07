/*******************************************************************************
 *******************************************************************************/
package asap.bml.ext.bmla.feedback;

import hmi.xml.XMLNameSpace;
import hmi.xml.XMLTokenizer;

import java.util.HashMap;
import java.util.List;

import lombok.Getter;
import saiba.bml.feedback.BMLBlockProgressFeedback;
import asap.bml.ext.bmla.BMLAInfo;
import asap.bml.ext.bmla.BMLAPrefix;

/**
 * BMLBlockProgressFeedback with posixTime
 * @author hvanwelbergen
 */
public class BMLABlockProgressFeedback extends BMLBlockProgressFeedback
{
    @Getter
    private long posixTime = 0;   
    
    @Getter
    private BMLABlockStatus status = BMLABlockStatus.NONE;
    
    private static final String POSIXTIME_ID = BMLAInfo.BMLA_NAMESPACE+":"+"posixTime";
    private static final String STATUS_ID = BMLAInfo.BMLA_NAMESPACE+":"+"status";
    
    public BMLABlockProgressFeedback()
    {
        super();
    }
    
    public BMLABlockProgressFeedback(String bmlId, String syncId, double globalTime, BMLABlockStatus status)
    {
        this(bmlId, syncId, globalTime, System.currentTimeMillis(), status);
    }
    
    public BMLABlockProgressFeedback(String bmlId, String syncId, double globalTime, long posixTime, BMLABlockStatus status)
    {
        super(bmlId, syncId, globalTime);
        setPosixTime(posixTime);
        setStatus(status);
    }
    
    public static BMLABlockProgressFeedback build(BMLBlockProgressFeedback fb)
    {
        BMLABlockProgressFeedback fbNew = new BMLABlockProgressFeedback();
        fbNew.readXML(fb.toXMLString());
        return fbNew;
    }
    
    private void setStatus(BMLABlockStatus status)
    {
        this.status = status;
        if(status != BMLABlockStatus.NONE)
        {
            addCustomStringParameterValue(STATUS_ID, status.toString());
        }
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
        if (specifiesCustomStringParameter(STATUS_ID))
        {
            setStatus(BMLABlockStatus.valueOf(getCustomStringParameterValue(STATUS_ID)));
        }
    }
    
    @Override
    public String toBMLFeedbackString(List<XMLNameSpace> xmlNamespaceList)
    {
        return super.toBMLFeedbackString(BMLAPrefix.insertBMLANamespacePrefix(xmlNamespaceList));
    }
}
