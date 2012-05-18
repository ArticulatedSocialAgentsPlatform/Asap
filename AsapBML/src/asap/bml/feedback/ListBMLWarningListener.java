package asap.bml.feedback;

import java.util.List;

import saiba.bml.feedback.BMLWarningFeedback;


/**
 * Stores all BMLWarningFeedbacks in the lists it is initalized with.
 * @author welberge
 */
public class ListBMLWarningListener implements BMLWarningListener
{
    private final List<BMLWarningFeedback> warningList;
    
    public ListBMLWarningListener(List<BMLWarningFeedback>bwList)
    {
        warningList = bwList;
    }
    
    @Override
    public void warn(BMLWarningFeedback bw)
    {
        warningList.add(bw);
    }
}
