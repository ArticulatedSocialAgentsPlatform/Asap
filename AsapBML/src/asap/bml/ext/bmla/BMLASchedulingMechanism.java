/*******************************************************************************
 *******************************************************************************/
package asap.bml.ext.bmla;

import saiba.bml.core.BMLBlockComposition;
import saiba.bml.core.CoreComposition;

/**
 * APPEND_AFTER: start when a selected set of previously sent behavior is finished<br> 
 * @author welberge
 */
public enum BMLASchedulingMechanism implements BMLBlockComposition
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
    
    public static BMLASchedulingMechanism parse(String input)
    {
        for(BMLASchedulingMechanism mech: BMLASchedulingMechanism.values())
        {
            if(mech.getNameStart().equals(input))
            {
                return mech;
            }
        }
        return UNKNOWN;
    } 
}
