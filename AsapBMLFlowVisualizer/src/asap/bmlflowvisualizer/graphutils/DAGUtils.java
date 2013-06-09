package asap.bmlflowvisualizer.graphutils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Topological sort algorithm, implemented from http://en.wikipedia.org/wiki/Topological_ordering
 * @author Herwin
 * 
 */
public class DAGUtils
{
    private DAGUtils()
    {
    }

    private static <V> Set<V> getIncomingNeighbours(V v, Collection<Edge<V>> edges)
    {
        Set<V> incoming = new HashSet<V>();
        for (Edge<V> e : edges)
        {
            if (e.getEnd().equals(v))
            {
                incoming.add(e.getStart());
            }
        }
        return incoming;
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

    public static <V> List<V> topologicalSort(Collection<V> vertices, Collection<Edge<V>> ed)
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
                    if (!hasIncomingEdges(m, edges))
                    {
                        S.add(m);
                    }
                }
            }
        }
        if (!edges.isEmpty())
        {
            throw new IllegalArgumentException("Attempting topological sort on a graph with one or more cycles.");
        }
        return L;
    }

    public static <V> List<V> longestPath(Collection<V> vertices, Collection<Edge<V>> ed)
    {
        List<V> L = new ArrayList<V>();
        List<V> topologicalOrder = topologicalSort(vertices, ed);

        Map<V, Integer> longestPaths = new HashMap<V, Integer>();
        for (V v : topologicalOrder)
        {
            int pl = 0;
            for (V inc : getIncomingNeighbours(v, ed))
            {
                if (longestPaths.get(inc) > pl)
                {
                    pl = longestPaths.get(inc);
                }
            }
            if (pl > 0)
            {
                longestPaths.put(v, pl + 1);
            }
            else
            {
                longestPaths.put(v, pl + 1);
            }
        }

        int max = 0;
        V maxV = null;
        for (Entry<V, Integer> entry : longestPaths.entrySet())
        {
            if (entry.getValue() >= max)
            {
                maxV = entry.getKey();
                max = entry.getValue();
            }
        }
        

        while (max != 0)
        {
            L.add(0,maxV);
            max = 0;
            Set<V> incoming = getIncomingNeighbours(maxV, ed);
            for (V inc : incoming)
            {
                int pl = longestPaths.get(inc);
                if (pl >= max)
                {
                    maxV = inc;
                    max = pl;
                }
            }            
        }

        return L;
    }
}
