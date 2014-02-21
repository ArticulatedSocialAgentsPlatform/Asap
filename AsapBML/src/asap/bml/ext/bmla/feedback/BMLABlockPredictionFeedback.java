package asap.bml.ext.bmla.feedback;

import hmi.xml.XMLTokenizer;

import java.util.HashMap;

import lombok.Getter;
import saiba.bml.feedback.BMLBlockPredictionFeedback;
import asap.bml.ext.bmla.BMLAInfo;

/**
 * BMLBlockPredictionFeedback extension to provide posix timestamps
 * @author hvanwelbergen
 * 
 */
public class BMLABlockPredictionFeedback extends BMLBlockPredictionFeedback
{
    private static final String POSIXSTARTTIME_ID = BMLAInfo.BMLA_NAMESPACE + ":" + "posixStartTime";
    private static final String POSIXENDTIME_ID = BMLAInfo.BMLA_NAMESPACE + ":" + "posixEndTime";

    @Getter
    private long posixStartTime;

    @Getter
    private long posixEndTime;

    public BMLABlockPredictionFeedback()
    {
        super();
    }

    public static BMLABlockPredictionFeedback build(BMLBlockPredictionFeedback fb)
    {
        BMLABlockPredictionFeedback fbNew = new BMLABlockPredictionFeedback();
        fbNew.readXML(fb.toXMLString());
        return fbNew;
    }

    public BMLABlockPredictionFeedback(String id, double globalStart, double globalEnd, long posixStart, long posixEnd)
    {
        super(id, globalStart, globalEnd);
        setPosixStartTime(posixStart);
        setPosixEndTime(posixEnd);
    }

    private void setPosixStartTime(long time)
    {
        posixStartTime = time;
        if (posixStartTime > 0)
        {
            addCustomStringParameterValue(POSIXSTARTTIME_ID, ""+posixStartTime);
        }
    }

    private void setPosixEndTime(long time)
    {
        posixEndTime = time;
        if (posixStartTime > 0)
        {
            addCustomStringParameterValue(POSIXENDTIME_ID, ""+posixEndTime);
        }
    }

    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        super.decodeAttributes(attrMap, tokenizer);
        if (specifiesCustomStringParameter(POSIXSTARTTIME_ID))
        {
            setPosixStartTime(Long.parseLong(getCustomStringParameterValue(POSIXSTARTTIME_ID)));
        }
        if (specifiesCustomStringParameter(POSIXENDTIME_ID))
        {
            setPosixEndTime(Long.parseLong(getCustomStringParameterValue(POSIXENDTIME_ID)));
        }
    }
}
