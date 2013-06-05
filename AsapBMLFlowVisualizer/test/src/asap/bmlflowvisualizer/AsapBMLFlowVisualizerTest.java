package asap.bmlflowvisualizer;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.swing.JPanel;

import org.junit.Before;
import org.junit.Test;

import saiba.bml.core.BehaviourBlock;

import asap.realizerport.RealizerPort;

/**
 * Unit tests for the AsapBMLFlowVisualizer
 * @author hvanwelbergen
 *
 */
public class AsapBMLFlowVisualizerTest
{
    private RealizerPort mockPort = mock(RealizerPort.class);
    private PlanningQueueVisualization mockPqVis = mock(PlanningQueueVisualization.class);
    private AsapBMLFlowVisualizer vis;
    
    @Before
    public void before()
    {
        when(mockPqVis.getVisualization()).thenReturn(new JPanel());
        vis = new AsapBMLFlowVisualizer(mockPort, mockPqVis);
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
        verify(mockPqVis).addBlock(any(BehaviourBlock.class));
    }
}
