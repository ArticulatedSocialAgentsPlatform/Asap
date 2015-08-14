/*******************************************************************************
 *******************************************************************************/
package asap.bml.bridge.ui;

import hmi.util.Resources;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Locale;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

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

import lombok.Data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import saiba.bml.builder.BehaviourBlockBuilder;
import asap.realizerport.RealizerPort;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

/**
 * A graphical UI to a RealizerPort, allowing for a few simple interactions with it such as
 * sending BML to it.
 * 
 * @author Dennis Reidsma, Herwin van Welbergen
 */
public class RealizerPortUI extends JPanel
{
    @Data
    private static final class DemoScript
    {
        final String name;
        final String filename;

        @Override
        public String toString()
        {
            return name;
        }
    }

    private static final class DemoScriptComparator implements Comparator<DemoScript>
    {
        @Override
        public int compare(DemoScript o1, DemoScript o2)
        {
            return o1.getName().compareTo(o2.getName());
        }
    }

    private static Logger logger = LoggerFactory.getLogger(RealizerPortUI.class.getName());

    private String demoScriptResource = null;

    private String loadPath = "../../Shared/repository/HMI/HmiElckerlyc/resources/";

    /**
     * if there is a bml realizer, a set of demo scripts can be "hard coded" into the UI. If you
     * choose a script from the list, this script is loaded into the bml input box, and parsed /
     * played.
     */
    public ArrayList<DemoScript> demoScripts = null;

    // XXX class is not serializable (see findbugs). Better to make this class HAVE a panel rather
    // than BE a panel
    public static final long serialVersionUID = 1L;

    /** A panel for adding buttons and such */
    protected JPanel buttonPanel = null;

    /** Text area to input BML */
    protected JTextArea bmlInput = null;

    /** combobox for demo scripts */
    protected JComboBox<DemoScript> demoScriptList = null;

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

        demoScripts = new ArrayList<DemoScript>();

        buttonPanel.add(playButton);
        buttonPanel.add(loadButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));

        getDemoScripts(resource);

        // did we obtain demoscripts? if so, load them to GUI
        if (demoScripts.size() > 0)
        {
            Collections.sort(demoScripts, new DemoScriptComparator());
            demoScriptList = new JComboBox<DemoScript>(demoScripts.toArray(new DemoScript[demoScripts.size()]));
            demoScriptList.insertItemAt(new DemoScript("",""), 0);
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

    private void getDemoScripts(String resource)
    {
        URL demoScriptUrl = new Resources("").getURL(resource);
        
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
                            String filename = name.substring(resourceDirName.length());
                            while (filename.startsWith("/"))
                                filename = filename.substring(1); // strip remaining "/" from start
                            String n = filename.replaceAll("-", ": ").replaceAll("_", " ").replaceAll(".xml", "");
                            demoScripts.add(new DemoScript(n, filename));
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
                            String n = f.getName().replaceAll("-", ": ").replaceAll("_", " ").replaceAll(".xml", "");
                            demoScripts.add(new DemoScript(n, f.getName()));
                        }
                    }
                }
                else
                {
                    logger.debug(demoScriptUrl + " not found ");
                }
            }
        }
    }

    /** Play the content of the BML input box; set the resulting output in the outputArea */
    public void playBMLContent()
    {
        realizerBridge.performBML(BehaviourBlockBuilder.resetBlock().toXMLString());
        String bmlContent = bmlInput.getText();
        bmlContent = bmlContent.replaceAll("(?s)<!--.*?-->", "");
        String bmls[] = Iterables.toArray(Splitter.on(Pattern.compile("<bml\\s")).trimResults().omitEmptyStrings().split(bmlContent), String.class);
        for (String bml : bmls)
        {
            realizerBridge.performBML("<bml "+bml);
        }
    }

    public void loadDemoScript(DemoScript script)
    {
        if (script.getName().equals("")) return;
        try
        {
            Resources r = new Resources("");
            if (demoScriptResource != null)
            {
                r = new Resources(demoScriptResource);
            }
            String scriptContent = r.read(script.getFilename());
            bmlInput.setText(scriptContent);
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
            loadDemoScript((DemoScript) demoScriptList.getSelectedItem());
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
                    InputStreamReader r;
                    try
                    {
                        r = new InputStreamReader(new FileInputStream(f), Charsets.UTF_8);
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
