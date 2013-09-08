package asap.bmlflowvisualizer.graphutils;

import java.awt.Point;
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

    private static <V> Set<V> getOutgoingNeighbours(V v, Collection<Edge<V>> edges)
    {
        Set<V> outgoing = new HashSet<V>();
        for (Edge<V> e : edges)
        {
            if (e.getStart().equals(v))
            {
                outgoing.add(e.getEnd());
            }
        }
        return outgoing;
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

    private static <V> void setConnectedDown(Set<V> checkConnections, Set<V> connections, Collection<Edge<V>> edges)
    {
        for (V v : checkConnections)
        {
            connections.add(v);
            Set<V> outgoing = getOutgoingNeighbours(v, edges);
            if (!outgoing.isEmpty())
            {
                setConnectedDown(outgoing, connections, edges);
            }
        }
    }

    private static <V> Set<V> getConnectedDown(V v, Collection<Edge<V>> edges)
    {
        Set<V> connections = new HashSet<V>();
        connections.add(v);
        Set<V> checkConnections = new HashSet<V>();
        checkConnections.add(v);
        setConnectedDown(checkConnections, connections, edges);
        return connections;
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
                if (e.getStart().equals(n))
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
            throw new IllegalArgumentException("Attempting topological sort on a graph with one or more cycles. Edges:" + ed + ", "
                    + " Vertices: " + vertices + " Unhandled edges " + edges);
        }
        return L;
    }

    public static <V> List<V> longestPath(Collection<V> vertices, Collection<Edge<V>> ed)
    {
        List<V> L = new ArrayList<V>();
        Map<V, Integer> longestPaths = longestPaths(vertices, ed);
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
            L.add(0, maxV);
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

    /**
     * For each vertex V, provide the longest path ending at V
     */
    public static <V> Map<V, Integer> longestPaths(Collection<V> vertices, Collection<Edge<V>> ed)
    {
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
            longestPaths.put(v, pl + 1);            
        }
        return longestPaths;
    }

    public static <V> Set<Set<V>> getClusters(Collection<V> vertices, Collection<Edge<V>> ed)
    {
        Set<Set<V>> clusters = new HashSet<Set<V>>();
        List<V> topSort = topologicalSort(vertices, ed);

        for (V v : topSort)
        {
            boolean alreadyIn = false;
            for (Set<V> cluster : clusters)
            {
                if (cluster.contains(v))
                {
                    alreadyIn = true;
                    continue;
                }
            }
            if (!alreadyIn)
            {
                Set<V> downConnected = getConnectedDown(v, ed);
                clusters.add(downConnected);
            }
        }

        return clusters;
    }

    public synchronized static <V> Map<V, Point> layout(Collection<V> vertices, Collection<Edge<V>> ed)
    {
        Map<V, Point> pos = new HashMap<V, Point>();
        Map<V, Integer> paths = longestPaths(vertices, ed);
        Map<Integer, Integer> width = new HashMap<Integer, Integer>();

        int currentx = 0;

        for (Set<V> cluster : getClusters(vertices, ed))
        {
            int maxx = currentx;
            for (V v : cluster)
            {
                int y = paths.get(v) - 1;
                if (width.get(y) == null)
                {
                    width.put(y, currentx);
                }
                else
                {
                    int x = width.get(y) + 1;
                    width.put(y, x);
                    if (x > maxx)
                    {
                        maxx = x;
                    }
                }
                pos.put(v, new Point(width.get(y), y));
            }
            for (Entry<Integer, Integer> entry : width.entrySet())
            {
                entry.setValue(maxx + 1);
            }
            currentx = maxx + 2;
        }
        return pos;
    }
}
