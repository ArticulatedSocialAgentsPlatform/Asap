/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.controller;

import hmi.physics.controller.ControllerParameterException;
import hmi.physics.controller.PhysicalController;
import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;

import asap.bml.ext.bmlt.BMLTParameter;

/**
 * Creates a controller from XML. XML writing functionality is not implemented.
 * 
 * @author welberge
 */
public class XMLController extends XMLStructureAdapter
{
    private String id;
    private String className;
    private PhysicalController controller;

    /**
     * @return the id
     */
    public String getId()
    {
        return id;
    }

    /**
     * @return the controller
     */
    public PhysicalController getController()
    {
        return controller;
    }

    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        while (tokenizer.atSTag(BMLTParameter.xmlTag()))
        {
            BMLTParameter nextParam = new BMLTParameter();
            nextParam.readXML(tokenizer);
            try
            {
                controller.setParameterValue(nextParam.name, nextParam.value);
            }
            catch (ControllerParameterException e)
            {
                XMLScanException ex = new XMLScanException();
                ex.initCause(e);
                throw ex;
            }
        }
    }

    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        id = getRequiredAttribute("id", attrMap, tokenizer);
        className = getRequiredAttribute("class", attrMap, tokenizer);
        super.decodeAttributes(attrMap, tokenizer);
        try
        {
            controller = (PhysicalController) Class.forName(className).newInstance();
        }
        catch (Exception e)
        {
            XMLScanException ex = new XMLScanException();
            ex.initCause(e);
            throw ex;
        }
    }

    /*
     * The XML Stag for XML encoding
     */
    private static final String XMLTAG = "Controller";

    /**
     * The XML Stag for XML encoding -- use this static method when you want to see if a given String equals the xml tag for this class
     */
    public static String xmlTag()
    {
        return XMLTAG;
    }

    /**
     * The XML Stag for XML encoding -- use this method to find out the run-time xml tag of an object
     */
    @Override
    public String getXMLTag()
    {
        return XMLTAG;
    }

}
