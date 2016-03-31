/*******************************************************************************
 *******************************************************************************/
package asap.tcpipadapters.ui;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import asap.realizerport.RealizerPort;
import asap.tcpipadapters.ConnectionStateListener;
import asap.tcpipadapters.TCPIPToBMLRealizerAdapter;
import asap.tcpipadapters.TCPIPToBMLRealizerAdapter.ServerState;

/**
 * A graphical UI that, given a RealizerBridge, offers functionality to start and stop
 * TCPIPRealizerBridgeServer for it
 * 
 * @author Dennis Reidsma
 */
public class BridgeServerUI implements ConnectionStateListener
{
    /** The realizerbridge */
    protected RealizerPort realizerBridge = null;

    /** The server that this UI has made for the realizerbridge */
    protected TCPIPToBMLRealizerAdapter server = null;

    protected JButton startButton = null;

    protected JButton stopButton = null;

    protected JSpinner bmlPortSpinner = null;

    protected JSpinner feedbackPortSpinner = null;

    protected JLabel stateLabel = null;
    
    private JPanel mainPanel = new JPanel();

    public BridgeServerUI(RealizerPort newBridge)
    {
        this(newBridge, 7500, 7501);
    }

    public BridgeServerUI(RealizerPort newBridge, TCPIPToBMLRealizerAdapter server)
    {
        this(newBridge, server.getRequestPort(), server.getFeedbackPort());
        this.server = server;
        server.addConnectionStateListener(this);
    }

    public BridgeServerUI(RealizerPort newBridge, int bmlPort, int feedbackPort)
    {
        realizerBridge = newBridge;

        stateLabel = new JLabel("" + ServerState.NOT_RUNNING);
        mainPanel.add(stateLabel);
        stopButton = new JButton("STOP");
        stopButton.addActionListener(new StopListener());
        mainPanel.add(stopButton);
        startButton = new JButton("START");
        startButton.addActionListener(new StartListener(this));
        mainPanel.add(startButton);

        bmlPortSpinner = new JSpinner(new SpinnerNumberModel(bmlPort, 1, 65000, 1));
        feedbackPortSpinner = new JSpinner(new SpinnerNumberModel(feedbackPort, 1, 65000, 1));
        mainPanel.add(new JLabel("bmlPort:"));
        mainPanel.add(bmlPortSpinner);
        mainPanel.add(new JLabel("feedbackPort:"));
        mainPanel.add(feedbackPortSpinner);

        stopButton.setEnabled(false);

    }

    public JComponent getUI()
    {
        return mainPanel;
    }
    
    public void stateChanged(final ServerState state)
    {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() { 
                // update the ui to reflect the new state, i.e. enable or disable the stop & connect button,
                // change icon or text that monitors the current state
                switch (state)
                {
                case WAITING:
                    stopButton.setEnabled(true);
                    startButton.setEnabled(false);
                    bmlPortSpinner.setEnabled(false);
                    feedbackPortSpinner.setEnabled(false);
                    stateLabel.setText("WAITING");
                    break;
                case CONNECTING:
                    stopButton.setEnabled(true);
                    startButton.setEnabled(false);
                    bmlPortSpinner.setEnabled(false);
                    feedbackPortSpinner.setEnabled(false);
                    stateLabel.setText("CONNECTING");
                    break;
                case CONNECTED:
                    stopButton.setEnabled(true);
                    startButton.setEnabled(false);
                    bmlPortSpinner.setEnabled(false);
                    feedbackPortSpinner.setEnabled(false);
                    stateLabel.setText("CONNECTED");
                    break;
                case DISCONNECTING:
                    stopButton.setEnabled(true);
                    startButton.setEnabled(false);
                    bmlPortSpinner.setEnabled(false);
                    feedbackPortSpinner.setEnabled(false);
                    stateLabel.setText("DISCONNECTING");
                    break;
                case NOT_RUNNING:
                    stopButton.setEnabled(false);
                    startButton.setEnabled(true);
                    bmlPortSpinner.setEnabled(true);
                    feedbackPortSpinner.setEnabled(true);
                    stateLabel.setText("NOT_RUNNING");
                    break;
                }
                stateLabel.revalidate();
                stateLabel.repaint();
            }
        });
    }

    /*
     * ============================================================================= A FEW HELPER
     * METHODS FOR SETTING UP THE UI
     * =============================================================================
     */

    /** start a new server */
    class StartListener implements ActionListener
    {
        BridgeServerUI theUI = null;

        public StartListener(BridgeServerUI ui)
        {
            theUI = ui;
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            server = new TCPIPToBMLRealizerAdapter(realizerBridge, ((Number) bmlPortSpinner.getValue()).intValue(),
                    ((Number) feedbackPortSpinner.getValue()).intValue());
            server.addConnectionStateListener(theUI);
        }
    }

    /** stop server */
    class StopListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            server.shutdown();
        }
    }

}
