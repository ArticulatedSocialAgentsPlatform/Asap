/*******************************************************************************
 *******************************************************************************/
package asap.ipaacaiuadapters;

/**
 * Constants for categories and payload keys
 * @author Herwin
 *
 */
public final class IpaacaBMLConstants
{
    private IpaacaBMLConstants(){}
    
    public static final String REALIZER_REQUEST_CATEGORY = "realizerRequest";
    public static final String REALIZER_FEEDBACK_CATEGORY = "realizerFeedback";
    public static final String REALIZER_REQUEST_KEY = "request";
    public static final String REALIZER_REQUEST_TYPE_KEY = "type"; // TODO: request type unused atm
    public static final String REALIZER_REQUEST_TYPE_BML = "bml";  //
    public static final String REALIZER_REQUEST_TYPE_BMLFILE = "bmlfile";  //
    public static final String BML_FEEDBACK_KEY = "bmlfeedback";
    public static final String BML_ID_KEY = "bmlid";
    public static final String IU_STATUS_KEY = "status";
    public static final String LAST_SYNC_ID_KEY = "lastSyncId";
    public static final String IU_ERROR_KEY = "error";
    public static final String IU_PREDICTED_END_TIME_KEY = "predictedEndTime";
    public static final String IU_PREDICTED_START_TIME_KEY = "predictedStartTime";
}
