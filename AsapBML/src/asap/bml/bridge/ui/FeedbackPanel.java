package asap.bml.bridge.ui;

import saiba.bml.bridge.RealizerPort;
import saiba.bml.feedback.BMLExceptionFeedback;
import saiba.bml.feedback.BMLExceptionListener;
import saiba.bml.feedback.BMLFeedbackListener;
import saiba.bml.feedback.BMLPerformanceStartFeedback;
import saiba.bml.feedback.BMLPerformanceStopFeedback;
import saiba.bml.feedback.BMLSyncPointProgressFeedback;
import saiba.bml.feedback.BMLWarningFeedback;
import saiba.bml.feedback.BMLWarningListener;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import asap.bml.ext.bmlt.feedback.BMLTSchedulingFinishedFeedback;
import asap.bml.ext.bmlt.feedback.BMLTSchedulingListener;
import asap.bml.ext.bmlt.feedback.BMLTSchedulingStartFeedback;

/**
 * User interface element to hook up to BML feedback information
 * @author reidsma, welberge
 */
public class FeedbackPanel extends JPanel implements BMLWarningListener, BMLFeedbackListener, BMLTSchedulingListener, BMLExceptionListener
{
    // XXX class is not serializable (see findbugs). Better to make this class HAVE a panel rather than BE a panel
    private static final long serialVersionUID = 1L;
    /** Text area to give output feedback */
    private JTextArea feedbackOutput = null;
    private JTextArea warningOutput = null;
    private JTextArea planningOutput = null;
    private JTabbedPane tabPane;

    public FeedbackPanel(RealizerPort bridge)
    {
        super();
        // text plane for result output
        feedbackOutput = new JTextArea();
        feedbackOutput.setEditable(false);
        JScrollPane resultScroll = new JScrollPane(feedbackOutput);
        resultScroll.setPreferredSize(new Dimension(500, 80));

        planningOutput = new JTextArea();
        planningOutput.setEditable(false);
        JScrollPane planningScroll = new JScrollPane(planningOutput);
        planningScroll.setPreferredSize(new Dimension(500, 80));

        warningOutput = new JTextArea();
        warningOutput.setEditable(false);
        JScrollPane warningScroll = new JScrollPane(warningOutput);
        warningScroll.setPreferredSize(new Dimension(500, 80));

        tabPane = new JTabbedPane();
        tabPane.addTab("Warnings", warningScroll);
        tabPane.addTab("Feedback", resultScroll);
        tabPane.addTab("Planning", planningScroll);
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
        planningOutput.setText("");
    }

    public void appendWarning(String text)
    {
        warningOutput.append(text);
        warningOutput.setCaretPosition(warningOutput.getText().length());
    }

    @Override
    public void performanceStop(BMLPerformanceStopFeedback psf)
    {
        feedbackOutput.append(psf.toString());
    }

    @Override
    public void performanceStart(BMLPerformanceStartFeedback psf)
    {
        feedbackOutput.append(psf.toString());
    }

    @Override
    public void syncProgress(BMLSyncPointProgressFeedback spp)
    {
        feedbackOutput.append(spp.toString());
    }

    @Override
    public void exception(BMLExceptionFeedback be)
    {
        warningOutput.append(be.toString());
    }

    @Override
    public void schedulingFinished(BMLTSchedulingFinishedFeedback pff)
    {
        planningOutput.append(pff.toString());
    }

    @Override
    public void schedulingStart(BMLTSchedulingStartFeedback psf)
    {
        planningOutput.append(psf.toString());
    }

    @Override
    public void warn(BMLWarningFeedback bw)
    {
        // show the feedback from the realizer
        warningOutput.append(bw.toString());
        warningOutput.setCaretPosition(warningOutput.getText().length());
    }
}
