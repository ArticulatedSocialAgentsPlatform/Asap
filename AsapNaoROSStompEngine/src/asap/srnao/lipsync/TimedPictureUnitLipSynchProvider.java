/*******************************************************************************
 *******************************************************************************/
package asap.srnao.lipsync;

import hmi.tts.Visime;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import saiba.bml.core.Behaviour;
import asap.realizer.feedback.NullFeedbackManager;
import asap.realizer.lipsync.LipSynchProvider;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.OffsetPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.ParameterException;
import asap.realizer.planunit.PlanManager;
import asap.realizer.planunit.TimedPlanUnit;
import asap.srnao.display.PictureDisplay;
import asap.srnao.planunit.AddAnimationXMLPU;
import asap.srnao.planunit.NUPrepareException;
import asap.srnao.planunit.TimedNaoUnit;

/**
 * LipSynchProvider implementation for the picture engine. This very rudimentary
 * lipsync uses no speech binding and does not link different visemes to
 * different pictures/animations. This is due to the absence of viseme
 * information in the Android TTS engine.
 *
 * @author Jordi Hendrix
 */
public class TimedPictureUnitLipSynchProvider implements LipSynchProvider {

    private final PlanManager<TimedNaoUnit> picturePlanManager;
    private static Logger logger = LoggerFactory.getLogger(TimedPictureUnitLipSynchProvider.class.getName());
    private PictureDisplay display;
    private String filePath;
    private String fileName;

    public TimedPictureUnitLipSynchProvider(PlanManager<TimedNaoUnit> picturePlanManager,
            PictureDisplay display, String filePath, String fileName) {
        logger.debug("TimedPictureUnitLipSynchProvider has been created.");
        this.picturePlanManager = picturePlanManager;
        this.display = display;
        this.filePath = filePath;
        this.fileName = fileName;
    }

    @Override
    public void addLipSyncMovement(BMLBlockPeg bbPeg, Behaviour beh, TimedPlanUnit bs, List<Visime> visemes) {
        double totalDuration = 0d;
        logger.debug("addLipSyncMovement has been called.");

        // Calculate total duration of speech
        for (Visime vis : visemes) {
            totalDuration += vis.getDuration();
        }

        // Create first AddAnimationXMLPU to determine preferred duration of animation
        AddAnimationXMLPU pu = new AddAnimationXMLPU();
        pu.setDisplay(display);
        try {
            pu.setParameterValue("filePath", filePath);
            pu.setParameterValue("fileName", fileName);
            pu.setParameterValue("layer", "12");
        } catch (ParameterException pe) {
            logger.error("Illegal xml file specified for PictureUnit lipsync.");
            return;
        }
        try {
            pu.prepareImages();
        } catch (NUPrepareException ex) {
            logger.error("XML lipsync animation file is invalid.");
            return;
        }

        // Convert duration to seconds
        totalDuration = totalDuration / 1000d;
        
        // Calculate amount of repetitions within speech duration
        int reps = (int) (totalDuration / pu.getPreferedDuration());
        
        logger.debug("trying to play: {} in {} reps", bs.getBMLId(), reps);
        logger.debug("adjusted anim length to {} to fit total duration of {}", (totalDuration / reps), totalDuration);

        // Create AddAnimationXMLPU for each repetition
        AddAnimationXMLPU newPU;
        for (int i = 0; i < reps; i++) {
            // Create plan unit
            newPU = (AddAnimationXMLPU) pu.copy(display);
            TimedNaoUnit tpu = newPU.createTNU(NullFeedbackManager.getInstance(), bbPeg, beh.getBmlId(), beh.id);
            tpu.setSubUnit(true);
            picturePlanManager.addPlanUnit(tpu);
            
            // Set timing info
            TimePeg startPeg = new OffsetPeg(bs.getTimePeg("start"), i * (totalDuration / reps));
            tpu.setTimePeg(tpu.getKeyPosition("start"), startPeg);
            TimePeg endPeg = new OffsetPeg(bs.getTimePeg("start"), (i + 1) * (totalDuration / reps));
            tpu.setTimePeg(tpu.getKeyPosition("end"), endPeg);
            logger.debug("Adding lip movement animation at {}-{}", tpu.getStartTime(), tpu.getEndTime());
        }
    }
}
