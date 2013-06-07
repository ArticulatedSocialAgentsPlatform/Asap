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
public class AsapBMLFlowVisualizerTest
{
    private RealizerPort mockPort = mock(RealizerPort.class);
    private PlanningQueueVisualization mockPlanqVis = mock(PlanningQueueVisualization.class);
    private FinishedQueueVisualization mockFinishedqVis = mock(FinishedQueueVisualization.class);
    private PlayingQueueVisualization mockPlayqVis = mock(PlayingQueueVisualization.class);
    private AsapBMLFlowVisualizerPort vis;
    
    @Before
    public void before()
    {
        when(mockPlanqVis.getVisualization()).thenReturn(new JPanel());
        when(mockFinishedqVis.getVisualization()).thenReturn(new JPanel());
        when(mockPlayqVis.getVisualization()).thenReturn(new JPanel());
        vis = new AsapBMLFlowVisualizerPort(mockPort);
        vis.addVisualization(mockPlanqVis,mockFinishedqVis,mockPlayqVis);        
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
        verify(mockPlanqVis).addBlock(any(BehaviourBlock.class));
    }
    
    @Test
    public void testFinished()
    {
        String feedbackString = "<blockProgress id=\"bml1:end\" globalTime=\"10\" xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\"/>";
        vis.feedback(feedbackString);
        verify(mockFinishedqVis).addBlock(any(BMLBlockProgressFeedback.class));
        verify(mockPlanqVis).removeBlock("bml1");
        verify(mockPlayqVis).removeBlock("bml1");
    }
    
    @Test
    public void testPredictionFeedback()
    {
        String feedbackString = "<predictionFeedback xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\">" +
        		"<bml id=\"bml1\" globalStart=\"1\" globalEnd=\"7\"/></predictionFeedback>";
        vis.feedback(feedbackString);
        verify(mockPlanqVis).removeBlock("bml1");
        verify(mockPlayqVis).updateBlock(any(BMLBlockPredictionFeedback.class));
    }
    
    @Test
    public void testStartFeedback()
    {
        String feedbackString = "<blockProgress id=\"bml1:start\" globalTime=\"10\" xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\"/>";
        vis.feedback(feedbackString);
        verify(mockPlanqVis).removeBlock("bml1");
        verify(mockPlayqVis).startBlock(any(BMLBlockProgressFeedback.class));
    }
}
