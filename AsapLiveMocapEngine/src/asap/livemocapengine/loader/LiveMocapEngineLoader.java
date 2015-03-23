/*******************************************************************************
 *******************************************************************************/
package asap.livemocapengine.loader;

import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.environmentbase.SensorLoader;
import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;

import lombok.Getter;
import asap.livemocapengine.LiveMocapPlanner;
import asap.livemocapengine.binding.NameTypeBinding;
import asap.livemocapengine.planunit.LiveMocapTMU;
import asap.realizer.DefaultEngine;
import asap.realizer.DefaultPlayer;
import asap.realizer.Engine;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.planunit.PlanManager;
import asap.realizer.planunit.SingleThreadedPlanPlayer;
import asap.realizerembodiments.AsapRealizerEmbodiment;
import asap.realizerembodiments.EngineLoader;

/**
 * Loader for the LiveMocapEngine, connects this engine to input and output loaders.
 * @author welberge
 * 
 */
public class LiveMocapEngineLoader implements EngineLoader
{
    private String id = "";
    private NameTypeBinding inputBinding = new NameTypeBinding();
    private NameTypeBinding outputBinding = new NameTypeBinding();
    private Engine engine;

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
            Loader... requiredLoaders) throws IOException
    {
        id = loaderId;
        AsapRealizerEmbodiment are = null;
        for (Loader e : requiredLoaders)
        {
            if (e instanceof EmbodimentLoader && ((EmbodimentLoader) e).getEmbodiment() instanceof AsapRealizerEmbodiment) are = (AsapRealizerEmbodiment) ((EmbodimentLoader) e)
                    .getEmbodiment();
        }
        while (!tokenizer.atETag("Loader"))
        {
            readSection(tokenizer, requiredLoaders);
        }
        FeedbackManager fbm = are.getFeedbackManager();
        PlanManager<LiveMocapTMU> planManager = new PlanManager<LiveMocapTMU>();
        LiveMocapPlanner planner = new LiveMocapPlanner(fbm, planManager, inputBinding, outputBinding);
        DefaultPlayer player = new DefaultPlayer(new SingleThreadedPlanPlayer<LiveMocapTMU>(fbm, planManager));
        engine = new DefaultEngine<LiveMocapTMU>(planner, player, planManager);

        // add engine to realizer;
        are.addEngine(engine);
    }

    private static class Input extends XMLStructureAdapter
    {
        @Getter
        private String name;
        @Getter
        private String interfaceStr;

        @Override
        public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
        {
            name = getRequiredAttribute("name", attrMap, tokenizer);
            interfaceStr = getRequiredAttribute("interface", attrMap, tokenizer);
            super.decodeAttributes(attrMap, tokenizer);
        }

        private static final String XMLTAG = "input";

        public static String xmlTag()
        {
            return XMLTAG;
        }

        @Override
        public String getXMLTag()
        {
            return XMLTAG;
        }
    }

    private static class Output extends XMLStructureAdapter
    {
        private static final String XMLTAG = "output";
        @Getter
        private String name;
        @Getter
        private String interfaceStr;

        public static String xmlTag()
        {
            return XMLTAG;
        }

        @Override
        public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
        {
            name = getRequiredAttribute("name", attrMap, tokenizer);
            interfaceStr = getRequiredAttribute("interface", attrMap, tokenizer);
            super.decodeAttributes(attrMap, tokenizer);
        }

        @Override
        public String getXMLTag()
        {
            return XMLTAG;
        }
    }

    private Loader findLoader(String id, Loader... requiredLoaders)
    {
        for (Loader l : requiredLoaders)
        {
            if (l.getId().equals(id)) return l;
        }
        return null;
    }

    private void checkInterface(String interfaceStr, Object obj)
    {
        Class<?> interfaceClass;
        try
        {
            interfaceClass = Class.forName(interfaceStr);
        }
        catch (ClassNotFoundException e)
        {
            throw new XMLScanException("Interface " + interfaceStr + " not found.", e);
        }
        if (!interfaceClass.isInstance(obj))
        {
            throw new XMLScanException("Object " + obj + " is not an instance of " + interfaceStr);
        }
    }

    protected void readSection(XMLTokenizer tokenizer, Loader... requiredLoaders) throws IOException
    {
        if (tokenizer.atSTag(Input.xmlTag()))
        {
            Input input = new Input();
            input.readXML(tokenizer);
            Loader l = findLoader(input.getName(), requiredLoaders);
            if (l == null)
            {
                throw new XMLScanException("Cannot find input sensor with name: " + input.getName());
            }
            SensorLoader loader;
            if (l instanceof SensorLoader)
            {
                loader = (SensorLoader) l;
            }
            else
            {
                throw new XMLScanException("input " + input + " is not coupled to an SensorLoader");
            }
            checkInterface(input.getInterfaceStr(), loader.getSensor());
            inputBinding.put(input.getName(), input.getInterfaceStr(), loader.getSensor());
        }
        else if (tokenizer.atSTag(Output.xmlTag()))
        {
            Output output = new Output();
            output.readXML(tokenizer);
            Loader l = findLoader(output.getName(), requiredLoaders);
            if (l == null)
            {
                throw new XMLScanException("Cannot find output embodimentloader with name: " + output.getName());
            }

            EmbodimentLoader loader;
            if (l instanceof EmbodimentLoader)
            {
                loader = (EmbodimentLoader) l;
            }
            else
            {
                throw new XMLScanException("output " + output + " is not coupled to an EmbodimentLoader");
            }
            checkInterface(output.getInterfaceStr(), loader.getEmbodiment());
            outputBinding.put(output.getName(), output.getInterfaceStr(), loader.getEmbodiment());
        }
        else
        {
            throw new XMLScanException("Invalid content " + tokenizer.currentTokenString() + " in LiveMocapEngineLoader xml.");
        }
    }

    @Override
    public void unload()
    {

    }

    @Override
    public Engine getEngine()
    {
        return engine;
    }
}
