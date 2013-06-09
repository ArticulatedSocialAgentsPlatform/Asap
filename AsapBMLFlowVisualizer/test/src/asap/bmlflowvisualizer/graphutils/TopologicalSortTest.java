package asap.bmlflowvisualizer.graphutils;

import static org.hamcrest.MatcherAssert.assertThat;

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
public class TopologicalSortTest
{
    @Test
    public void sortOne()
    {
        List<String> V = TopologicalSort.sort(ImmutableList.of("bml1"), new ArrayList<Edge<String>>());
        assertThat(V, IsIterableContainingInOrder.contains("bml1"));
    }

    @Test
    public void sortQueue()
    {
        List<String> V = TopologicalSort.sort(ImmutableList.of("bml1", "bml2", "bml3"),
                ImmutableList.of(new Edge<String>("bml1", "bml2"), new Edge<String>("bml2", "bml3")));
        assertThat(V, IsIterableContainingInOrder.contains("bml1", "bml2", "bml3"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void sortCycle()
    {
        TopologicalSort.sort(ImmutableList.of("bml1", "bml2"),
                ImmutableList.of(new Edge<String>("bml1", "bml2"), new Edge<String>("bml2", "bml1")));
    }

    @Test
    public void sort()
    {
        List<String> V = TopologicalSort.sort(ImmutableList.of("bml1", "bml2", "bml3", "bml4", "bml5", "bml6"), ImmutableList.of(
                new Edge<String>("bml1", "bml2"), new Edge<String>("bml1", "bml3"), new Edge<String>("bml3", "bml4"), new Edge<String>(
                        "bml2", "bml6"), new Edge<String>("bml4", "bml6")));
        
        //implementation specific check: other orders are also valid topological orders 
        assertThat(V, IsIterableContainingInOrder.contains("bml1","bml3","bml4","bml2","bml5","bml6"));
    }
}
