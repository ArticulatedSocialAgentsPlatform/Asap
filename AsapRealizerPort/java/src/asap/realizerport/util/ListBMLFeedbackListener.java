/*******************************************************************************
 *******************************************************************************/
package asap.realizerport.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import saiba.bml.feedback.BMLBlockPredictionFeedback;
import saiba.bml.feedback.BMLBlockProgressFeedback;
import saiba.bml.feedback.BMLFeedback;
import saiba.bml.feedback.BMLFeedbackParser;
import saiba.bml.feedback.BMLPredictionFeedback;
import saiba.bml.feedback.BMLSyncPointProgressFeedback;
import saiba.bml.feedback.BMLWarningFeedback;
import asap.realizerport.BMLFeedbackListener;

/**
 * Stores all BMLFeedbacks in the lists it is initialized with.
 * @author welberge
 */
@Slf4j
public final class ListBMLFeedbackListener implements BMLFeedbackListener
{
    private final List<BMLPredictionFeedback> predictionList;
    private final List<BMLWarningFeedback> warningList;
    private final List<BMLSyncPointProgressFeedback> feedBackList;
    private final List<BMLBlockProgressFeedback> blockFeedbackList;
    private final HashMap<Object, Integer> indexMap = new HashMap<Object, Integer>();
    private int index = 0;

    private ListBMLFeedbackListener(List<BMLWarningFeedback> wList, List<BMLPredictionFeedback> bpList,
            List<BMLSyncPointProgressFeedback> fbList, List<BMLBlockProgressFeedback> blockFeedbackList)
    {
        predictionList = bpList;
        warningList = wList;
        feedBackList = fbList;
        this.blockFeedbackList = blockFeedbackList;
    }

    /**
     * Builder for a ListBMLFeedbackListener
     * @author Herwin
     */
    public final static class Builder
    {
        private List<BMLPredictionFeedback> predictionList = new ArrayList<>();
        private List<BMLWarningFeedback> warningList = new ArrayList<>();
        private List<BMLSyncPointProgressFeedback> feedBackList = new ArrayList<>();
        private List<BMLBlockProgressFeedback> blockFeedbackList = new ArrayList<>();

        public Builder predictionList(List<BMLPredictionFeedback> predictionList)
        {
            this.predictionList = predictionList;
            return this;
        }

        public Builder warningList(List<BMLWarningFeedback> warningList)
        {
            this.warningList = warningList;
            return this;
        }

        public Builder feedBackList(List<BMLSyncPointProgressFeedback> feedBackList)
        {
            this.feedBackList = feedBackList;
            return this;
        }

        public Builder blockFeedbackList(List<BMLBlockProgressFeedback> blockFeedbackList)
        {
            this.blockFeedbackList = blockFeedbackList;
            return this;
        }

        public ListBMLFeedbackListener build()
        {
            return new ListBMLFeedbackListener(warningList, predictionList, feedBackList, blockFeedbackList);
        }
    }

    public void addPrediction(BMLPredictionFeedback bp)
    {
        predictionList.add(bp);
    }

    public void addWarning(BMLWarningFeedback bw)
    {
        warningList.add(bw);
    }

    public List<BMLBlockPredictionFeedback> getBlockStartOnlyPredictions()
    {
        List<BMLBlockPredictionFeedback> bpList = new ArrayList<BMLBlockPredictionFeedback>();
        for (BMLPredictionFeedback bpf : predictionList)
        {
            for (BMLBlockPredictionFeedback bp : bpf.getBmlBlockPredictions())
            {
                if (bp.getGlobalEnd() == BMLBlockPredictionFeedback.UNKNOWN_TIME
                        && bp.getGlobalStart() != BMLBlockPredictionFeedback.UNKNOWN_TIME)
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
        for (BMLPredictionFeedback bpf : predictionList)
        {
            for (BMLBlockPredictionFeedback bp : bpf.getBmlBlockPredictions())
            {
                if (bp.getGlobalEnd() != BMLBlockPredictionFeedback.UNKNOWN_TIME)
                {
                    bpList.add(bp);
                }
            }
        }
        return bpList;
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

    public void addBlockProgress(BMLBlockProgressFeedback psf)
    {
        blockFeedbackList.add(psf);
        indexMap.put(psf, index);
        index++;
    }

    public void addSyncProgress(BMLSyncPointProgressFeedback spp)
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

    @Override
    public void feedback(String feedback)
    {
        BMLFeedback fb;
        try
        {
            fb = BMLFeedbackParser.parseFeedback(feedback);
        }
        catch (IOException e)
        {
            log.warn("Invalid feedback " + feedback, e);
            return;
        }

        if (fb instanceof BMLBlockProgressFeedback)
        {
            this.addBlockProgress((BMLBlockProgressFeedback) fb);
        }
        else if (fb instanceof BMLPredictionFeedback)
        {
            this.addPrediction((BMLPredictionFeedback) fb);
        }
        else if (fb instanceof BMLWarningFeedback)
        {
            this.addWarning((BMLWarningFeedback) fb);
        }
        else if (fb instanceof BMLSyncPointProgressFeedback)
        {
            this.addSyncProgress((BMLSyncPointProgressFeedback) fb);
        }
    }
}
