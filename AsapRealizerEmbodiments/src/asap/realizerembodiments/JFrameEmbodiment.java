/*******************************************************************************
 *******************************************************************************/
package asap.realizerembodiments;

import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.jcomponentenvironment.JComponentEmbodiment;
import hmi.util.ArrayUtils;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.awt.Toolkit;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asap.bml.bridge.ui.FeedbackPanel;
import asap.bml.bridge.ui.RealizerPortUI;

/** This "embodiment" allows a VH to create Swing GUI components. */
public class JFrameEmbodiment implements EmbodimentLoader
{
    private Logger logger = LoggerFactory.getLogger(JFrameEmbodiment.class.getName());

    private XMLStructureAdapter adapter = new XMLStructureAdapter();

    private AsapRealizerEmbodiment are = null;

    /** The button to kill the virtualhuman */
    @SuppressWarnings("unused")
    private JButton killVH;

    private JFrame theUI = null;
    private JPanel contentPanel;
    
    private JComponentEmbodiment jcEmbodiment = new JComponentEmbodiment();

    public JFrameEmbodiment()
    {
        try
        {
            SwingUtilities.invokeAndWait(new Runnable()
            {

                @Override
                public void run()
                {
                    contentPanel = new JPanel();
                    jcEmbodiment.setMasterComponent(contentPanel);
                }
            });
        }
        catch (InterruptedException e)
        {
            logger.warn("Exception constructing contentPanel", e);
            Thread.interrupted();
        }
        catch (InvocationTargetException e)
        {
            logger.warn("Exception constructing contentPanel", e);
        }

    }

    private String loaderId = "";

    public void setId(String id)
    {
        this.loaderId = id;
    }

    @Override
    public String getId()
    {
        return loaderId;
    }

    public void initialize(final String finalName)
    {
        try
        {
            SwingUtilities.invokeAndWait(new Runnable()
            {

                @Override
                public void run()
                {
                    // make UI frame
                    theUI = new JFrame(finalName);
                    theUI.setLocation(650, 50);
                    theUI.setSize(800, 600);
                    theUI.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                    theUI.setVisible(true);
                    contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.PAGE_AXIS));
                    contentPanel.setAlignmentX(JFrame.LEFT_ALIGNMENT);
                    contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                    theUI.getContentPane().add(contentPanel);
                }
            });
        }
        catch (InterruptedException e)
        {
            logger.warn("Exception in JFrameEmbodiment initialization", e);
            Thread.interrupted();
        }
        catch (InvocationTargetException e)
        {
            logger.warn("Exception in JFrameEmbodiment initialization", e);
        }
    }

    /** No loading necessary, actually! Empty content expected. No required embodiments */
    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
            Loader... requiredLoaders) throws IOException
    {
        setId(loaderId);
        final String finalName = vhName;

        are = ArrayUtils.getFirstClassOfType(requiredLoaders, AsapRealizerEmbodiment.class);

        if (are == null)
        {
            throw new RuntimeException("JFrameEmbodiment requires an AsapRealizerEmbodiment when loading");
        }

        final XMLTokenizer tok = tokenizer;
        try
        {
            initialize(finalName);
            SwingUtilities.invokeAndWait(new Runnable()
            {
                @Override
                public void run()
                {
                    // if specified by XML, create serverUI and/or BML test interface and/or feedbackpanel based on the elckerlycrealizer
                    try
                    {
                        while (!tok.atETag("Loader"))
                        {
                            readSection(tok);
                        }
                    }
                    catch (IOException e)
                    {
                        logger.warn("IOException reading section", e);
                    }
                }
            });
        }
        catch (InterruptedException e)
        {
            logger.warn("Exception JFrameEmbodiment from XML", e);
            Thread.interrupted();
        }
        catch (InvocationTargetException e)
        {
            logger.warn("Exception JFrameEmbodiment from XML", e);
        }

    }

    protected void readSection(XMLTokenizer tokenizer) throws IOException
    {
        HashMap<String, String> attrMap = null;
        /*
         * if (tokenizer.atSTag("ServerUI"))
         * {
         * TCPIPToBMLRealizerAdapter theServer = are.getTcpipToBMLRealizerAdapter();
         * BridgeServerUI bsui = null;
         * if (theServer == null)
         * {
         * bsui = new BridgeServerUI(are.getRealizerPort(), 7521, 1257);
         * }
         * else
         * {
         * bsui = new BridgeServerUI(are.getRealizerPort(), theServer);
         * }
         * addJComponent(bsui);
         * tokenizer.takeSTag("ServerUI");
         * tokenizer.takeETag("ServerUI");
         * }
         * else
         */
        if (tokenizer.atSTag("BmlUI"))
        {
            attrMap = tokenizer.getAttributes();
            String demoscriptdir = adapter.getOptionalAttribute("demoscriptresources", attrMap, "bml1.0/defaultexamples");
            addJComponent(new RealizerPortUI(are.getRealizerPort(), demoscriptdir));
            tokenizer.takeSTag("BmlUI");
            tokenizer.takeETag("BmlUI");
        }
        else if (tokenizer.atSTag("FeedbackUI"))
        {
            addJComponent(new FeedbackPanel(are.getRealizerPort()));
            tokenizer.takeSTag("FeedbackUI");
            tokenizer.takeETag("FeedbackUI");
        }
        else if (tokenizer.atSTag("KillButton"))
        {
            /*
             * // make kill button
             * // make the 'kill VH' button and add it to this UI
             * killVH = new JButton("KILL VH");
             * killVH.addActionListener(new ActionListener()
             * {
             * public void actionPerformed(ActionEvent e)
             * {
             * theVirtualHuman.unload();
             * }
             * });
             * addJComponent(killVH);
             */
            logger.error("no kill button implemented");
            tokenizer.takeSTag("KillButton");
            tokenizer.takeETag("KillButton");
        }
        else
        {
            throw tokenizer.getXMLScanException("Unknown tag in Loader content");
        }

    }

    @Override
    public void unload()
    {
        pullThePlug();
    }

    public void addKeyListener(KeyListener kl)
    {
        theUI.addKeyListener(kl);
        // contentPanel.addKeyListener(kl);
    }

    public void addWindowListener(WindowListener wl)
    {
        theUI.addWindowListener(wl);
    }

    public void addJComponent(JComponent jc)
    {
        jc.setAlignmentX(JFrame.LEFT_ALIGNMENT);
        contentPanel.add(jc);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public void removeJComponent(JComponent jc)
    {
        contentPanel.remove(jc);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    /** Return this embodiment */
    @Override
    public JComponentEmbodiment getEmbodiment()
    {
        return jcEmbodiment;
    }

    /**
     * method to programatically close the frame, from
     * http://stackoverflow.com/questions
     * /1234912/how-to-programmatically-close-a-jframe
     */
    public void pullThePlug()
    {
        theUI.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        WindowEvent wev = new WindowEvent(theUI, WindowEvent.WINDOW_CLOSING);
        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
    }

}
