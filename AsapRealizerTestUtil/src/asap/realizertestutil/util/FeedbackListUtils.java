/*******************************************************************************
 *******************************************************************************/
package asap.realizertestutil.util;

import java.util.ArrayList;
import java.util.List;

import saiba.bml.feedback.BMLSyncPointProgressFeedback;
/**
 * Test utilities acting on a list of BMLSyncPointProgressFeedback
 * @author welberge
 */
public final class FeedbackListUtils
{
    private FeedbackListUtils(){}
    
    public static List<String> getSyncs(List<BMLSyncPointProgressFeedback> fbList)
    {
        List<String> syncs = new ArrayList<String>();
        for(BMLSyncPointProgressFeedback fb:fbList)
        {
            syncs.add(fb.getSyncId());
        }
        return syncs;
    }
    
    public static List<String> getIds(List<BMLSyncPointProgressFeedback> fbList)
    {
        List<String> ids = new ArrayList<String>();
        for(BMLSyncPointProgressFeedback fb:fbList)
        {
            ids.add(fb.getCharacterId());
        }
        return ids;
    }
    
    public static List<String> getBmlIds(List<BMLSyncPointProgressFeedback> fbList)
    {
        List<String> bmlIds = new ArrayList<String>();
        for(BMLSyncPointProgressFeedback fb:fbList)
        {
            bmlIds.add(fb.getBMLId());
        }
        return bmlIds;
    }
}
