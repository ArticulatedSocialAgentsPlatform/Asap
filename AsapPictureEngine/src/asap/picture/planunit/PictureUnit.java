/*******************************************************************************
 *******************************************************************************/
package asap.picture.planunit;

import asap.picture.display.PictureDisplay;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.KeyPositionManager;
import asap.realizer.planunit.ParameterException;

/**
 * Contains a set of keys that map to 'world' time to animation time.
 * @author Dennis Reidsma
 */
public interface PictureUnit extends KeyPositionManager
{
    void setFloatParameterValue(String name, float value)throws ParameterException;
    void setParameterValue(String name, String value)throws ParameterException;
    String getParameterValue(String name)throws ParameterException;
    float getFloatParameterValue(String name)throws ParameterException;
    
    boolean hasValidParameters();    
    
    void prepareImages() throws PUPrepareException;
       
    /** start the unit.*/
    void startUnit(double time) throws PUPlayException;
        
    /**
     * Executes the picture unit
     * @param t execution time, 0 &lt t &lt 1
     * @throws PUPlayException if the play fails for some reason
     */
    void play(double t)throws PUPlayException;
    
    /** Clean up the unit - i.e. remove traces of this pictureunit */    
    void cleanup();

    /**
     * Creates the TimedPictureUnit corresponding to this picture unit
     * @param bmlId     BML block id
     * @param id         behavior id
     * @return          the TPU
     */
    TimedPictureUnit createTPU(FeedbackManager bfm, BMLBlockPeg bbPeg,String bmlId,String id);
    
    
    /**
     * @return Preferred duration (in seconds) of this picture unit, 0 means not determined/infinite 
     */
    double getPreferedDuration();
    
    /**
     * Create a copy of this picture unit
     */
    PictureUnit copy(PictureDisplay display);  
}