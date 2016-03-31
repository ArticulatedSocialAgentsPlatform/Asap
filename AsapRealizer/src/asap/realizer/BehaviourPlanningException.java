/*******************************************************************************
 *******************************************************************************/
package asap.realizer;

import saiba.bml.core.Behaviour;

/**
 * Thrown by Planners when a behavior cannot be constructed 
 * @author Herwin
 */
public final class BehaviourPlanningException extends Exception
{
    private static final long serialVersionUID = 1L;
    private final Behaviour behaviour;
    
    
    public Behaviour getBehaviour()
    {
        return behaviour;
    }

    public BehaviourPlanningException(Behaviour b, String m, Exception ex)
    {
        this(b,m+"\n"+ex.getMessage());
        initCause(ex);
    }
    
    public BehaviourPlanningException(Behaviour b, String m)
    {
        super(m);
        behaviour = b;
    }
}
