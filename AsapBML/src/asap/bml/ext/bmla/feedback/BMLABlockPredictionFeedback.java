package asap.bml.ext.bmla.feedback;

import hmi.xml.XMLTokenizer;

import java.util.HashMap;

import asap.bml.ext.bmla.BMLAInfo;
import lombok.Getter;
import saiba.bml.feedback.BMLBlockPredictionFeedback;

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
    private double posixStartTime;

    @Getter
    private double posixEndTime;

    public BMLABlockPredictionFeedback()
    {
        super();
    }

    public BMLABlockPredictionFeedback(String id, double globalStart, double globalEnd, double posixStart, double posixEnd)
    {
        super(id, globalStart, globalEnd);
        setPosixStartTime(posixStart);
        setPosixEndTime(posixEnd);
    }
    
    private void setPosixStartTime(double time)
    {
        posixStartTime = time;
        if (posixStartTime > 0)
        {
            addCustomFloatParameterValue(POSIXSTARTTIME_ID, (float) posixStartTime);
        }
    }

    private void setPosixEndTime(double time)
    {
        posixEndTime = time;
        if (posixStartTime > 0)
        {
            addCustomFloatParameterValue(POSIXENDTIME_ID, (float) posixEndTime);
        }
    }

    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        super.decodeAttributes(attrMap, tokenizer);
        if (specifiesCustomFloatParameter(POSIXSTARTTIME_ID))
        {
            setPosixStartTime(getCustomFloatParameterValue(POSIXSTARTTIME_ID));
        }
        if (specifiesCustomFloatParameter(POSIXENDTIME_ID))
        {
            setPosixEndTime(getCustomFloatParameterValue(POSIXENDTIME_ID));
        }
    }
}
