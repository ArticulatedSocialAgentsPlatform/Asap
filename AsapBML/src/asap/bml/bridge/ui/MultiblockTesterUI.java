/*******************************************************************************
 *******************************************************************************/
package asap.bml.bridge.ui;

import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asap.realizerport.RealizerPort;

import com.google.common.base.Charsets;

/**
 * A graphical UI to a RealizerBridge, allowing one to prepare a set of BML blocks with scheduling
 * instructions and then fire them off to the realizer.
 * 
 * @author Dennis Reidsma
 */
public class MultiblockTesterUI extends JFrame
{
    private FeedbackPanel feedbackPane;

    private Logger logger = LoggerFactory.getLogger(MultiblockTesterUI.class.getName());

    protected String loadPath = "../../Shared/repository/HMI/HmiElckerlyc/resources/enterface/bml "
            + "Enterface BvS/Fire_and_Multiblocktesters experiment2/";

    protected String specFileName = "file.xml";

    // XXX class is not serializable (see findbugs). Better to make this class HAVE a panel rather
    // than BE a panel
    public static final long serialVersionUID = 1L;

    /** main content panel */
    protected JPanel contentPanel = new JPanel();

    /** A panel for adding buttons and such */
    protected JPanel buttonPanel = null;

    /** The realizerbridge */
    protected RealizerPort realizerBridge = null;

    protected ArrayList<ArrayList<String>> scripts = new ArrayList<ArrayList<String>>();

    protected ArrayList<Boolean> hasFired = new ArrayList<Boolean>();

    protected ArrayList<JButton> fireButtons = new ArrayList<JButton>();

    /** Init the frame with buttons and feedback text area */
    public MultiblockTesterUI(RealizerPort bridge)
    {
        super("BML multiblock testing control");
        realizerBridge = bridge;

        feedbackPane = new FeedbackPanel(bridge);

        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.LINE_AXIS));
        contentPanel.setAlignmentX(LEFT_ALIGNMENT);

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        JButton load = new JButton("Load spec");
        load.addActionListener(new LoadListener());
        buttonPanel.add(load);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JButton restart = new JButton("Restart scenario");
        restart.addActionListener(new RestartListener());
        buttonPanel.add(restart);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        buttonPanel.setAlignmentX(LEFT_ALIGNMENT);

        contentPanel.add(buttonPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        contentPanel.add(feedbackPane);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        getContentPane().add(contentPanel);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        pack();
        setVisible(true);
    }

    public void addBlockPanel(String newName, ArrayList<String> newScripts)
    {
        JPanel blockPanel = new JPanel();
        blockPanel.setLayout(new BoxLayout(blockPanel, BoxLayout.LINE_AXIS));
        blockPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        JButton fireButton = new JButton(newName);
        fireButtons.add(fireButton);
        fireButton.addActionListener(new FireListener(fireButtons.size() - 1));
        blockPanel.add(fireButton);
        blockPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        fireButton.setEnabled(false);

        scripts.add(newScripts);

        hasFired.add(Boolean.valueOf(true));

        blockPanel.setAlignmentX(LEFT_ALIGNMENT);

        buttonPanel.add(blockPanel);
    }

    public void changeBlockPanel(int index, String newName, ArrayList<String> newScripts)
    {
        fireButtons.get(index).setText(newName);
        fireButtons.get(index).setEnabled(false);
        scripts.get(index).clear();
        scripts.get(index).addAll(newScripts);
        hasFired.set(index, Boolean.valueOf(true));
    }

    public void clearSpecs()
    {
        for (int i = 0; i < fireButtons.size(); i++)
        {
            fireButtons.get(i).setText("<none>");
            fireButtons.get(i).setEnabled(false);
            scripts.get(i).clear();
            hasFired.set(i, Boolean.valueOf(true));
        }
    }

    public void loadSpecs()
    {
        try
        {
            XMLTokenizer tok = new XMLTokenizer(new InputStreamReader(new FileInputStream(specFileName), Charsets.UTF_8));
            tok.takeSTag("elckerlycmultiblocktester");
            int i = 0;
            while (tok.atSTag("scriptlist"))
            {
                HashMap<String, String> attrMap = tok.getAttributes();
                String newName = new XMLStructureAdapter().getRequiredAttribute("name", attrMap, tok);
                tok.takeSTag("scriptlist");
                ArrayList<String> newScripts = new ArrayList<String>();
                while (tok.atSTag("script"))
                {
                    HashMap<String, String> attrMap2 = tok.getAttributes();
                    String filename = new XMLStructureAdapter().getRequiredAttribute("filename", attrMap2, tok);

                    StringBuffer contents = new StringBuffer();
                    BufferedReader reader = null;
                    reader = new BufferedReader(new InputStreamReader(new FileInputStream(loadPath + "/" + filename), Charsets.UTF_8));
                    String text = null;
                    while ((text = reader.readLine()) != null)
                    {
                        contents.append(text).append(System.getProperty("line.separator"));
                    }
                    reader.close();
                    newScripts.add(contents.toString());

                    tok.takeSTag("script");
                    tok.takeETag("script");
                }
                if (i >= fireButtons.size())
                {
                    addBlockPanel(newName, newScripts);
                }
                else
                {
                    changeBlockPanel(i, newName, newScripts);
                }
                tok.takeETag("scriptlist");
                i++;
            }
            tok.takeETag("elckerlycmultiblocktester");
        }
        catch (IOException ex)
        {
            logger.warn("Cannot load specs in multiblockdemo: " + specFileName);
            logger.debug("Error: ", ex);
            JOptionPane.showMessageDialog(null, "Cannot load multiblock tester specification from file \"" + specFileName
                    + "\". See logging output for more information.", "alert", JOptionPane.ERROR_MESSAGE);
            clearSpecs();
        }
    }

    /*
     * ============================================================================= A FEW HELPER
     * METHODS FOR SETTING UP THE UI
     * =============================================================================
     */

    class FireListener implements ActionListener
    {
        int index = -1;

        public FireListener(int i)
        {
            index = i;
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            hasFired.set(index, true);
            fireButtons.get(index).setEnabled(false);
            for (int i = 0; i < scripts.get(index).size(); i++)
            {
                logger.debug("New script to send...");
                realizerBridge.performBML(scripts.get(index).get(i));
                logger.debug("Sent script:");
                logger.debug(scripts.get(index).get(i));
            }

        }
    }

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
                    return "Multiblock UI specs (.xml)";
                }
            });
            int returnVal = chooser.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION)
            {
                File f = chooser.getSelectedFile();
                if (f != null)
                {
                    loadPath = f.getParent();
                    specFileName = f.getAbsolutePath();
                    clearSpecs();
                    loadSpecs();
                }
            }
        }
    }

    /** Clear BML Realizer, re-load specs, prepare scripts */
    class RestartListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            clearSpecs();
            loadSpecs();
            realizerBridge.performBML("<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" "
                    + "id=\"clear\" composition=\"REPLACE\"></bml>");
            for (int i = 0; i < fireButtons.size(); i++)
            {
                fireButtons.get(i).setEnabled(true);
                hasFired.set(i, Boolean.valueOf(false));
            }
        }
    }
}
