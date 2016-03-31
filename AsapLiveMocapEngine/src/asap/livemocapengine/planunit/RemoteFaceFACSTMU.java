/*******************************************************************************
 *******************************************************************************/
package asap.livemocapengine.planunit;

import hmi.faceembodiments.AUConfig;
import hmi.faceembodiments.FACSFaceEmbodiment;
import asap.livemocapengine.inputs.FACSFaceInput;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.TimedPlanUnitPlayException;

/**
 * Uses an FACSFaceInput to remotely control a FACSFaceEmbodiment
 * @author welberge
 * 
 */
public class RemoteFaceFACSTMU extends LiveMocapTMU
{
    private final FACSFaceInput faceInput;
    private final FACSFaceEmbodiment faceEmbodiment;

    public RemoteFaceFACSTMU(FACSFaceInput faceInput, FACSFaceEmbodiment faceEmbodiment, FeedbackManager fbm, BMLBlockPeg bmlPeg,
            String bmlId, String behId)
    {
        super(fbm, bmlPeg, bmlId, behId);
        this.faceEmbodiment = faceEmbodiment;
        this.faceInput = faceInput;
    }

    @Override
    protected void playUnit(double time) throws TimedPlanUnitPlayException
    {
        AUConfig[] configs = faceInput.getAUConfigs();
        if (configs != null)
        {
            faceEmbodiment.setAUs(faceInput.getAUConfigs());
        }
    }
}
