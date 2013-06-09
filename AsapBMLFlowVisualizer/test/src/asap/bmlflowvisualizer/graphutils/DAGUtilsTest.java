package asap.bmlflowvisualizer.graphutils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

/**
 * Unit tests for TopologicalSort
 * @author Herwin
 * 
 */
public class DAGUtilsTest
{
    @Test
    public void sortNone()
    {
        List<String> V = DAGUtils.topologicalSort(new ArrayList<String>(), new ArrayList<Edge<String>>());
        assertTrue(V.isEmpty());
    }
    
    @Test
    public void sortOne()
    {
        List<String> V = DAGUtils.topologicalSort(ImmutableList.of("bml1"), new ArrayList<Edge<String>>());
        assertThat(V, IsIterableContainingInOrder.contains("bml1"));
    }

    @Test
    public void sortQueue()
    {
        List<String> V = DAGUtils.topologicalSort(ImmutableList.of("bml1", "bml2", "bml3"),
                ImmutableList.of(new Edge<String>("bml1", "bml2"), new Edge<String>("bml2", "bml3")));
        assertThat(V, IsIterableContainingInOrder.contains("bml1", "bml2", "bml3"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void sortCycle()
    {
        DAGUtils.topologicalSort(ImmutableList.of("bml1", "bml2"),
                ImmutableList.of(new Edge<String>("bml1", "bml2"), new Edge<String>("bml2", "bml1")));
    }

    @Test
    public void sort()
    {
        List<String> V = DAGUtils.topologicalSort(ImmutableList.of("bml1", "bml2", "bml3", "bml4", "bml5", "bml6"), ImmutableList.of(
                new Edge<String>("bml1", "bml2"), new Edge<String>("bml1", "bml3"), new Edge<String>("bml3", "bml4"), new Edge<String>(
                        "bml2", "bml6"), new Edge<String>("bml4", "bml6")));
        
        //implementation specific check: other orders are also valid topological orders 
        assertThat(V, IsIterableContainingInOrder.contains("bml1","bml3","bml4","bml2","bml5","bml6"));
    }
    
    @Test
    public void longestPathNone()
    {
        List<String> V = DAGUtils.longestPath(new ArrayList<String>(), new ArrayList<Edge<String>>());
        assertTrue(V.isEmpty());
    }
    
    @Test
    public void longestPathOne()
    {
        List<String> V = DAGUtils.longestPath(ImmutableList.of("bml1"), new ArrayList<Edge<String>>());
        assertThat(V, IsIterableContainingInOrder.contains("bml1"));
    }
    
    @Test
    public void longestPathQueue()
    {
        List<String> V = DAGUtils.longestPath(ImmutableList.of("bml1", "bml2", "bml3"),
                ImmutableList.of(new Edge<String>("bml1", "bml2"), new Edge<String>("bml2", "bml3")));
        assertThat(V, IsIterableContainingInOrder.contains("bml1", "bml2", "bml3"));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void longestPathCycle()
    {
        DAGUtils.topologicalSort(ImmutableList.of("bml1", "bml2"),
                ImmutableList.of(new Edge<String>("bml1", "bml2"), new Edge<String>("bml2", "bml1")));
    }
    
    @Test
    public void longestPath()
    {
        List<String> V = DAGUtils.longestPath(ImmutableList.of("bml1", "bml2", "bml3", "bml4", "bml5", "bml6"), ImmutableList.of(
                new Edge<String>("bml1", "bml2"), new Edge<String>("bml1", "bml3"), new Edge<String>("bml3", "bml4"), new Edge<String>(
                        "bml2", "bml6"), new Edge<String>("bml4", "bml6")));
        assertThat(V, IsIterableContainingInOrder.contains("bml1","bml3","bml4","bml6"));
    }
}
