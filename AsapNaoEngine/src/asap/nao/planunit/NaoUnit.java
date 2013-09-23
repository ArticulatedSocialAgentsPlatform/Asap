/*******************************************************************************
 * Copyright (C) 2009 Human Media Interaction, University of Twente, the Netherlands
 * 
 * This file is part of the Elckerlyc BML realizer.
 * 
 * Elckerlyc is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Elckerlyc is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Elckerlyc.  If not, see http://www.gnu.org/licenses/.
 ******************************************************************************/
package asap.nao.planunit;

import asap.nao.Nao;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.KeyPositionManager;
import asap.realizer.planunit.ParameterException;
import asap.realizer.planunit.TimedPlanUnitPlayException;

/**
 * An abstract unit desinged for the Nao Robot (real or virtual).
 * Contains a set of keys that map to 'world' time to animation time.
 * @author Robin ten Buuren
 */
public interface NaoUnit extends KeyPositionManager
{
    void setFloatParameterValue(String name, float value)throws ParameterException;
    void setParameterValue(String name, String value)throws ParameterException;
    String getParameterValue(String name)throws ParameterException;
    float getFloatParameterValue(String name)throws ParameterException;
    
    boolean hasValidParameters();    
   
    /** start the unit. this may involve things like querying and storing the current pose of the ears */
    void startUnit(double time) throws TimedPlanUnitPlayException;
        
    /**
     * Executes the nao unit, typically by moving the ears or setting the LEDs
     * @param t execution time, 0 &lt t &lt 1
     * @throws NUPlayException if the play fails for some reason
     */
    void play(double t)throws NUPlayException;
    
    /** Clean up the rabbit - i.e. remove traces of this naounit */    
    void cleanup();

    /**
     * Creates the TimedNao corresponding to this nao unit
     * @param bmlId     BML block id
     * @param id         behaviour id
     * @return          the TNU
     */
    TimedNaoUnit createTNU(FeedbackManager bfm, BMLBlockPeg bbPeg,String bmlId,String id);
    
    
    /**
     * @return Prefered duration (in seconds) of this nao unit, 0 means not determined/infinite 
     */
    double getPreferedDuration();
    
    /**
     * Create a copy of this nao unit and link it to the NAO
     */
    NaoUnit copy(Nao n);
}