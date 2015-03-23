/*******************************************************************************
 *******************************************************************************/
package asap.textengine;

import asap.realizer.planunit.ParameterException;


/**
 * Used to display the text of a TextSpeechUnit 
 * @author welberge
 */
public interface TextOutput
{
    /**
     * Replace current text by text
     */
    void setText(String text);
    
    void setFloatParameterValue(String parameter, float value)throws ParameterException;
    void setParameterValue(String parameter, String value)throws ParameterException;
    float getFloatParameterValue(String parameter)throws ParameterException;
    String getParameterValue(String parameter)throws ParameterException;
}
