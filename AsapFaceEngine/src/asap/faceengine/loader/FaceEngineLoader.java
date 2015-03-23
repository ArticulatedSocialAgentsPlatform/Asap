/*******************************************************************************
 *******************************************************************************/
package asap.faceengine.loader;

import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.faceanimation.FaceController;
import hmi.faceanimation.FaceControllerPose;
import hmi.faceanimation.converters.EmotionConverter;
import hmi.faceanimation.converters.FACSConverter;
import hmi.faceanimationui.converters.EmotionConverterFrame;
import hmi.faceanimationui.converters.FACSConverterFrame;
import hmi.faceanimationui.converters.MPEG4ControllerFrame;
import hmi.faceembodiments.FaceEmbodiment;
import hmi.jcomponentenvironment.JComponentEmbodiment;
import hmi.util.Resources;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;

import javax.swing.SwingUtilities;

import asap.faceengine.FaceAnimationPlanPlayer;
import asap.faceengine.FacePlanner;
import asap.faceengine.facebinding.FaceBinding;
import asap.faceengine.faceunit.TimedFaceUnit;
import asap.realizer.DefaultEngine;
import asap.realizer.DefaultPlayer;
import asap.realizer.Engine;
import asap.realizer.Player;
import asap.realizer.planunit.PlanManager;
import asap.realizerembodiments.AsapRealizerEmbodiment;
import asap.realizerembodiments.EngineLoader;

/**

*/
public class FaceEngineLoader implements EngineLoader
{
    private XMLStructureAdapter adapter = new XMLStructureAdapter();    
    private JComponentEmbodiment jce = null;

    private boolean initUI = false;

    private Engine engine = null;
    private Player facePlayer = null;
    private EmotionConverter econv;
    private FACSConverter fconv = null;
    private PlanManager<TimedFaceUnit> planManager = null;
    private String id = "";
    // some variables cached during loading
    private FaceBinding facebinding = null;

    private AsapRealizerEmbodiment are = null;
    private FaceControllerPose fcp;
    
    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
            Loader... requiredLoaders) throws IOException
    {
        id = loaderId;
        FaceEmbodiment m4e = null;
        for (Loader e : requiredLoaders)
        {
            if (e instanceof EmbodimentLoader && ((EmbodimentLoader) e).getEmbodiment() 
                    instanceof FaceEmbodiment) m4e = (FaceEmbodiment) ((EmbodimentLoader) e)
                    .getEmbodiment();
            if (e instanceof EmbodimentLoader && ((EmbodimentLoader) e).getEmbodiment() 
                    instanceof JComponentEmbodiment) jce = (JComponentEmbodiment) ((EmbodimentLoader) e)
                    .getEmbodiment();
            if (e instanceof EmbodimentLoader && ((EmbodimentLoader) e).getEmbodiment() 
                    instanceof AsapRealizerEmbodiment) are = (AsapRealizerEmbodiment) ((EmbodimentLoader) e).getEmbodiment();
        }
        if (m4e == null)
        {
            throw new RuntimeException("FaceEngineLoader requires an EmbodimentLoader containing a FaceEmbodiment");
        }
        if (are == null)
        {
            throw new RuntimeException("FaceEngineLoader requires an EmbodimentLoader containing a AsapRealizerEmbodiment");
        }
        fcp = new FaceControllerPose(m4e.getFaceController());
        while (!tokenizer.atETag("Loader"))
        {
            readSection(tokenizer);
        }
        constructEngine(tokenizer);
    }

    @Override
    public void unload()
    {
        // engine.shutdown();already done in scheduler...
    }

    protected void readSection(XMLTokenizer tokenizer) throws IOException
    {
        HashMap<String, String> attrMap = null;
        if (tokenizer.atSTag("FaceBinding"))
        {
            attrMap = tokenizer.getAttributes();
            facebinding = new FaceBinding();
            
            String resourcePath = adapter.getOptionalAttribute("resources", attrMap, "");
            String fileName = adapter.getRequiredAttribute("filename", attrMap, tokenizer);
            facebinding.readXML(new Resources(resourcePath).getReader(fileName));
            tokenizer.takeEmptyElement("FaceBinding");
        }
        else if (tokenizer.atSTag("FACSConverterData"))
        {
            attrMap = tokenizer.getAttributes();
            fconv = new FACSConverter(new Resources(adapter.getOptionalAttribute("resources", attrMap, "")),
                    adapter.getRequiredAttribute("filename", attrMap, tokenizer));
            tokenizer.takeEmptyElement("FACSConverterData");
        }
        else if (tokenizer.atSTag("FaceUI"))
        {
            if (jce == null) throw tokenizer.getXMLScanException("Cannot add FaceUI when no JComponentEmbodiment is set");
            initUI = true;
            tokenizer.takeSTag("FaceUI");
            tokenizer.takeETag("FaceUI");
        }
        else
        {
            throw tokenizer.getXMLScanException("Unknown tag in Loader content");
        }
    }

    /** tokenizer used for throwing scanexceptions */
    private void constructEngine(XMLTokenizer tokenizer)
    {
       
        if (facebinding == null) throw tokenizer.getXMLScanException("facebinding is null, cannot build faceplanner ");
        planManager = new PlanManager<TimedFaceUnit>();
        facePlayer = new DefaultPlayer(new FaceAnimationPlanPlayer(are.getFeedbackManager(),planManager,fcp));
        econv = new EmotionConverter();
        if (fconv==null)fconv = new FACSConverter();
        FacePlanner facePlanner = new FacePlanner(are.getFeedbackManager(), fcp, fconv, econv,
                facebinding, planManager, are.getPegBoard());
        engine = new DefaultEngine<TimedFaceUnit>(facePlanner, facePlayer, planManager);
        engine.setId(id);

        // add engine to realizer;
        are.addEngine(engine);

        // init ui?
        if (initUI)
        {
            final javax.swing.JPanel faceUIPanel = new javax.swing.JPanel();
            javax.swing.JButton showFACSConverter = new javax.swing.JButton();
            showFACSConverter.setText("FACS Converter");
            showFACSConverter.addActionListener(new java.awt.event.ActionListener()
            {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    new FACSConverterFrame(fconv, fcp);
                }
            });
            faceUIPanel.add(showFACSConverter);

            javax.swing.JButton showEmotionConverter = new javax.swing.JButton();
            showEmotionConverter.setText("Emotion Converter");
            showEmotionConverter.addActionListener(new java.awt.event.ActionListener()
            {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    new EmotionConverterFrame(econv, fcp);
                }
            });
            faceUIPanel.add(showEmotionConverter);

            javax.swing.JButton showDirectMPEG4Control = new javax.swing.JButton();
            showDirectMPEG4Control.setText("Direct MPEG4 Control");
            showDirectMPEG4Control.addActionListener(new java.awt.event.ActionListener()
            {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    new MPEG4ControllerFrame(fcp);
                }
            });
            faceUIPanel.add(showDirectMPEG4Control);

            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    jce.addJComponent(faceUIPanel);
                }
            });

        }
    }

    /** Return the Engine that was constructed from the XML specification */
    public Engine getEngine()
    {
        return engine;
    }

    public Player getFacePlayer()
    {
        return facePlayer;
    }

    public FaceController getFaceController()
    {
        return fcp;        
    }

    public FACSConverter getFACSConverter()
    {
        return fconv;
    }

    public PlanManager<TimedFaceUnit> getPlanManager()
    {
        return planManager;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String newId)
    {
        id = newId;
    }
}
