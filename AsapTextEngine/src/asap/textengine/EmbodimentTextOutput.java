/*******************************************************************************
 *******************************************************************************/
package asap.textengine;

import hmi.textembodiments.TextEmbodiment;
import asap.realizer.planunit.ParameterException;
import asap.realizer.planunit.ParameterNotFoundException;
/**
 * TextOutput implementation that sends the text directly to a TextEmbodiment.
 * Does not support parameter setting/getting.
 * @author reidsma
 *
 */
public class EmbodimentTextOutput implements TextOutput
{
    TextEmbodiment textEmbodiment = null;

    public EmbodimentTextOutput(TextEmbodiment te)
    {
        textEmbodiment = te;
    }

    @Override
    public void setText(String text)
    {
        textEmbodiment.setText(text);
    }

    @Override
    public void setFloatParameterValue(String parameter, float value) throws ParameterException
    {
        throw new ParameterNotFoundException(parameter);
    }

    @Override
    public void setParameterValue(String parameter, String value) throws ParameterException
    {
        throw new ParameterNotFoundException(parameter);

    }

    @Override
    public float getFloatParameterValue(String parameter) throws ParameterException
    {
        throw new ParameterNotFoundException(parameter);
    }

    @Override
    public String getParameterValue(String parameter) throws ParameterException
    {
        throw new ParameterNotFoundException(parameter);
    }

}
