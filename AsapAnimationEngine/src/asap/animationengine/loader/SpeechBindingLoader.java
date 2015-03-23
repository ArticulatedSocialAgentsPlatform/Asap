/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.loader;

import hmi.util.Resources;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;

import asap.animationengine.gesturebinding.SpeechBinding;

/**
 * Utility class to load a speechbinding
 * @author hvanwelbergen
 *
 */
final class SpeechBindingLoader
{
    private SpeechBindingLoader(){}
    
    static SpeechBinding load(XMLTokenizer tokenizer) throws IOException
    {
        XMLStructureAdapter adapter = new XMLStructureAdapter();
        SpeechBinding sb = null;
        if (tokenizer.atSTag("SpeechBinding"))
        {

            HashMap<String, String> attrMap = tokenizer.getAttributes();
            sb = new SpeechBinding(new Resources(adapter.getOptionalAttribute("basedir", attrMap, "")));
            try
            {
                sb.readXML(new Resources(adapter.getOptionalAttribute("resources", attrMap, "")).getReader(adapter
                        .getRequiredAttribute("filename", attrMap, tokenizer)));
            }
            catch (Exception e)
            {
                throw new RuntimeException("Cannnot load SpeechBinding: " + e);
            }
            tokenizer.takeEmptyElement("SpeechBinding");
        }
        return sb;
    }
}
