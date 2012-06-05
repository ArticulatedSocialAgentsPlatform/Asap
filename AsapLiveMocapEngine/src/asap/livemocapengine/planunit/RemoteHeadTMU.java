package asap.livemocapengine.planunit;

import asap.livemocapengine.inputs.EulerInput;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.utils.EulerHeadEmbodiment;

/**
 * Uses an EulerInput to remotely control a head embodiment
 * @author welberge
 */
public class RemoteHeadTMU extends LiveMocapTMU
{
    private final EulerHeadEmbodiment headEmbodiment;
    private final EulerInput headInput;


    public RemoteHeadTMU(EulerInput headInput, EulerHeadEmbodiment headEmbodiment, FeedbackManager fbm, BMLBlockPeg bmlPeg, String bmlId,
            String behId)
    {
        super(fbm, bmlPeg, bmlId, behId);
        this.headEmbodiment = headEmbodiment;
        this.headInput = headInput;        
    }

    @Override
    protected void playUnit(double time) throws TimedPlanUnitPlayException
    {
        headEmbodiment.setHeadRollPitchYawDegrees(headInput.getRollDegrees(), headInput.getPitchDegrees(), headInput.getYawDegrees());
    }
}
