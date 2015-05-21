/*******************************************************************************
 *******************************************************************************/
package asap.picture.loader;

import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;

import asap.picture.lipsync.TimedPictureUnitLipSynchProvider;
import asap.realizer.lipsync.LipSynchProvider;
import asap.realizerembodiments.LipSynchProviderLoader;

/**
 * Loader for a TimedPictureUnitLipSynchProvider. This loader requires an
 * AnimationXML element indicating the filePath and fileName of an animation xml
 * file to be used for display during speech. This very rudimentary lipsync uses
 * no speech binding and does not link different visemes to different
 * pictures/animations. This is due to the absence of viseme information in the
 * Android TTS engine.
 *
 * @author Jordi Hendrix
 */
public class TimedPictureUnitLipSynchProviderLoader implements LipSynchProviderLoader {

    private String id;
    private LipSynchProvider lipSyncProvider;
    private XMLStructureAdapter adapter = new XMLStructureAdapter();

    public void setId(String newId) {
        id = newId;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments, Loader ... requiredLoaders) 
    	throws IOException 
    {
        setId(loaderId);

        PictureEngineLoader pel = null;
        PictureEmbodiment pe = null;
        for (Loader e : requiredLoaders) {
            if (e instanceof PictureEngineLoader) {
                pel = (PictureEngineLoader) e;
            }
            if (e instanceof PictureEmbodiment) {
                pe = (PictureEmbodiment) e;
            }
        }

        if (pel == null) {
            throw tokenizer.getXMLScanException("TimedPictureUnitLipSynchProviderLoader requires pictureengine.");
        }
        if (pe == null) {
            throw tokenizer.getXMLScanException("TimedPictureUnitLipSynchProviderLoader requires pictureembodiment.");
        }

        String xmlFileName = null;
        String xmlFilePath = null;

        // Read AnimationXML element
        while (!tokenizer.atETag("Loader")) {
            if (tokenizer.atSTag("AnimationXML")) {

                HashMap<String, String> attrMap = tokenizer.getAttributes();
                xmlFileName = adapter.getRequiredAttribute("fileName", attrMap, tokenizer);
                xmlFilePath = adapter.getRequiredAttribute("filePath", attrMap, tokenizer);

                tokenizer.takeEmptyElement("AnimationXML");
            }
        }

        if (xmlFileName == null || xmlFilePath == null) {
            throw tokenizer.getXMLScanException("TimedPictureUnitLipSynchProviderLoader requires a valid AnimationXML element.");
        }

        lipSyncProvider = new TimedPictureUnitLipSynchProvider(pel.getPlanManager(), pe.getPictureDisplay(), xmlFilePath, xmlFileName);
    }

    @Override
    public void unload() {
    }

    @Override
    public LipSynchProvider getLipSyncProvider() {
        return lipSyncProvider;
    }
}
