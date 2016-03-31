/*******************************************************************************
 *******************************************************************************/
package asap.bml.ext.bmla.builder;

import asap.bml.ext.bmla.BMLATrajectory;

/**
 * Builds a BMLATrajectory
 * @author Herwin
 *
 */
public class BMLATrajectoryBuilder
{
    private final String type, targetValue;
    private String initialValue = "";
    
    public BMLATrajectoryBuilder(String type, String targetValue)
    {
        this.type = type;
        this.targetValue = targetValue;        
    }
    
    public BMLATrajectoryBuilder initialValue(String initialValue)
    {
        this.initialValue = initialValue;
        return this;
    }
    
    public BMLATrajectory build()
    {
        BMLATrajectory traj = new BMLATrajectory();
        traj.targetValue = targetValue;
        traj.type = type;
        traj.initialValue = initialValue;
        return traj;
    }
}
