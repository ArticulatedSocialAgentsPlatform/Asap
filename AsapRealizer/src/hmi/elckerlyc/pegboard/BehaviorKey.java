package hmi.elckerlyc.pegboard;

import lombok.Data;

/**
 * behaviorId, bmlId pair 
 * @author hvanwelbergen
 */
@Data
public class BehaviorKey
{
    private final String bmlId;    
    private final String behaviorId;
}
