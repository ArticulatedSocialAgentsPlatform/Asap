package asap.bmlb;

import hmi.bml.core.BMLBlockComposition;
import hmi.bml.ext.bmlt.BMLTSchedulingMechanism;

/**
 * The scheduling mechanisms used in BMLB
 * @author hvanwelbergen
 */
public enum BMLBComposition implements BMLBlockComposition
{
    UNKNOWN, REPLACE, MERGE, APPEND, APPEND_AFTER, CHUNK_AFTER;
    
    @Override
    public String getNameStart()
    {
        switch(this)
        {
        case CHUNK_AFTER: return "CHUNK-AFTER";
        default: return toBMLTSchedulingMechanism().getNameStart();
        }        
    }
    
    public static BMLBComposition convert(BMLBlockComposition mech)
    {
        return parse(mech.getNameStart());
    }
    
    public static BMLBComposition parse(String input)
    {
        for(BMLBComposition mech: BMLBComposition.values())
        {
            if(mech.getNameStart().equals(input))
            {
                return mech;
            }
        }
        return UNKNOWN;
    }  
    
    private BMLTSchedulingMechanism toBMLTSchedulingMechanism()
    {
        switch(this)
        {
        case UNKNOWN: return BMLTSchedulingMechanism.UNKNOWN;
        case REPLACE: return BMLTSchedulingMechanism.REPLACE;
        case MERGE: return BMLTSchedulingMechanism.MERGE;
        case APPEND: return BMLTSchedulingMechanism.APPEND;
        case APPEND_AFTER: return BMLTSchedulingMechanism.APPEND_AFTER;
        default: return BMLTSchedulingMechanism.UNKNOWN;
        }
    }
}
