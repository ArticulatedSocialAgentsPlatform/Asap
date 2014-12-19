package asap.bmlflowvisualizer.graphutils;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hamcrest.collection.IsIterableContainingInAnyOrder;
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

        // implementation specific check: other orders are also valid topological orders
        assertThat(V, IsIterableContainingInOrder.contains("bml1", "bml3", "bml4", "bml2", "bml5", "bml6"));
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
        DAGUtils.longestPath(ImmutableList.of("bml1", "bml2"),
                ImmutableList.of(new Edge<String>("bml1", "bml2"), new Edge<String>("bml2", "bml1")));
    }

    @Test
    public void longestPath()
    {
        List<String> V = DAGUtils.longestPath(ImmutableList.of("bml1", "bml2", "bml3", "bml4", "bml5", "bml6"), ImmutableList.of(
                new Edge<String>("bml1", "bml2"), new Edge<String>("bml1", "bml3"), new Edge<String>("bml3", "bml4"), new Edge<String>(
                        "bml2", "bml6"), new Edge<String>("bml4", "bml6")));
        assertThat(V, IsIterableContainingInOrder.contains("bml1", "bml3", "bml4", "bml6"));
    }

    @Test
    public void longestPathsNone()
    {
        Map<String, Integer> paths = DAGUtils.longestPaths(new ArrayList<String>(), new ArrayList<Edge<String>>());
        assertTrue(paths.isEmpty());
    }

    @Test
    public void longestPathsOne()
    {
        Map<String, Integer> paths = DAGUtils.longestPaths(ImmutableList.of("bml1"), new ArrayList<Edge<String>>());
        assertEquals(Integer.valueOf(1), paths.get("bml1"));
    }

    @Test
    public void longestPathsQueue()
    {
        Map<String, Integer> paths = DAGUtils.longestPaths(ImmutableList.of("bml1", "bml2", "bml3"),
                ImmutableList.of(new Edge<String>("bml1", "bml2"), new Edge<String>("bml2", "bml3")));
        assertEquals(Integer.valueOf(1), paths.get("bml1"));
        assertEquals(Integer.valueOf(2), paths.get("bml2"));
        assertEquals(Integer.valueOf(3), paths.get("bml3"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void longestPathsCycle()
    {
        DAGUtils.longestPaths(ImmutableList.of("bml1", "bml2"),
                ImmutableList.of(new Edge<String>("bml1", "bml2"), new Edge<String>("bml2", "bml1")));
    }

    @Test
    public void longestPaths()
    {
        Map<String, Integer> paths = DAGUtils.longestPaths(ImmutableList.of("bml1", "bml2", "bml3", "bml4", "bml5", "bml6"), ImmutableList
                .of(new Edge<String>("bml1", "bml2"), new Edge<String>("bml1", "bml3"), new Edge<String>("bml3", "bml4"), new Edge<String>(
                        "bml2", "bml6"), new Edge<String>("bml4", "bml6")));
        assertEquals(Integer.valueOf(1), paths.get("bml1"));
        assertEquals(Integer.valueOf(2), paths.get("bml2"));
        assertEquals(Integer.valueOf(2), paths.get("bml3"));
        assertEquals(Integer.valueOf(3), paths.get("bml4"));
        assertEquals(Integer.valueOf(1), paths.get("bml5"));
        assertEquals(Integer.valueOf(4), paths.get("bml6"));
    }

    @Test
    public void getClustersNone()
    {
        Set<Set<String>> clusters = DAGUtils.getClusters(new ArrayList<String>(), new ArrayList<Edge<String>>());
        assertTrue(clusters.isEmpty());
    }

    @Test
    public void getClustersOne()
    {
        Set<Set<String>> clusters = DAGUtils.getClusters(ImmutableList.of("bml1"), new ArrayList<Edge<String>>());
        assertEquals(1, clusters.size());
    }

    @Test
    public void getClustersQueue()
    {
        Set<Set<String>> clusters = DAGUtils.getClusters(ImmutableList.of("bml1", "bml2", "bml3"),
                ImmutableList.of(new Edge<String>("bml1", "bml2"), new Edge<String>("bml2", "bml3")));
        assertEquals(1, clusters.size());
        assertThat(clusters.iterator().next(), IsIterableContainingInAnyOrder.containsInAnyOrder("bml1", "bml2", "bml3"));
    }
    
    @Test
    public void getClustersTree()
    {
        Set<Set<String>> clusters  = DAGUtils.getClusters(ImmutableList.of("bml1", "bml2", "bml3"),
                ImmutableList.of(new Edge<String>("bml1", "bml2"), new Edge<String>("bml1", "bml3")));
        assertEquals(1, clusters.size());
        
    }

    @Test
    public void getClusters()
    {
        Set<Set<String>> clusters = DAGUtils.getClusters(ImmutableList.of("bml1", "bml2", "bml3", "bml4", "bml5", "bml6"), ImmutableList.of(
                new Edge<String>("bml1", "bml2"), new Edge<String>("bml1", "bml3"), new Edge<String>("bml3", "bml4"), new Edge<String>(
                        "bml2", "bml6"), new Edge<String>("bml4", "bml6")));
        assertEquals(2, clusters.size());
        Iterator<Set<String>> c = clusters.iterator();
        assertThat(c.next(), anyOf(IsIterableContainingInAnyOrder.containsInAnyOrder("bml1", "bml2", "bml3","bml4","bml6"),
                IsIterableContainingInAnyOrder.containsInAnyOrder("bml5")));
        assertThat(c.next(), anyOf(IsIterableContainingInAnyOrder.containsInAnyOrder("bml1", "bml2", "bml3","bml4","bml6"),
                IsIterableContainingInAnyOrder.containsInAnyOrder("bml5")));
    }
    
    @Test
    public void layoutNone()
    {
        Map<String,Point> layout = DAGUtils.layout(new ArrayList<String>(), new ArrayList<Edge<String>>());
        assertTrue(layout.isEmpty());
    }
    
    @Test
    public void layoutOne()
    {
        Map<String,Point> layout = DAGUtils.layout(ImmutableList.of("bml1"), new ArrayList<Edge<String>>());
        assertEquals(1,layout.size());
        assertEquals(new Point(0,0), layout.get("bml1"));
    }
    
    @Test
    public void layoutQueue()
    {
        Map<String,Point> layout = DAGUtils.layout(ImmutableList.of("bml1", "bml2", "bml3"),
                ImmutableList.of(new Edge<String>("bml1", "bml2"), new Edge<String>("bml2", "bml3")));
        assertEquals(3,layout.size());
        assertEquals(new Point(0,0), layout.get("bml1"));
        assertEquals(new Point(0,1), layout.get("bml2"));
        assertEquals(new Point(0,2), layout.get("bml3"));
    }
    
    @Test
    public void layoutTree()
    {
        Map<String,Point> layout = DAGUtils.layout(ImmutableList.of("bml1", "bml2", "bml3"),
                ImmutableList.of(new Edge<String>("bml1", "bml2"), new Edge<String>("bml1", "bml3")));
        assertEquals(3,layout.size());
        assertEquals(new Point(0,0), layout.get("bml1"));
        assertThat(layout.get("bml2"), anyOf( equalTo(new Point(0,1)), equalTo(new Point(1,1))));
        assertThat(layout.get("bml3"), anyOf( equalTo(new Point(0,1)), equalTo(new Point(1,1))));        
    }
    
    @Test
    public void layout()
    {
        Map<String,Point> layout = DAGUtils.layout(ImmutableList.of("bml1", "bml2", "bml3", "bml4", "bml5", "bml6"), ImmutableList.of(
                new Edge<String>("bml1", "bml2"), new Edge<String>("bml1", "bml3"), new Edge<String>("bml3", "bml4"), new Edge<String>(
                        "bml2", "bml6"), new Edge<String>("bml4", "bml6")));
        assertEquals(6,layout.size());
        System.out.println(layout);
        assertThat(layout.get("bml1"), anyOf(equalTo(new Point(0,0)),equalTo(new Point(2,0))));
        assertThat(layout.get("bml5"), anyOf(equalTo(new Point(0,0)),equalTo(new Point(2,0))));
        assertThat(layout.get("bml2"), anyOf(equalTo(new Point(0,1)),equalTo(new Point(1,1)),equalTo(new Point(2,1)),equalTo(new Point(3,1))));
        assertThat(layout.get("bml3"), anyOf(equalTo(new Point(0,1)),equalTo(new Point(1,1)),equalTo(new Point(2,1)),equalTo(new Point(3,1))));
        assertEquals(new Point(layout.get("bml3").x, 2), layout.get("bml4"));
        assertEquals(new Point(layout.get("bml3").x, 3), layout.get("bml6"));
    }
}
