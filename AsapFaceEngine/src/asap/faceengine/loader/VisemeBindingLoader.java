/*******************************************************************************
 *******************************************************************************/
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
public final class VisemeBindingLoader
{
    private VisemeBindingLoader()
    {
    }

    public static boolean isAtVisemeBindingTag(XMLTokenizer tokenizer) throws IOException
    {
        return tokenizer.atSTag("MorphVisemeBinding")||tokenizer.atSTag("FACSVisemeBinding");
    }
    
    public static VisemeBinding load(XMLTokenizer tokenizer, FACSConverter fc) throws IOException
    {
        VisemeBinding visBinding = null;

        if (tokenizer.atSTag("MorphVisemeBinding"))
        {
            visBinding = loadMorphVisemeBinding(tokenizer);
        }
        else if (tokenizer.atSTag("FACSVisemeBinding"))
        {
            visBinding = loadFACSVisemeBinding(tokenizer, fc);
        }
        return visBinding;
    }

    private static FACSVisemeBinding loadFACSVisemeBinding(XMLTokenizer tokenizer, FACSConverter fc) throws IOException
    {
        FACSVisemeBinding visBinding;
        XMLStructureAdapter adapter = new XMLStructureAdapter();
        HashMap<String, String> attrMap = tokenizer.getAttributes();
        VisemeToFACSMapping mapping = new VisemeToFACSMapping();
        mapping.readXML(new Resources(adapter.getOptionalAttribute("resources", attrMap, "")).getReader(adapter.getRequiredAttribute(
                "filename", attrMap, tokenizer)));
        visBinding = new FACSVisemeBinding(mapping, fc);
        tokenizer.takeEmptyElement("FACSVisemeBinding");
        return visBinding;
    }

    public static MorphVisemeBinding loadMorphVisemeBinding(XMLTokenizer tokenizer) throws IOException
    {
        MorphVisemeBinding visBinding;
        XMLStructureAdapter adapter = new XMLStructureAdapter();
        HashMap<String, String> attrMap = tokenizer.getAttributes();
        VisemeToMorphMapping mapping = new VisemeToMorphMapping();
        mapping.readXML(new Resources(adapter.getOptionalAttribute("resources", attrMap, "")).getReader(adapter.getRequiredAttribute(
                "filename", attrMap, tokenizer)));
        visBinding = new MorphVisemeBinding(mapping);
        tokenizer.takeEmptyElement("MorphVisemeBinding");
        return visBinding;
    }
}
