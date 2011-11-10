package asap.bmlb;

import hmi.bml.core.CoreSchedulingMechanism;
import hmi.bml.core.SchedulingMechanism;

public enum BMLBSchedulingMechanisms implements SchedulingMechanism
{
    REPLACE, MERGE, APPEND, APPEND_AFTER, CHUNK_AFTER;
    
    @Override
    public String getNameStart()
    {
        switch(this)
        {
        case CHUNK_AFTER: return "chunk-after";
        default: return toCoreSchedulingMechanism().getNameStart();
        }        
    }    
    
    private CoreSchedulingMechanism toCoreSchedulingMechanism()
    {
        return CoreSchedulingMechanism.valueOf(toString());
    }
}
