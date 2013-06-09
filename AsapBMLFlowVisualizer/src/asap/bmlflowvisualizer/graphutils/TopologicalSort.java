package asap.bmlflowvisualizer.graphutils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Topological sort algorithm, implemented from http://en.wikipedia.org/wiki/Topological_ordering
 * @author Herwin
 * 
 */
public class TopologicalSort
{
    private TopologicalSort()
    {
    }

    private static <V> Set<V> getIncomingEdges(Collection<Edge<V>> edges)
    {
        Set<V> incoming = new HashSet<V>();
        for (Edge<V> e : edges)
        {
            incoming.add(e.getEnd());
        }
        return incoming;
    }

    private static <V> Set<V> getVerticesWithNoIncomingEdges(Collection<V> vertices, Collection<Edge<V>> edges)
    {
        Set<V> v = new HashSet<V>();
        v.addAll(vertices);
        Set<V> incoming = getIncomingEdges(edges);
        v.removeAll(incoming);
        return v;
    }

    private static <V> boolean hasIncomingEdges(V v, Collection<Edge<V>> edges)
    {
        Set<V> incoming = getIncomingEdges(edges);
        return incoming.contains(v);
    }

    public static <V> List<V> sort(Collection<V> vertices, Collection<Edge<V>> ed)
    {
        List<V> L = new ArrayList<V>();
        List<Edge<V>> edges = new ArrayList<Edge<V>>();
        edges.addAll(ed);
        Set<V> S = getVerticesWithNoIncomingEdges(vertices, edges);
        while (!S.isEmpty())
        {
            V n = S.iterator().next();
            S.remove(n);
            L.add(n);

            for (Edge<V> e : ed)
            {
                if (e.getStart() == n)
                {
                    V m = e.getEnd();
                    edges.remove(e);
                    if(!hasIncomingEdges(m,edges))
                    {
                        S.add(m);
                    }
                }
            }
        }
        if(!edges.isEmpty())
        {
            throw new IllegalArgumentException("Attempting topological sort on a graph with one or more cycles.");
        }
        return L;
    }
}
