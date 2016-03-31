/*******************************************************************************
 *******************************************************************************/
package asap.realizer.pegboard;

import lombok.Data;

import com.google.common.collect.ImmutableSet;

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
