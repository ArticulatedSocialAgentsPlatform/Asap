/*******************************************************************************
 *******************************************************************************/
package asap.textengine;

import hmi.util.StringUtil;

import java.awt.Font;

import javax.swing.JLabel;

import asap.realizer.planunit.InvalidParameterException;
import asap.realizer.planunit.ParameterException;
import asap.realizer.planunit.ParameterNotFoundException;

/**
 * TextOutput that prints the text to a JLabel. The volume parameter is linked to font size.
 * @author welberge
 *
 */
public class JLabelTextOutput implements TextOutput
{
    private JLabel label;
    private static final float FONT_PER_VOLUME_PERCENT = 0.25f;

    public JLabelTextOutput(JLabel l)
    {
        label = l;
    }

    @Override
    public void setText(String text)
    {
        label.setText(text);
        Font curFont = label.getFont();
        label.setFont(new Font(curFont.getFontName(), curFont.getStyle(), (int) (50 * FONT_PER_VOLUME_PERCENT)));
        // System.out.println("text: "+text);
    }

    @Override
    public void setFloatParameterValue(String parameter, float value) throws ParameterException
    {
        if (parameter.equals("volume"))
        {
            Font curFont = label.getFont();
            label.setFont(new Font(curFont.getFontName(), curFont.getStyle(), (int) (FONT_PER_VOLUME_PERCENT * value)));
        }
        else
        {
            throw new ParameterNotFoundException(parameter);
        }
    }

    @Override
    public void setParameterValue(String parameter, String value) throws ParameterException
    {
        if (StringUtil.isNumeric(value))
        {
            setFloatParameterValue(parameter, Float.parseFloat(value));
        }
        else
        {
            throw new InvalidParameterException(parameter,value);
        }
    }

    @Override
    public float getFloatParameterValue(String parameter) throws ParameterException
    {
        if (parameter.equals("volume"))
        {
            Font curFont = label.getFont();
            return curFont.getSize() / FONT_PER_VOLUME_PERCENT;
        }
        else
        {
            throw new ParameterNotFoundException(parameter);
        }
    }

    @Override
    public String getParameterValue(String parameter) throws ParameterException
    {
        return "" + getFloatParameterValue(parameter);
    }

}
