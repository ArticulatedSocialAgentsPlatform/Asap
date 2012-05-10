package hmi.bml.ext.bmlt;

import hmi.bml.core.BMLBlockComposition;
import hmi.bml.core.CoreComposition;

/**
 * APPEND_AFTER: start when a selected set of previously sent behavior is finished<br> 
 * @author welberge
 */
public enum BMLTSchedulingMechanism implements BMLBlockComposition
{
    UNKNOWN, REPLACE, MERGE, APPEND, APPEND_AFTER;
    
    @Override
    public String getNameStart()
    {
        switch(this)
        {
        case APPEND_AFTER: return "APPEND-AFTER";
        default: return toCoreSchedulingMechanism().getNameStart();
        }        
    }    
    
    private CoreComposition toCoreSchedulingMechanism()
    {
        switch(this)
        {
        case UNKNOWN: return CoreComposition.UNKNOWN;
        case REPLACE: return CoreComposition.REPLACE;
        case MERGE: return CoreComposition.MERGE;
        case APPEND: return CoreComposition.APPEND;
        default: return CoreComposition.UNKNOWN;
        }
    }
    
    public static BMLTSchedulingMechanism parse(String input)
    {
        for(BMLTSchedulingMechanism mech: BMLTSchedulingMechanism.values())
        {
            if(mech.getNameStart().equals(input))
            {
                return mech;
            }
        }
        return UNKNOWN;
    } 
}
