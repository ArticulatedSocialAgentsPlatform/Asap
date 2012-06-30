package asap.bml.feedback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import saiba.bml.feedback.BMLBlockProgressFeedback;
import saiba.bml.feedback.BMLSyncPointProgressFeedback;

/**
 * Stores all BML feedbacks in the lists this is initialized with.
 * 
 * @author welberge
 */
public class ListFeedbackListener implements BMLFeedbackListener
{
    private final List<BMLSyncPointProgressFeedback> feedBackList;
    private final List<BMLBlockProgressFeedback> blockFeedbackList;
    private final HashMap<Object, Integer> indexMap = new HashMap<Object, Integer>();
    private int index = 0;

    public ListFeedbackListener(List<BMLSyncPointProgressFeedback> fbList)
    {
        feedBackList = fbList;
        blockFeedbackList = new ArrayList<BMLBlockProgressFeedback>();
    }

    public BMLBlockProgressFeedback getBlockProgress(String bmlId, String syncId)
    {
        for (BMLBlockProgressFeedback bp : blockFeedbackList)
        {
            if (bp.getBmlId().equals(bmlId) && bp.getSyncId().equals(syncId))
            {
                return bp;
            }
        }
        return null;
    }

    public ListFeedbackListener(List<BMLSyncPointProgressFeedback> fbList, List<BMLBlockProgressFeedback> blockFeedbackList)
    {
        feedBackList = fbList;
        this.blockFeedbackList = blockFeedbackList;
    }

    @Override
    public void blockProgress(BMLBlockProgressFeedback psf)
    {
        blockFeedbackList.add(psf);
        indexMap.put(psf, index);
        index++;
    }

    @Override
    public void syncProgress(BMLSyncPointProgressFeedback spp)
    {
        feedBackList.add(spp);
        indexMap.put(spp, index);
        index++;
    }

    public int getIndex(Object feedback)
    {
        return indexMap.get(feedback);
    }

    public List<BMLSyncPointProgressFeedback> getFeedback(String bmlId, String behaviorId)
    {
        List<BMLSyncPointProgressFeedback> syncs = new ArrayList<BMLSyncPointProgressFeedback>();
        for (BMLSyncPointProgressFeedback fb : feedBackList)
        {

            if (fb.getBMLId().equals(bmlId) && fb.getBehaviourId().equals(behaviorId))
            {
                syncs.add(fb);
            }
        }
        return syncs;
    }

    public List<String> getFeedbackSyncIds(String bmlId, String behaviorId)
    {
        List<String> syncs = new ArrayList<String>();

        for (BMLSyncPointProgressFeedback fb : feedBackList)
        {
            if (fb.getBMLId().equals(bmlId) && fb.getBehaviourId().equals(behaviorId))
            {
                syncs.add(fb.getSyncId());
            }
        }
        return syncs;
    }
}
