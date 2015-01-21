/*******************************************************************************
 *******************************************************************************/
package asap.audioengine;

import hmi.audioenvironment.SoundManager;
import hmi.util.Resources;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import saiba.bml.core.Behaviour;
import asap.bml.ext.bmlt.BMLTAudioFileBehaviour;
import asap.realizer.AbstractPlanner;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.SyncAndTimePeg;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.OffsetPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.PlanManager;
import asap.realizer.scheduler.TimePegAndConstraint;

/**
 * Planner for BMLT audio behaviours
 * @author welberge
 */
public class AudioPlanner extends AbstractPlanner<TimedAbstractAudioUnit>
{
    private Resources audioResource;
    private static Logger logger = LoggerFactory.getLogger(AudioPlanner.class.getName());
    private final SoundManager soundManager;
    private static final double TIMEPEG_TOLERANCE = 0.003;

    public AudioPlanner(FeedbackManager bfm, Resources audioRes, PlanManager<TimedAbstractAudioUnit> planManager, SoundManager soundManager)
    {
        super(bfm, planManager);
        audioResource = audioRes;
        this.soundManager = soundManager;
    }

    private TimedAbstractAudioUnit createAudioUnit(BMLBlockPeg bbPeg, Behaviour b) throws BehaviourPlanningException
    {
        BMLTAudioFileBehaviour bAudio = (BMLTAudioFileBehaviour) b;
        String fileName = bAudio.getStringParameterValue("fileName");
        InputStream inputStream = audioResource.getInputStream(fileName);
        if(inputStream == null)
        {
            try
            {
                inputStream = new URL(fileName).openStream();
            }
            catch (MalformedURLException e)
            {
                throw new BehaviourPlanningException(b,"Cannot find audio file "+bAudio.getStringParameterValue("fileName"),e);
            }
            catch (IOException e)
            {
                throw new BehaviourPlanningException(b,"Cannot find audio file "+bAudio.getStringParameterValue("fileName"),e);
            }
        }
        if(inputStream == null)
        {
            throw new BehaviourPlanningException(b,"Cannot find audio file "+bAudio.getStringParameterValue("fileName"));
        }
        TimedAbstractAudioUnit au = new TimedWavAudioUnit(soundManager, fbManager, bbPeg, inputStream, bAudio.getBmlId(), bAudio.id);
        try
        {
            au.setup();
        }
        catch (AudioUnitPlanningException e)
        {
            throw new BehaviourPlanningException(b, e.getLocalizedMessage(), e);
        }
        logger.debug("Creating audio unit {} duration: {}", b.id, au.getPreferedDuration());
        return au;
    }

    /**
     * Creates a AudioUnit that satisfies sacs and adds it to the audio plan. All registered BMLFeedbackListeners are linked to this AudioUnit.
     */
    @Override
    public List<SyncAndTimePeg> addBehaviour(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sacs, TimedAbstractAudioUnit au)
            throws BehaviourPlanningException
    {
        validateSyncs(sacs, b);
        ArrayList<SyncAndTimePeg> satp = new ArrayList<SyncAndTimePeg>();
        if (au == null)
        {
            au = createAudioUnit(bbPeg, b);
        }
        
        linkStartAndEnd(b, sacs, au);
        if (au.getStartPeg()!=null)
        {
            satp.add(new SyncAndTimePeg(b.getBmlId(), b.id, "start", au.getStartPeg()));
        }
        else
        {
            TimePeg sp = new TimePeg(bbPeg);
            satp.add(new SyncAndTimePeg(b.getBmlId(), b.id, "start",sp));
            au.setStart(sp);
        }
        
        if (au.getEndPeg() != null)
        {
            satp.add(new SyncAndTimePeg(b.getBmlId(), b.id, "end", au.getEndPeg()));            
        }
        else
        {
            TimePeg ep = new TimePeg(bbPeg);
            satp.add(new SyncAndTimePeg(b.getBmlId(), b.id, "end",ep));
            au.setEnd(ep);
        }
        planManager.addPlanUnit(au);

        return satp;
    }

    private void linkStartAndEnd(Behaviour b, List<TimePegAndConstraint> sacs, TimedAbstractAudioUnit au)
    {
        // link start and end sync
        for (TimePegAndConstraint sac : sacs)
        {
            if (sac.syncId.equals("start"))
            {
                if (sac.offset == 0)
                {
                    au.setStart(sac.peg);

                }
                else
                {
                    OffsetPeg p = new OffsetPeg(sac.peg, -sac.offset);
                    au.setStart(p);
                }
            }
            if (sac.syncId.equals("end"))
            {
                if (sac.offset == 0)
                {

                    au.setEnd(sac.peg);
                }
                else
                {
                    OffsetPeg p = new OffsetPeg(sac.peg, -sac.offset);
                    au.setEnd(p);
                }
            }
        }
    }

    private void validateSyncs(List<TimePegAndConstraint> sacs, Behaviour b) throws BehaviourPlanningException
    {
        for (TimePegAndConstraint sac : sacs)
        {
            if (!sac.syncId.equals("start") && !sac.syncId.equals("end"))
            {
                throw new BehaviourPlanningException(b, "Attempting to synchronize a audiofile behaviour with sync: " + sac.syncId
                        + " other than start or end");
            }
        }
    }

    @Override
    public TimedAbstractAudioUnit resolveSynchs(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sacs) throws BehaviourPlanningException
    {
        TimedAbstractAudioUnit au = createAudioUnit(bbPeg, b);
        double startTime = bbPeg.getValue();
        boolean startFound = false;

        validateSyncs(sacs, b);

        // resolve start time
        for (TimePegAndConstraint sac : sacs)
        {
            if (sac.syncId.equals("start") && sac.peg.getGlobalValue() != TimePeg.VALUE_UNKNOWN)
            {
                startTime = sac.peg.getGlobalValue() - sac.offset;
                startFound = true;
            }
        }

        if (!startFound)
        {
            for (TimePegAndConstraint sac : sacs)
            {
                if (sac.peg.getGlobalValue() != TimePeg.VALUE_UNKNOWN)
                {
                    if (sac.syncId.equals("end") && sac.peg.getGlobalValue() != TimePeg.VALUE_UNKNOWN)
                    {
                        startTime = sac.peg.getGlobalValue() - au.getPreferedDuration() - sac.offset;
                        break;
                    }
                }
            }
        }

        // resolve start and end
        TimePegAndConstraint sacNotStart = null;
        for (TimePegAndConstraint sac : sacs)
        {
            if (!sac.syncId.equals("start"))
            {
                sacNotStart = sac;
                break;
            }
        }

        for (TimePegAndConstraint sac : sacs)
        {
            if (sac.syncId.equals("end"))
            {
                if (sac.peg.getGlobalValue() == TimePeg.VALUE_UNKNOWN)
                {
                    sac.peg.setGlobalValue(au.getPreferedDuration() + startTime + sac.offset);
                }
                else if (Math.abs(sac.peg.getGlobalValue() - (startTime + au.getPreferedDuration() + sac.offset)) > TIMEPEG_TOLERANCE)
                {
                    throw new BehaviourPlanningException(b, "Stretching audio fragments is not supported yet. "
                            + "Should not be too hard to do, though." + " Behavior omitted.");
                }
            }

            if (sac.syncId.equals("start") && sac.peg.getGlobalValue() == TimePeg.VALUE_UNKNOWN)
            {
                if (sac.resolveAsStartOffset)
                {
                    OffsetPeg p = (OffsetPeg) sac.peg;
                    p.setLink(sacNotStart.peg);
                    p.setOffset(startTime - sacNotStart.peg.getGlobalValue());
                }
                else
                {
                    sac.peg.setGlobalValue(startTime + sac.offset);
                }
            }
        }
        linkStartAndEnd(b, sacs, au);
        return au;
    }

    @Override
    public List<Class<? extends Behaviour>> getSupportedBehaviours()
    {
        List<Class<? extends Behaviour>> list = new ArrayList<Class<? extends Behaviour>>();
        list.add(BMLTAudioFileBehaviour.class);
        return list;
    }

    @Override
    public List<Class<? extends Behaviour>> getSupportedDescriptionExtensions()
    {
        List<Class<? extends Behaviour>> list = new ArrayList<Class<? extends Behaviour>>();
        list.add(BMLTAudioFileBehaviour.class);
        return list;
    }

    @Override
    public double getRigidity(Behaviour beh)
    {
        return 1;
    }
    
    @Override
    public void shutdown()
    {
        for(TimedAbstractAudioUnit au:planManager.getPlanUnits())
        {
            au.cleanup();
        }
    }
}
