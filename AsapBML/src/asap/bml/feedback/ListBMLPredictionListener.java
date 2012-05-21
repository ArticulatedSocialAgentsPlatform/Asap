package asap.bml.feedback;

import java.util.ArrayList;
import java.util.List;

import saiba.bml.feedback.BMLBlockPredictionFeedback;
import saiba.bml.feedback.BMLPredictionFeedback;


/**
 * Stores all BMLPredictionFeedbacks in the lists it is initalized with.
 * @author welberge
 */
public class ListBMLPredictionListener implements BMLPredictionListener
{
    private final List<BMLPredictionFeedback> predictionList;
    
    public ListBMLPredictionListener(List<BMLPredictionFeedback>bpList)
    {
        predictionList = bpList;
    }
    
    @Override
    public void prediction(BMLPredictionFeedback bp)
    {
        predictionList.add(bp);
    }
    
    public List<BMLBlockPredictionFeedback> getBlockStartOnlyPredictions()
    {
        List<BMLBlockPredictionFeedback> bpList = new ArrayList<BMLBlockPredictionFeedback>();
        for(BMLPredictionFeedback bpf:predictionList)
        {
            for(BMLBlockPredictionFeedback bp:bpf.getBmlBlockPredictions())
            {
                if(bp.getGlobalEnd()==BMLBlockPredictionFeedback.UNKNOWN_TIME && bp.getGlobalStart()!=BMLBlockPredictionFeedback.UNKNOWN_TIME)
                {
                    bpList.add(bp);
                }
            }
        }
        return bpList;
    }
    
    public List<BMLBlockPredictionFeedback> getBlockEndPredictions()
    {
        List<BMLBlockPredictionFeedback> bpList = new ArrayList<BMLBlockPredictionFeedback>();
        for(BMLPredictionFeedback bpf:predictionList)
        {
            for(BMLBlockPredictionFeedback bp:bpf.getBmlBlockPredictions())
            {
                if(bp.getGlobalEnd()!=BMLBlockPredictionFeedback.UNKNOWN_TIME)
                {
                    bpList.add(bp);
                }
            }
        }
        return bpList;
    }
}
