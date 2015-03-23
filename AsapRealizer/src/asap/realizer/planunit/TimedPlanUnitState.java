/*******************************************************************************
 *******************************************************************************/
package asap.realizer.planunit;

/**
 * Phases from:
 * Kopp, Stefan and Wachsmuth, Ipke, 
 * Synthesizing multimodal utterances for conversational agents, 
 * in: Comput. Animat. Virtual Worlds, 15:1(39--52)
 * @author welberge
 */
public enum TimedPlanUnitState
{
    IN_PREP,    //being planned
    PENDING,    //IN_PREP completed
    LURKING,    //ready to be executed
    IN_EXEC,    //playing
    SUBSIDING,  //in retraction phase
    DONE;       //finished playing
    
    public boolean isPlaying()
    {
        return this == IN_EXEC || this == SUBSIDING;
    }
    
    public boolean isSubsiding()
    {
        return this == SUBSIDING;
    }
    
    public boolean isSubsidingOrDone()
    {
        return this == SUBSIDING || this == DONE;
    }
    
    public boolean isDone()
    {
        return this == DONE;
    }
    
    public boolean isLurking()
    {
        return this == LURKING;
    }
    
    public boolean isInExec()
    {
        return this == IN_EXEC;
    }
    
    public boolean isPending()
    {
        return this == PENDING;
    }
    
    public boolean isInPrep()
    {
        return this == IN_PREP;
    }
}
