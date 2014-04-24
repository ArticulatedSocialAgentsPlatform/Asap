package asap.bmlflowvisualizer;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.swing.JPanel;

import org.junit.Before;
import org.junit.Test;

import saiba.bml.core.BehaviourBlock;
import saiba.bml.feedback.BMLBlockPredictionFeedback;
import saiba.bml.feedback.BMLBlockProgressFeedback;
import asap.realizerport.RealizerPort;

/**
 * Unit tests for the AsapBMLFlowVisualizer
 * @author hvanwelbergen
 * 
 */
public class BMLFlowVisualizerTest
{
    private RealizerPort mockPort = mock(RealizerPort.class);
    private BMLFlowVisualization mockPlanqVis = mock(BMLFlowVisualization.class);
    private BMLFlowVisualization mockFinishedqVis = mock(BMLFlowVisualization.class);
    private BMLFlowVisualization mockPlayqVis = mock(BMLFlowVisualization.class);
    private BMLFlowVisualizerPort vis;

    @Before
    public void before()
    {
        when(mockPlanqVis.getVisualization()).thenReturn(new JPanel());
        when(mockFinishedqVis.getVisualization()).thenReturn(new JPanel());
        when(mockPlayqVis.getVisualization()).thenReturn(new JPanel());
        vis = new BMLFlowVisualizerPort(mockPort);
        vis.setVisualization(mockPlanqVis, mockFinishedqVis, mockPlayqVis);
    }

    @Test
    public void testBMLSendThrough()
    {
        String bmlString = "<bml id=\"bml1\" xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\"></bml>";
        vis.performBML(bmlString);
        verify(mockPort).performBML(bmlString);
    }

    @Test
    public void testAddToPlanning()
    {
        String bmlString = "<bml id=\"bml1\" xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\"></bml>";
        vis.performBML(bmlString);
        verify(mockPlanqVis).planBlock(any(BehaviourBlock.class));
        verify(mockPlayqVis).planBlock(any(BehaviourBlock.class));
        verify(mockFinishedqVis).planBlock(any(BehaviourBlock.class));
    }

    @Test
    public void testFinished()
    {
        String feedbackString = "<blockProgress id=\"bml1:end\" globalTime=\"10\" xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\"/>";
        vis.feedback(feedbackString);
        verify(mockFinishedqVis).finishBlock(any(BMLBlockProgressFeedback.class));
        verify(mockPlanqVis).finishBlock(any(BMLBlockProgressFeedback.class));
        verify(mockPlayqVis).finishBlock(any(BMLBlockProgressFeedback.class));
    }

    @Test
    public void testPredictionFeedback()
    {
        String feedbackString = "<predictionFeedback xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\">"
                + "<bml id=\"bml1\" globalStart=\"1\" globalEnd=\"7\"/></predictionFeedback>";
        vis.feedback(feedbackString);
        verify(mockPlanqVis).updateBlock(any(BMLBlockPredictionFeedback.class));
        verify(mockPlayqVis).updateBlock(any(BMLBlockPredictionFeedback.class));
        verify(mockFinishedqVis).updateBlock(any(BMLBlockPredictionFeedback.class));
    }

    @Test
    public void testStartFeedback()
    {
        String feedbackString = "<blockProgress id=\"bml1:start\" globalTime=\"10\" xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\"/>";
        vis.feedback(feedbackString);
        verify(mockPlanqVis).startBlock(any(BMLBlockProgressFeedback.class));
        verify(mockPlayqVis).startBlock(any(BMLBlockProgressFeedback.class));
        verify(mockFinishedqVis).startBlock(any(BMLBlockProgressFeedback.class));
    }

    @Test
    public void testClear()
    {
        String bmlString = "<bml id=\"bml1\" composition=\"REPLACE\" xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\"/>";
        vis.performBML(bmlString);
        verify(mockPlanqVis).clear();
        verify(mockPlayqVis).clear();
        verify(mockFinishedqVis).clear();
    }

    @Test
    public void testInvalidBML()
    {
        String bmlString = "<bml id=\"bml1\" xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" "
                + "xmlns:pe=\"http://hmi.ewi.utwente.nl/pictureengine\"> "
                + "<pe:addAnimationXML id=\"anim\" layer=\"5\" start=\"0\" end=\"5\" "
                + "filePath=\"pictureengine/example/animations/\" fileName=\"speak.xml\"/>" + "</bml>";
        vis.performBML(bmlString);
    }

    @Test
    public void testInvalidBML2()
    {
        vis.performBML("<invalid>");
        verify(mockPort).performBML("<invalid>");
    }

    @Test
    public void testInvalidFeedback2()
    {
        vis.feedback("<invalid>");
    }

    @Test
    public void testInvalidFeedback()
    {
        String feedbackString = "<blockProgress id=\"bml1:start\" globalTime=\"10\" xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\">";
        vis.feedback(feedbackString);
    }
}
