/*******************************************************************************
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
package asap.bml.bridge.ui;

import hmi.util.Resources;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.Document;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asap.bml.bridge.RealizerPort;

/**
 * A graphical UI to a RealizerPort, allowing for a few simple interactions with it such as
 * sending BML to it.
 * 
 * @author Dennis Reidsma
 */
public class RealizerPortUI extends JPanel
{

    private static Logger logger = LoggerFactory.getLogger(RealizerPortUI.class.getName());

    private URL demoScriptUrl = null;
    private String demoScriptResource = null;

    private String loadPath = "../../Shared/repository/HMI/HmiElckerlyc/resources/";

    /**
     * if there is a bml realizer, a set of demo scripts can be "hard coded" into the UI. If you
     * choose a script from the list, this script is loaded into the bml input box, and parsed /
     * played.
     */
    public ArrayList<String> demoScripts = null;

    /** names for the demo scripts, to be used in the drop down list */
    public ArrayList<String> demoScriptNames = null;

    // XXX class is not serializable (see findbugs). Better to make this class HAVE a panel rather
    // than BE a panel
    public static final long serialVersionUID = 1L;

    /** A panel for adding buttons and such */
    protected JPanel buttonPanel = null;

    /** Text area to input BML */
    protected JTextArea bmlInput = null;

    /** combobox for demo scripts */
    protected JComboBox demoScriptList = null;

    /** The realizerbridge */
    protected RealizerPort realizerBridge = null;

    /**
     * Init the frame with input text are -- use default set of demo
     * scripts
     */
    public RealizerPortUI(RealizerPort bridge)
    {
        this(bridge, null);
    }

    /** Init the frame with input text area, play button */
    public RealizerPortUI(RealizerPort bridge, String resource)
    {
        super();

        demoScriptResource = resource;
        demoScriptUrl = new Resources("").getURL(resource);

        realizerBridge = bridge;

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setAlignmentX(LEFT_ALIGNMENT);

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        JButton playButton = new JButton("PLAY");
        playButton.addActionListener(new PlayListener());
        JButton loadButton = new JButton("LOAD");
        loadButton.addActionListener(new LoadListener());

        demoScripts = new ArrayList<String>();
        demoScriptNames = new ArrayList<String>();

        buttonPanel.add(playButton);
        buttonPanel.add(loadButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));

        // find the demoscripts, based on the URL
        if (demoScriptUrl != null)
        {
            // is it a jar or not?
            if (demoScriptUrl.getProtocol().toLowerCase(Locale.US).equals("jar"))
            { // it is in a jar. open jar, enumerate entries and add them.
                try
                {
                    logger.debug("Loading demo scripts from JAR");
                    JarURLConnection connection = (JarURLConnection) demoScriptUrl.openConnection();
                    JarFile jarFile = connection.getJarFile();
                    String resourceDirName = connection.getEntryName();
                    Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements())
                    {
                        JarEntry entry = entries.nextElement();
                        String name = entry.getName();
                        if (name.endsWith(".xml") && name.startsWith(resourceDirName))
                        {
                            String shortName = name.substring(resourceDirName.length());
                            while (shortName.startsWith("/"))
                                shortName = shortName.substring(1); // strip remaining "/" from start
                            demoScripts.add(shortName);
                            demoScriptNames.add(shortName.replaceAll("-", ": ").replaceAll("_", " ").replaceAll(".xml", ""));
                        }
                    }
                }
                catch (Exception ex)
                {
                    logger.debug("Error reading demo scripts from jar", ex);
                }
            }
            else
            { // a file, not from jar. try to list directory contents
                logger.debug("Loading demo scripts from file");
                File demoScriptDir = null;
                try
                {
                    demoScriptDir = new File(demoScriptUrl.toURI());
                    if (!demoScriptDir.isDirectory())
                    {
                        logger.debug("Demo script directory is not a directory");
                        demoScriptDir = null;
                    }
                }
                catch (Exception ex)
                {
                    logger.debug("Cannot open demo script directory", ex);
                }
                if (demoScriptDir != null)
                {
                    logger.debug(demoScriptUrl + " found ");
                    for (File f : demoScriptDir.listFiles())
                    {
                        if (f.isFile() && f.getName().endsWith(".xml"))
                        {
                            demoScripts.add(f.getName());
                            demoScriptNames.add(f.getName().replaceAll("-", ": ").replaceAll("_", " ").replaceAll(".xml", ""));
                        }
                    }
                }
                else
                {
                    logger.debug(demoScriptUrl + " not found ");
                }
            }
        }

        // did we obtain demoscripts? if so, load them to GUI
        if (demoScripts.size() > 0)
        {
            demoScriptList = new JComboBox(demoScriptNames.toArray(new String[demoScriptNames.size()]));
            demoScriptList.insertItemAt("", 0);
            demoScriptList.setEditable(false);
            demoScriptList.setSelectedItem("");
            demoScriptList.addActionListener(new DemoScriptSelectionListener());
            buttonPanel.add(new JLabel("Demo scripts:"));
            buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
            buttonPanel.add(demoScriptList);
        }

        buttonPanel.setAlignmentX(LEFT_ALIGNMENT);

        // textbox for input
        bmlInput = new JTextArea("<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\"" + " id=\"bml1\">\n"
                + "<speech id=\"speech1\" start=\"2\">\n" + "<text>Hello! This is a basic BML test for the realizer bridge!</text>\n"
                + "</speech>\n" + "</bml>");
        final UndoManager undo = new UndoManager();
        Document doc = bmlInput.getDocument(); // Listen for undo and redo events
        doc.addUndoableEditListener(new UndoableEditListener()
        {
            public void undoableEditHappened(UndoableEditEvent evt)
            {
                undo.addEdit(evt.getEdit());
            }
        }); // Create an undo action and add it to the text component
        bmlInput.getActionMap().put("Undo", new AbstractAction("Undo")
        {
            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent evt)
            {
                try
                {
                    if (undo.canUndo())
                    {
                        undo.undo();
                    }
                }
                catch (CannotUndoException e)
                {

                }
            }
        }); // Bind the undo action to ctl-Z
        bmlInput.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo"); // Create a redo
                                                                                 // action and add
                                                                                 // it to the text
                                                                                 // component
        bmlInput.getActionMap().put("Redo", new AbstractAction("Redo")
        {
            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent evt)
            {
                try
                {
                    if (undo.canRedo())
                    {
                        undo.redo();
                    }
                }
                catch (CannotRedoException e)
                {

                }
            }
        }); // Bind the redo action to ctl-Y
        bmlInput.getInputMap().put(KeyStroke.getKeyStroke("control Y"), "Redo");

        JScrollPane bmlScroll = new JScrollPane(bmlInput);
        bmlScroll.setPreferredSize(new Dimension(500, 80));

        add(buttonPanel);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(bmlScroll);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    }

    /** Play the content of the BML input box; set the resulting output in the outputArea */
    public void playBMLContent()
    {
        realizerBridge.performBML("<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" "
                + "id=\"clear\" composition=\"REPLACE\"></bml>");
        realizerBridge.performBML(bmlInput.getText());
    }

    public void loadDemoScript(String scriptName)
    {
        if (scriptName.equals("")) return;
        try
        {
            String fileName = demoScripts.get(demoScriptNames.indexOf(scriptName));
            Resources r = new Resources("");
            if (demoScriptResource != null)
            {
                r = new Resources(demoScriptResource);
            }
            String script = r.read(fileName);
            bmlInput.setText(script);
            playBMLContent();
        }
        catch (IOException ex)
        {
            JOptionPane.showMessageDialog(null, "Error loading demo script.", "alert", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }

    }

    /*
     * ============================================================================= A FEW HELPER
     * METHODS FOR SETTING UP THE UI
     * =============================================================================
     */

    class DemoScriptSelectionListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            loadDemoScript((String) demoScriptList.getSelectedItem());
        }
    }

    /** play content of text panel */
    class PlayListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            playBMLContent();
        }
    }

    /** load file content into text panel */
    class LoadListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            JFileChooser chooser = new JFileChooser(loadPath);
            chooser.setFileFilter(new FileFilter()
            {
                @Override
                public boolean accept(File f)
                {
                    return f.isDirectory() || f.getName().endsWith(".xml");
                }

                @Override
                public String getDescription()
                {
                    return "BML scripts (.xml)";
                }
            });
            int returnVal = chooser.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION)
            {
                File f = chooser.getSelectedFile();
                if (f != null)
                {
                    loadPath = f.getParent();
                    FileReader r;
                    try
                    {
                        r = new FileReader(f);
                    }
                    catch (FileNotFoundException e1)
                    {
                        JOptionPane.showMessageDialog(null, "File not found; see stack trace for more info.", "alert",
                                JOptionPane.ERROR_MESSAGE);
                        e1.printStackTrace();
                        return;
                    }

                    try
                    {
                        bmlInput.read(r, chooser.getSelectedFile().getName());
                    }
                    catch (IOException ex)
                    {
                        JOptionPane.showMessageDialog(null, "Error reading file; see stack trace for more info.", "alert",
                                JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                    try
                    {
                        r.close();
                    }
                    catch (IOException e1)
                    {
                        JOptionPane.showMessageDialog(null, "Error closing file; see stack trace for more info.", "alert",
                                JOptionPane.ERROR_MESSAGE);
                        e1.printStackTrace();
                    }
                }
            }
        }
    }
}
