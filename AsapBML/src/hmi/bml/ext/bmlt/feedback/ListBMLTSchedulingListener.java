package hmi.bml.ext.bmlt.feedback;

import java.util.List;


/**
 * Stores all BMLTPlanningFinishedFeedback and BMLTPlanningStartFeedbacks in the list
 * it is initialized with.
 * @author welberge
 */
public class ListBMLTSchedulingListener implements BMLTSchedulingListener
{
    private final List<BMLTSchedulingFinishedFeedback> finishedSchedulingList;
    private final List<BMLTSchedulingStartFeedback> startedSchedulingList;

    public ListBMLTSchedulingListener(List<BMLTSchedulingStartFeedback> startPlanningList,
            List<BMLTSchedulingFinishedFeedback> finishedPlanningList)
    {
        this.finishedSchedulingList = finishedPlanningList;
        this.startedSchedulingList = startPlanningList;
    }

    @Override
    public void schedulingFinished(BMLTSchedulingFinishedFeedback pff)
    {
        finishedSchedulingList.add(pff);
    }

    @Override
    public void schedulingStart(BMLTSchedulingStartFeedback psf)
    {
        startedSchedulingList.add(psf);
    }

}
