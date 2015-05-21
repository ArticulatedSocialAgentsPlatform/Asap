/*******************************************************************************
 *******************************************************************************/
package asap.livemocapengine.planunit;

import hmi.headandgazeembodiments.GazeEmbodiment;
import asap.livemocapengine.inputs.EulerInput;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.TimedPlanUnitPlayException;

/**
 * Gazes at a position provided by a PositionSensor
 * @author welberge
 *
 */
public class RemoteGazeTMU extends LiveMocapTMU
{
    private final EulerInput eulerInput;
    private final GazeEmbodiment gazeEmbodiment;
    
    public RemoteGazeTMU(EulerInput gazeinput, GazeEmbodiment embodiment, FeedbackManager fbm, BMLBlockPeg bmlPeg, String bmlId, String behId)
    {
        super(fbm, bmlPeg, bmlId, behId);
        this.eulerInput = gazeinput;
        this.gazeEmbodiment = embodiment;   
    }

    @Override
    protected void playUnit(double time) throws TimedPlanUnitPlayException
    {
        gazeEmbodiment.setGazeRollPitchYawDegrees(eulerInput.getRollDegrees(), eulerInput.getPitchDegrees(), eulerInput.getYawDegrees());
    }
}
