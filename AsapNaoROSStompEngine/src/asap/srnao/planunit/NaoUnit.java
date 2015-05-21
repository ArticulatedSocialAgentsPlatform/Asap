/*******************************************************************************
 *******************************************************************************/
package asap.srnao.planunit;

import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.KeyPositionManager;
import asap.realizer.planunit.ParameterException;
import asap.srnao.display.PictureDisplay;

/**
 * Contains a set of keys that map to 'world' time to animation time.
 * @author Daniel
 */
public interface NaoUnit extends KeyPositionManager
{
    void setFloatParameterValue(String name, float value)throws ParameterException;
    void setParameterValue(String name, String value)throws ParameterException;
    String getParameterValue(String name)throws ParameterException;
    float getFloatParameterValue(String name)throws ParameterException;
    
    boolean hasValidParameters();    
    
    void prepareImages() throws NUPrepareException;
       
    /** start the unit.*/
    void startUnit(double time) throws NUPlayException;
        
    /**
     * Executes the nao unit
     * @param t execution time, 0 &lt t &lt 1
     * @throws NUPlayException if the play fails for some reason
     */
    void play(double t)throws NUPlayException;
    
    /** Clean up the unit - i.e. remove traces of this naounit */    
    void cleanup();

    /**
     * Creates the TimedNaoUnit corresponding to this nao unit
     * @param bmlId     BML block id
     * @param id         behavior id
     * @return          the TNU
     */
    TimedNaoUnit createTNU(FeedbackManager bfm, BMLBlockPeg bbPeg,String bmlId,String id);
    
    
    /**
     * @return Preferred duration (in seconds) of this nao unit, 0 means not determined/infinite 
     */
    double getPreferedDuration();
    
    /**
     * Create a copy of this nao unit
     */
    NaoUnit copy(PictureDisplay display);  
}