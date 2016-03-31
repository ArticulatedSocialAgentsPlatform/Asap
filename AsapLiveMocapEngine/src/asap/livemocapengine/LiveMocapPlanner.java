/*******************************************************************************
 *******************************************************************************/
package asap.livemocapengine;

import hmi.faceembodiments.FACSFaceEmbodiment;
import hmi.headandgazeembodiments.EulerHeadEmbodiment;
import hmi.headandgazeembodiments.GazeEmbodiment;

import java.util.List;

import saiba.bml.BMLInfo;
import saiba.bml.core.Behaviour;
import asap.livemocapengine.binding.NameTypeBinding;
import asap.livemocapengine.bml.LiveMocapBehaviour;
import asap.livemocapengine.bml.RemoteFaceFACSBehaviour;
import asap.livemocapengine.bml.RemoteGazeBehaviour;
import asap.livemocapengine.bml.RemoteHeadBehaviour;
import asap.livemocapengine.inputs.EulerInput;
import asap.livemocapengine.inputs.FACSFaceInput;
import asap.livemocapengine.inputs.PositionInput;
import asap.livemocapengine.planunit.LiveMocapTMU;
import asap.livemocapengine.planunit.RemoteFaceFACSTMU;
import asap.livemocapengine.planunit.RemoteGazeTMU;
import asap.livemocapengine.planunit.RemoteHeadTMU;
import asap.realizer.AbstractPlanner;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.SyncAndTimePeg;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.OffsetPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.PlanManager;
import asap.realizer.scheduler.TimePegAndConstraint;

import com.google.common.collect.ImmutableList;

/**
 * A planner for LiveMocapBehaviours
 * @author welberge
 * 
 */
public class LiveMocapPlanner extends AbstractPlanner<LiveMocapTMU>
{
    static
    {
        BMLInfo.addBehaviourType(RemoteHeadBehaviour.xmlTag(), RemoteHeadBehaviour.class);        
        BMLInfo.addBehaviourType(RemoteFaceFACSBehaviour.xmlTag(), RemoteFaceFACSBehaviour.class);
        BMLInfo.addBehaviourType(RemoteGazeBehaviour.xmlTag(), RemoteGazeBehaviour.class);
    }
    
    private final NameTypeBinding inputBinding;
    private final NameTypeBinding outputBinding;
    
    public LiveMocapPlanner(FeedbackManager fbm, PlanManager<LiveMocapTMU> planManager
            , NameTypeBinding inputBinding, NameTypeBinding outputBinding)
    {
        super(fbm, planManager);
        this.inputBinding = inputBinding;
        this.outputBinding = outputBinding;
    }

    private LiveMocapTMU createLiveMocapTMU(LiveMocapBehaviour b, FeedbackManager fbm, BMLBlockPeg bmlPeg) throws BehaviourPlanningException
    {
        if(b instanceof RemoteHeadBehaviour)
        {
            EulerInput input = inputBinding.get(b.getStringParameterValue("input"), EulerInput.class);
            if(input == null)
            {
                throw new BehaviourPlanningException(b,"No input found that matches "+b.getStringParameterValue("input")+","+EulerInput.class);
            }
            EulerHeadEmbodiment output = outputBinding.get(b.getStringParameterValue("output"), EulerHeadEmbodiment.class);
            if(output == null)
            {
                throw new BehaviourPlanningException(b,"No output found that matches "+b.getStringParameterValue("output")+","+EulerHeadEmbodiment.class);
            }
            return new RemoteHeadTMU(input,output, fbm, bmlPeg, b.getBmlId(),b.id);
        }
        if(b instanceof RemoteFaceFACSBehaviour)
        {
            FACSFaceInput input = inputBinding.get(b.getStringParameterValue("input"), FACSFaceInput.class);
            if(input == null)
            {
                throw new BehaviourPlanningException(b,"No input found that matches "+b.getStringParameterValue("input")+","+FACSFaceInput.class);
            }
            FACSFaceEmbodiment output = outputBinding.get(b.getStringParameterValue("output"), FACSFaceEmbodiment.class);
            if(output == null)
            {
                throw new BehaviourPlanningException(b,"No output found that matches "+b.getStringParameterValue("output")+","+FACSFaceEmbodiment.class);
            }
            return new RemoteFaceFACSTMU(input,output, fbm, bmlPeg, b.getBmlId(),b.id);
        }
        if(b instanceof RemoteGazeBehaviour)
        {
        	EulerInput input = inputBinding.get(b.getStringParameterValue("input"), EulerInput.class);
            if(input == null)
            {
                throw new BehaviourPlanningException(b,"No input found that matches "+b.getStringParameterValue("input")+","+PositionInput.class);
            }
            GazeEmbodiment output = outputBinding.get(b.getStringParameterValue("output"), GazeEmbodiment.class);
            if(output == null)
            {
                throw new BehaviourPlanningException(b,"No output found that matches "+b.getStringParameterValue("output")+","+GazeEmbodiment.class);
            }
            return new RemoteGazeTMU(input, output, fbm, bmlPeg, b.getBmlId(), b.id);
        }
        return null;
    }
    
    private void validateSacs(Behaviour b, List<TimePegAndConstraint> sacs) throws BehaviourPlanningException
    {
        if (sacs.size() > 2)
        {
            throw new BehaviourPlanningException(b, "Behaviour "+b+"has > 2 constraints");
        }
        for (TimePegAndConstraint sac : sacs)
        {
            if (!sac.syncId.equals("start") && !sac.syncId.equals("end"))
            {
                throw new BehaviourPlanningException(b, "ParameterValueChange behavior " + b
                        + " has a synchronization constraint other than start or end, on sync " + sac.syncId);
            }
        }
    }
    
    private void setupPegs(LiveMocapTMU lmu, List<TimePegAndConstraint> sac)
    {
        if (getSacStart(sac) != null)
        {
            TimePeg start;
            if (getSacStart(sac).offset == 0)
            {
                start = getSacStart(sac).peg;
            }
            else
            {
                start = new OffsetPeg(getSacStart(sac).peg, -getSacStart(sac).offset);
            }
            lmu.setStartPeg(start);
            if (start.getGlobalValue() == TimePeg.VALUE_UNKNOWN)
            {
                start.setLocalValue(0);
            }
        }
        
        if (getSacEnd(sac) != null)
        {
            TimePeg end;
            if (getSacEnd(sac).offset == 0)
            {
                end = getSacEnd(sac).peg;
            }
            else
            {
                end = new OffsetPeg(getSacEnd(sac).peg, -getSacEnd(sac).offset);
            }
            lmu.setEndPeg(end);
        }        
    }
    
    @Override
    public List<SyncAndTimePeg> addBehaviour(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sacs, LiveMocapTMU planElement)
            throws BehaviourPlanningException
    {
        if(planElement==null)
        {
            planElement = createLiveMocapTMU((LiveMocapBehaviour)b, fbManager, bbPeg);
        }
        planManager.addPlanUnit(planElement);
        validateSacs(b,sacs);
        setupPegs(planElement,sacs);
        
        return constructSyncAndTimePegs(bbPeg,b,planElement);
    }

    @Override
    public LiveMocapTMU resolveSynchs(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sacs) throws BehaviourPlanningException
    {
        LiveMocapTMU lmu = createLiveMocapTMU((LiveMocapBehaviour)b, fbManager, bbPeg);
        validateSacs(b,sacs);
        setupPegs(lmu,sacs);
        return lmu;
    }

    @Override
    public List<Class<? extends Behaviour>> getSupportedBehaviours()
    {
        //@formatter:off        
        return new ImmutableList.Builder<Class<? extends Behaviour>>()
            .add(RemoteHeadBehaviour.class)
            .add(RemoteFaceFACSBehaviour.class)
            .add(RemoteGazeBehaviour.class)
            .build();
        //@formatter:on
    }

    @Override
    public List<Class<? extends Behaviour>> getSupportedDescriptionExtensions()
    {
        return ImmutableList.of();
    }

    @Override
    public double getRigidity(Behaviour beh)
    {
        return 0;
    }

}
