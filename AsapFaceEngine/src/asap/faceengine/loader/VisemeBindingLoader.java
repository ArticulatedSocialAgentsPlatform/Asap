package asap.faceengine.loader;

import hmi.faceanimation.converters.FACSConverter;
import hmi.util.Resources;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;

import asap.faceengine.viseme.FACSVisemeBinding;
import asap.faceengine.viseme.MorphVisemeBinding;
import asap.faceengine.viseme.VisemeBinding;
import asap.faceengine.viseme.VisemeToFACSMapping;
import asap.faceengine.viseme.VisemeToMorphMapping;

/**
 * Utility class to load the VisemeBinding
 * @author hvanwelbergen
 */
final class VisemeBindingLoader
{
    private VisemeBindingLoader()
    {
    }

    static VisemeBinding load(XMLTokenizer tokenizer, FACSConverter fc) throws IOException
    {
        VisemeBinding visBinding = null;
        XMLStructureAdapter adapter = new XMLStructureAdapter();

        if (tokenizer.atSTag("MorphVisemeBinding"))
        {
            HashMap<String, String> attrMap = tokenizer.getAttributes();
            VisemeToMorphMapping mapping = new VisemeToMorphMapping();
            mapping.readXML(new Resources(adapter.getOptionalAttribute("resources", attrMap, "")).getReader(adapter.getRequiredAttribute(
                    "filename", attrMap, tokenizer)));
            visBinding = new MorphVisemeBinding(mapping);
            tokenizer.takeEmptyElement("MorphVisemeBinding");
        }
        else if (tokenizer.atSTag("FACSVisemeBinding"))
        {
            HashMap<String, String> attrMap = tokenizer.getAttributes();
            VisemeToFACSMapping mapping = new VisemeToFACSMapping();
            mapping.readXML(new Resources(adapter.getOptionalAttribute("resources", attrMap, "")).getReader(adapter.getRequiredAttribute(
                    "filename", attrMap, tokenizer)));
            visBinding = new FACSVisemeBinding(mapping, fc);
            tokenizer.takeEmptyElement("FACSVisemeBinding");
        }
        return visBinding;
    }
}
