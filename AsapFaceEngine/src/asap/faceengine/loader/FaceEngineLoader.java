/*******************************************************************************
 * 
 * Copyright (C) 2009 Human Media Interaction, University of Twente, the Netherlands
 * 
 * This file is part of the Elckerlyc BML realizer.
 * 
 * Elckerlyc is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Elckerlyc is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Elckerlyc.  If not, see http://www.gnu.org/licenses/.
 ******************************************************************************/
package asap.faceengine.loader;

import hmi.faceanimation.FaceController;
import hmi.faceanimation.FaceEmbodiment;
import hmi.faceanimation.converters.EmotionConverter;
import hmi.faceanimation.converters.FACSConverter;
import hmi.faceanimation.converters.ui.EmotionConverterFrame;
import hmi.faceanimation.converters.ui.FACSConverterFrame;
import hmi.faceanimation.converters.ui.MPEG4ControllerFrame;
import hmi.util.Resources;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;

import javax.swing.SwingUtilities;

import asap.environment.AsapVirtualHuman;
import asap.environment.EmbodimentLoader;
import asap.environment.EngineLoader;
import asap.environment.Loader;
import asap.environment.impl.JComponentEmbodiment;
import asap.faceengine.FacePlanner;
import asap.faceengine.facebinding.FaceBinding;
import asap.faceengine.faceunit.TimedFaceUnit;
import asap.realizer.DefaultEngine;
import asap.realizer.DefaultPlayer;
import asap.realizer.Engine;
import asap.realizer.Player;
import asap.realizer.planunit.PlanManager;
import asap.realizer.planunit.SingleThreadedPlanPlayer;
import asap.utils.Environment;

/**

*/
public class FaceEngineLoader implements EngineLoader
{
    private XMLStructureAdapter adapter = new XMLStructureAdapter();
    private FaceEmbodiment m4e = null;
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
    private AsapVirtualHuman theVirtualHuman = null;

    @Override
    public void readXML(XMLTokenizer tokenizer, String newId, AsapVirtualHuman avh, Environment[] environments, Loader... requiredLoaders)
            throws IOException
    {
        id = newId;
        theVirtualHuman = avh;
        for (Loader e : requiredLoaders)
        {
            if (e instanceof EmbodimentLoader && ((EmbodimentLoader) e).getEmbodiment() 
                    instanceof FaceEmbodiment) m4e = (FaceEmbodiment) ((EmbodimentLoader) e)
                    .getEmbodiment();
            if (e instanceof EmbodimentLoader && ((EmbodimentLoader) e).getEmbodiment() 
                    instanceof JComponentEmbodiment) jce = (JComponentEmbodiment) ((EmbodimentLoader) e)
                    .getEmbodiment();
        }
        if (m4e == null)
        {
            throw new RuntimeException("FaceEngineLoader requires an EmbodimentLoader containing a FaceEmbodiment");
        }
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
            try
            {
                facebinding.readXML(new Resources(adapter.getOptionalAttribute("resources", attrMap, "")).getReader(adapter
                        .getRequiredAttribute("filename", attrMap, tokenizer)));
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw new RuntimeException("Cannnot load FaceBinding: " + e);
            }
            tokenizer.takeEmptyElement("FaceBinding");
        }
        if (tokenizer.atSTag("FACSConverterData"))
        {
            attrMap = tokenizer.getAttributes();
            fconv = new FACSConverter(new Resources(adapter.getOptionalAttribute("resources", attrMap, "")),adapter.getRequiredAttribute("filename", attrMap, tokenizer));
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
        facePlayer = new DefaultPlayer(new SingleThreadedPlanPlayer<TimedFaceUnit>(theVirtualHuman.getElckerlycRealizer()
                .getFeedbackManager(), planManager));
        econv = new EmotionConverter();
        if (fconv==null)fconv = new FACSConverter();
        FaceController fc = null;
        if (m4e != null)
        {
            fc = m4e.getFaceController();
        }
        FacePlanner facePlanner = new FacePlanner(theVirtualHuman.getElckerlycRealizer().getFeedbackManager(), fc, fconv, econv,
                facebinding, planManager);
        engine = new DefaultEngine<TimedFaceUnit>(facePlanner, facePlayer, planManager);
        engine.setId(id);

        // add engine to realizer;
        theVirtualHuman.getElckerlycRealizer().addEngine(engine);

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
                    new FACSConverterFrame(fconv, m4e.getFaceController());
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
                    new EmotionConverterFrame(econv, m4e.getFaceController());
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
                    new MPEG4ControllerFrame(m4e.getFaceController());
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
        if (m4e != null)
        {
            return m4e.getFaceController();
        }
        return null;
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
