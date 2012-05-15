package asap.faceengine.loader;

import java.io.IOException;
import java.util.HashMap;

import asap.faceengine.lipsync.TimedFaceUnitLipSynchProvider;
import asap.faceengine.viseme.MorphVisemeBinding;
import asap.faceengine.viseme.VisemeBinding;
import asap.faceengine.viseme.VisemeToMorphMapping;
import hmi.util.Resources;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;
import asap.environment.AsapVirtualHuman;
import asap.environment.LipSynchProviderLoader;
import asap.environment.Loader;
import asap.realizer.lipsync.LipSynchProvider;
import asap.utils.Environment;

/**
 * Loads a TimedFaceUnitLipSynchProvider
 * @author hvanwelbergen
 */
public class TimedFaceUnitLipSynchProviderLoader implements LipSynchProviderLoader
{
    private String id;
    private LipSynchProvider lipSyncProvider;
    private XMLStructureAdapter adapter = new XMLStructureAdapter();

    public void setId(String newId)
    {
        id = newId;
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public void readXML(XMLTokenizer tokenizer, String newId, AsapVirtualHuman avh, Environment[] environments, Loader... requiredLoaders)
            throws IOException
    {
        setId(newId);

        FaceEngineLoader fal = null;
        for (Loader e : requiredLoaders)
        {
            if (e instanceof FaceEngineLoader) fal = (FaceEngineLoader) e;
        }
        if (fal == null)
        {
            throw tokenizer.getXMLScanException("TimedFaceUnitLipSynchProviderLoader requires mixedanimationenvironment.");
        }
        VisemeBinding visBinding = null;
        while (!tokenizer.atETag("Loader"))
        {
            if (tokenizer.atSTag("MorphVisemeBinding"))
            {
                HashMap<String, String> attrMap = tokenizer.getAttributes();
                VisemeToMorphMapping mapping = new VisemeToMorphMapping();
                mapping.readXML(new Resources(adapter.getOptionalAttribute("resources", attrMap, "")).getReader(adapter.getRequiredAttribute(
                        "filename", attrMap, tokenizer)));
                visBinding = new MorphVisemeBinding(mapping);
                tokenizer.takeEmptyElement("MorphVisemeBinding");
            }
        }
        

        if (visBinding == null)
        {
            throw tokenizer.getXMLScanException("TimedFaceUnitLipSynchProvider requires a visimebinding.");
        }
        
        lipSyncProvider = new TimedFaceUnitLipSynchProvider(visBinding, fal.getFaceController(), fal.getPlanManager());
    }

    @Override
    public void unload()
    {

    }

    @Override
    public LipSynchProvider getLipSyncProvider()
    {
        return lipSyncProvider;
    }
}
