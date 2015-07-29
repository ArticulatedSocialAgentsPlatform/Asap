package asap.animationengine.loader;

import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.util.ArrayUtils;
import hmi.xml.XMLScanException;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import lombok.Getter;
import asap.animationengine.visualprosody.HeadVisualProsodyProvider;
import asap.realizer.visualprosody.VisualProsodyProvider;
import asap.realizerembodiments.AsapRealizerEmbodiment;
import asap.realizerembodiments.VisualProsodyProviderLoader;
import asap.visualprosody.VisualProsodyLoader;

/**
 * Loader for HeadVisualProsodyProvider
 * @author hvanwelbergen
 *
 */
public class HeadVisualProsodyProviderLoader implements VisualProsodyProviderLoader
{
    @Getter
    private String id;
    private HeadVisualProsodyProvider vpp;
    private VisualProsodyLoader v;

    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
            Loader... requiredLoaders) throws IOException
    {
        MixedAnimationEngineLoader ael = ArrayUtils.getFirstClassOfType(requiredLoaders, MixedAnimationEngineLoader.class);
        if (ael == null)
        {
            throw tokenizer.getXMLScanException("HeadVisualProsodyProviderLoader requires mixedanimationenvironment.");
        }
        AsapRealizerEmbodiment are = ArrayUtils.getFirstClassOfType(requiredLoaders, AsapRealizerEmbodiment.class);
        if (are == null)
        {
            throw new RuntimeException("HeadVisualProsodyProviderLoader requires an EmbodimentLoader containing a AsapRealizerEmbodiment");
        }
        while (!tokenizer.atETag("Loader"))
        {
            readSection(tokenizer);
        }

        if (v == null)
        {
            throw new XMLScanException("HeadVisualProsodyProviderLoader requires a visualprosodyprovider");
        }
        vpp = new HeadVisualProsodyProvider(v.constructProsodyProvider(), ael.getAnimationPlayer(), ael.getPlanManager());
    }

    private void readSection(XMLTokenizer tokenizer) throws IOException
    {
        v = new VisualProsodyLoader();
        if (tokenizer.atSTag("visualprosodyprovider"))
        {
            v.readXML(tokenizer);
        }
    }

    @Override
    public void unload()
    {

    }

    @Override
    public VisualProsodyProvider getVisualProsodyProvider()
    {
        return vpp;
    }

}
