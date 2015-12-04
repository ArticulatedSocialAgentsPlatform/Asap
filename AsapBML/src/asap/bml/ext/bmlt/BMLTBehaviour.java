/*******************************************************************************
 *******************************************************************************/
package asap.bml.ext.bmlt;

import hmi.xml.XMLFormatting;
import hmi.xml.XMLScanException;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;

import saiba.bml.core.Behaviour;

/**
 * Abstract class for all BMLT specific Behaviours
 * @author welberge
 */
public abstract class BMLTBehaviour extends Behaviour
{
    public BMLTBehaviour(String bmlId)
    {
        super(bmlId);
    }

    public BMLTBehaviour(String bmlId, String id)
    {
        super(bmlId, id);
    }
    
    public static final String BMLTNAMESPACE = "http://hmi.ewi.utwente.nl/bmlt";

    @Override
    public String getNamespace()
    {
        return BMLTNAMESPACE;
    }

    protected HashMap<String, BMLTParameter> parameters = new HashMap<String, BMLTParameter>();

    @Override
    public String getStringParameterValue(String name)
    {
        if (parameters.get(name) != null)
        {
            return parameters.get(name).value;
        }
        return super.getStringParameterValue(name);
    }

    @Override
    public boolean specifiesParameter(String name)
    {
        if (parameters.get(name) != null) return true;
        return super.specifiesParameter(name);
    }

    @Override
    public float getFloatParameterValue(String name)
    {
        if (parameters.get(name) != null)
        {
            return Float.parseFloat(parameters.get(name).value);
        }
        return super.getFloatParameterValue(name);
    }

    @Override
    public boolean satisfiesConstraint(String name, String value)
    {
        BMLTParameter p = parameters.get(name);
        if (p != null)
        {
            return p.value.equals(value);
        }
        return super.satisfiesConstraint(name, value);
    }

    @Override
    public boolean hasContent()
    {
        if (parameters.size() > 0) return true;
        return super.hasContent();
    }

    @Override
    public StringBuilder appendContent(StringBuilder buf, XMLFormatting fmt)
    {
        for (BMLTParameter p : parameters.values())
        {
            p.appendXML(buf, fmt);
        }
        return buf;
    }

    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        while (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            if (tag.equals(BMLTParameter.xmlTag()))
            {
                decodeBMLTParameter(tokenizer);
            }
            else
            {
                throw new XMLScanException("Invalid content " + tag + " in BMLTBehavior " + id);
            }
        }
    }

    protected void decodeBMLTParameter(XMLTokenizer tokenizer) throws IOException
    {
        BMLTParameter param = new BMLTParameter();
        param.readXML(tokenizer);
        parameters.put(param.name, param);
    }
}
