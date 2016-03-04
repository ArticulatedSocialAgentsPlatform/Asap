/*******************************************************************************
 *******************************************************************************/
package asap.emitterengine.planunit;

import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.KeyPositionManager;
import asap.realizer.planunit.ParameterException;
import asap.realizer.planunit.TimedPlanUnitPlayException;


/**
 * 
 * @author Dennis Reidsma
 */
public interface EmitterUnit extends KeyPositionManager
{
    void setFloatParameterValue(String name, float value)throws ParameterException;
    void setParameterValue(String name, String value)throws ParameterException;
    String getParameterValue(String name)throws ParameterException;
    float getFloatParameterValue(String name)throws ParameterException;
    
    boolean hasValidParameters();
   
    /** start the unit. this may involve things such as creating the actual emitter */
    void startUnit(double time) throws TimedPlanUnitPlayException;
        
    /**
     * Executes the emitter unit
     * @param t execution time, 0 &lt; t &lt; 1
     * @throws EUPlayException if the play fails for some reason
     */
    void play(double t)throws EUPlayException;
    
    /** Clean up the unit -- e.g., stop and clean up emitter */    
    void cleanup();

    /**
     * Creates the TimedEmitterUnit corresponding to this emitter unit
     * @param bmlId     BML block id
     * @param id         behaviour id
     * @return          the TEU
     */
    TimedEmitterUnit createTEU(FeedbackManager bfm, BMLBlockPeg bbPeg,String bmlId,String id);
    
    /**
     * @return Prefered duration (in seconds) of this nabaztag unit, 0 means not determined/infinite 
     */
    double getPreferedDuration();
}