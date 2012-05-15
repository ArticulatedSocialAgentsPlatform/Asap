package asap.realizer.pegboard;

import com.google.common.collect.ImmutableSet;

import lombok.Data;

/**
 * Stores cluster of connected behaviors
 * @author hvanwelbergen
 */
@Data
public class BehaviorCluster
{
    private final ImmutableSet<BehaviorKey> behaviors;
    private final boolean grounded;
}
