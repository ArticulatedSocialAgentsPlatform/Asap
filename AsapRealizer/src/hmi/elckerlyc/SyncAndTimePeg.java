package hmi.elckerlyc;

import hmi.elckerlyc.pegboard.TimePeg;


/**
 * Syncs + their linked TimePeg
 * @author Herwin
 *
 */
public final class SyncAndTimePeg
{
    public final TimePeg peg;
    public final String sync;
    public final String id;
    public final String bmlId;
    
    public SyncAndTimePeg(String bmlId, String id, String sync, TimePeg p)
    {
        this.sync = sync;
        this.id = id;
        this.bmlId = bmlId;
        this.peg = p;
    }
}
