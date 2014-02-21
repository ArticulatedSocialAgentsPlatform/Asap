package asap.bml.ext.bmla.feedback;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import saiba.bml.feedback.BMLBlockPredictionFeedback;
import saiba.bml.feedback.BMLPredictionFeedback;

/**
 * BMLPredictionFeedback with BMLABlockPredictions
 * @author hvanwelbergen
 */
public class BMLAPredictionFeedback extends BMLPredictionFeedback
{
    public static BMLAPredictionFeedback build(BMLPredictionFeedback bpf)
    {
        BMLAPredictionFeedback fb = new BMLAPredictionFeedback();        
        fb.readXML(bpf.toXMLString());
        return fb;
    }
    /**
     * Get a copy of the BMLBlockPredictionFeedbacks, wrapped into BMLABlockPredictionFeedback
     */
    public ImmutableList<BMLABlockPredictionFeedback> getBMLABlockPredictions()
    {
        List<BMLABlockPredictionFeedback> fbList = new ArrayList<BMLABlockPredictionFeedback>();
        for(BMLBlockPredictionFeedback fb:getBmlBlockPredictions())
        {
            fbList.add(BMLABlockPredictionFeedback.build(fb));
        }
        return ImmutableList.copyOf(fbList);
    }
}
