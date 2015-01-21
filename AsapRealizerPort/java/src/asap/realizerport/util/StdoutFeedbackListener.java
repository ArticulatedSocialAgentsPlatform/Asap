/*******************************************************************************
 *******************************************************************************/
package asap.realizerport.util;

import asap.realizerport.BMLFeedbackListener;


/**
 * Sends BML feedback to the stdout
 * @author welberge
 */
public class StdoutFeedbackListener implements BMLFeedbackListener
{
    @Override
    public void feedback(String feedback)
    {
        System.out.println(feedback);        
    }
}
