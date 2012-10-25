package asap.bml.bridge.ui;

import saiba.bml.feedback.BMLBlockProgressFeedback;
import saiba.bml.feedback.BMLPredictionFeedback;
import saiba.bml.feedback.BMLSyncPointProgressFeedback;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import saiba.bml.feedback.BMLWarningFeedback;

import asap.bml.bridge.RealizerPort;
import asap.bml.feedback.BMLFeedbackListener;

/**
 * User interface element to hook up to BML feedback information
 * @author reidsma, welberge
 */
public class FeedbackPanel extends JPanel implements BMLFeedbackListener
{
    // XXX class is not serializable (see findbugs). Better to make this class HAVE a panel rather than BE a panel
    private static final long serialVersionUID = 1L;
    /** Text area to give output feedback */
    private JTextArea feedbackOutput = null;
    private JTextArea warningOutput = null;
    private JTextArea predictionOutput = null;
    private JTabbedPane tabPane;

    private static final int PREF_WIDTH = 500;
    private static final int PREF_HEIGHT = 80;
    
    public FeedbackPanel(RealizerPort bridge)
    {
        super();
        // text plane for result output
        feedbackOutput = new JTextArea();
        feedbackOutput.setEditable(false);
        JScrollPane resultScroll = new JScrollPane(feedbackOutput);
        resultScroll.setPreferredSize(new Dimension(PREF_WIDTH, PREF_HEIGHT));

        predictionOutput = new JTextArea();
        predictionOutput.setEditable(false);
        JScrollPane planningScroll = new JScrollPane(predictionOutput);
        planningScroll.setPreferredSize(new Dimension(PREF_WIDTH, PREF_HEIGHT));

        warningOutput = new JTextArea();
        warningOutput.setEditable(false);
        JScrollPane warningScroll = new JScrollPane(warningOutput);
        warningScroll.setPreferredSize(new Dimension(PREF_WIDTH, PREF_HEIGHT));

        tabPane = new JTabbedPane();
        tabPane.addTab("Warnings", warningScroll);
        tabPane.addTab("Feedback", resultScroll);
        tabPane.addTab("Prediction", planningScroll);
        add(tabPane);
        JButton clearButton = new JButton("clear");
        clearButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                clear();
            }
        });
        add(clearButton);
        bridge.addListeners(this);
    }

    public void clear()
    {
        feedbackOutput.setText("");
        warningOutput.setText("");
        predictionOutput.setText("");
    }

    public void appendWarning(String text)
    {
        warningOutput.append(text);
        warningOutput.setCaretPosition(warningOutput.getText().length());
    }

    public void blockProgress(BMLBlockProgressFeedback psf)
    {
        feedbackOutput.append(psf.toString());
    }    

    public void syncProgress(BMLSyncPointProgressFeedback spp)
    {
        feedbackOutput.append(spp.toString());
    }

    public void prediction(BMLPredictionFeedback bpf)
    {
        predictionOutput.append(bpf.toString());
    }

    public void warn(BMLWarningFeedback bw)
    {
        // show the feedback from the realizer
        warningOutput.append(bw.toString());
        warningOutput.setCaretPosition(warningOutput.getText().length());
    }

    @Override
    public void feedback(String feedback)
    {
         //TODO       
    }
}
