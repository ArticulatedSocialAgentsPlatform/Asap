package asap.bmlflowvisualizer.graphutils;

import lombok.Data;


/**
 * Stores an edge between vertex start and end
 * @author Herwin
 * 
 * @param <V> vertex type/id
 */
@Data
public class Edge<V>
{
    final V start;
    final V end;
}
