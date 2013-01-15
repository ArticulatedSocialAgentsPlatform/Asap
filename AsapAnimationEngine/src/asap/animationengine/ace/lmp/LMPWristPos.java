package asap.animationengine.ace.lmp;

import hmi.animation.Hanim;
import hmi.animation.VJoint;
import hmi.math.Mat4f;
import hmi.math.Vec3f;
import hmi.neurophysics.FittsLaw;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import saiba.bml.core.Behaviour;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.ace.CurvedGStroke;
import asap.animationengine.ace.GuidingSequence;
import asap.animationengine.ace.GuidingStroke;
import asap.animationengine.ace.LinearGStroke;
import asap.animationengine.ace.TPConstraint;
import asap.animationengine.procanimation.IKBody;
import asap.math.splines.NUSSpline3;
import asap.math.splines.SparseVelocityDef;
import asap.motionunit.TMUPlayException;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.scheduler.TimePegAndConstraint;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;

/**
 * Local motor program for wrist positioning
 * @author hvanwelbergen
 * @author Stefan Kopp (original C++ version)
 */
@Slf4j
public class LMPWristPos extends LMPPos
{
    private final PegBoard pegBoard;
    private ImmutableSet<String> kinematicJoints;

    private NUSSpline3 spline;
    private AnimationPlayer aniPlayer;
    private IKBody ikBody;
    private String scope;
    private double rightArmStartSwivel;
    private double leftArmStartSwivel;
    private final String baseJoint;

    public void resolveSynchs(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sac) throws BehaviourPlanningException
    {
        linkSynchs(sac);
        resolveTimePegs(bbPeg.getValue());
    }

    public LMPWristPos(String scope, FeedbackManager bbf, BMLBlockPeg bmlBlockPeg, String bmlId, String id, PegBoard pegBoard,
            GuidingSequence gSeq, String baseJoint, AnimationPlayer aniPlayer)
    {
        super(bbf, bmlBlockPeg, bmlId, id, pegBoard);
        this.baseJoint = baseJoint;
        this.gSeq = gSeq;
        this.pegBoard = pegBoard;
        this.aniPlayer = aniPlayer;
        this.ikBody = new IKBody(aniPlayer.getVNext()); // TODO: may also be on additive joint
        this.scope = scope;

        if (scope.equals("left_arm"))
        {
            kinematicJoints = ImmutableSet.of(Hanim.l_shoulder, Hanim.l_elbow);
        }
        else if (scope.equals("right_arm"))
        {
            kinematicJoints = ImmutableSet.of(Hanim.r_shoulder, Hanim.r_elbow);
        }
    }

    public float[] getPosition(double t)
    {
        if (spline != null)
        {
            VJoint vjBase = aniPlayer.getVCurr().getPartBySid(baseJoint);
            float pos[] = spline.getPosition(t);
            float m[] = Mat4f.getMat4f();
            vjBase.getPathTransformMatrix(aniPlayer.getVCurr(), m);
            Mat4f.transformPoint(m,pos);
            return pos;
        }
        else
        {
            return null;
        }
    }

    public float[] getVelocity(double t)
    {
        if (spline != null)
        {
            return spline.getFirstDerivative(t);
        }
        else
        {
            return Vec3f.getVec3f(0, 0, 0);
        }
    }

    private void avoidBodyCollisions(GuidingSequence gSeq, String _scopeStr)
    {
        // TODO: implement this
    }

    @Override
    public List<String> getAvailableSyncs()
    {
        List<String> syncs = super.getAvailableSyncs();
        if (!syncs.contains("strokeStart"))
        {
            syncs.add("strokeStart");
        }
        if (!syncs.contains("strokeEnd"))
        {
            syncs.add("strokeEnd");
        }
        return syncs;
    }

    @Override
    public void startUnit(double time) throws TimedPlanUnitPlayException
    {
        if (gSeq != null && !gSeq.isEmpty())
        {
            gSeq.setStartPos(getGlobalWristPosition());
            gSeq.setST(new TPConstraint(time));
            rightArmStartSwivel = ikBody.getSwivelRightArm();
            leftArmStartSwivel = ikBody.getSwivelLeftArm();
            refine();
        }
        else
        {
            log.warn("LMP_WristPos::activate : trajectory empty or already active!");
        }

        feedback("start", time);
        super.startUnit(time);
    }

    @Override
    public void relaxUnit(double time)
    {

    }

    private void buildTrajectory()
    {
        spline = buildSpline(gSeq);
    }

    private void refine()
    {
        buildTrajectory();

        // TODO
        // 2. -- if overshooting required, create dedicated lmp as successor
        // if ( lmpOVS != 0 )
        // {
        // // determine start time
        // MgcReal startOVS,trajDur = spline->GetMaxTime() - spline->GetMinTime();
        // // only movements that are longer than 0.4 sec own a significant overshooting phase of 0.3 sec!
        // if ( trajDur > 0.4 )
        // {
        // GuidingSequence seq;
        // seq.addGuidingStroke( new LinearGStroke ( GuidingStroke::STP_RETRACT,
        // gSeq->getEndTime(),
        // gSeq->getEndPos() ));
        // // NOTE:
        // // start point of overshooting lmp is determined at activation
        // // time, i.e., we do not need to give an origin for 'seq'!
        // lmpOVS->setGuidingSeq( seq );
        // startOVS = spline->GetMinTime() + 0.85*trajDur; //traj->GetMaxTime() - 0.3;
        // activateSuccessorAt( lmpOVS, startOVS );
        // }
        //
        // // make sure that overshooting is created only once
        // lmpOVS = 0;
        // }
    }

    // TODO Change to this when body collision stuff is implemented
    // private NUSSpline3 buildSpline(GuidingSequence _gSeq, String _scopeStr)

    private NUSSpline3 buildSpline(GuidingSequence _gSeq)
    {
        NUSSpline3 _spline = null;

        if (_gSeq != null && !_gSeq.isEmpty())
        {
            // -- avoid body collisions --
            // TODO
            // avoidBodyCollisions(_gSeq, _scopeStr);

            // cout << ":: building trajectory from:";
            // _gSeq->writeTo(cout); cout << endl;

            // -- build parametric curve for the trajectory --
            List<Double> tv = new ArrayList<>();
            List<float[]> pv = new ArrayList<>();
            List<SparseVelocityDef> vv = new ArrayList<>();

            // set start conds
            pv.add(_gSeq.getStartPos());
            tv.add(_gSeq.getStartTPC().getTime());
            vv.add(new SparseVelocityDef(0, Vec3f.getVec3f(0, 0, 0))); // v

            // MgcVector3 vStart = _gSeq->getStartDirOfStroke(0);
            // vv.push_back(make_pair(0,vStart));

            // complete curvilinear guiding strokes
            // cout << "completing curvilinear strokes..." << endl;
            double sT = getStartTime();
            double prepDur = getTime("strokeStart") - getStartTime();
            _gSeq.getStroke(0).setEDt(prepDur);

            for (int i = 0; i < _gSeq.size(); i++)
            {
                if (_gSeq.getStroke(i) instanceof CurvedGStroke)
                {
                    CurvedGStroke cs = (CurvedGStroke) _gSeq.getStroke(i);
                    cs.formAt(_gSeq.getStartPosOfStroke(i), sT);
                }
                // sT = _gSeq.getStroke(i).getEndTime();
                sT += _gSeq.getStroke(i).getEDt();
            }

            // append guiding strokes
            // cout << "setting up trajectory constraints..." << endl;
            float[] p, v;

            // sT = _gSeq.getStartTPC().getTime();
            sT = getStartTime();

            // double prepDur = getPreparationDuration();

            for (int i = 0; i < _gSeq.size(); i++)
            {
                if (_gSeq.getStroke(i) instanceof LinearGStroke)
                {
                    // cout << "appending linear stroke for t=" << sT << "-"
                    // << _gSeq->getStroke(i)->eT << endl;
                    pv.add(_gSeq.getStroke(i).getEndPos());
                    tv.add(_gSeq.getStroke(i).getEDt() + sT);
                    sT += _gSeq.getStroke(i).getEDt();
                }
                else if (_gSeq.getStroke(i) instanceof CurvedGStroke)
                {
                    // cout << "appending curvilinear stroke for t=" << sT << "-"
                    // << _gSeq->getStroke(i)->eT << endl;
                    CurvedGStroke cs = (CurvedGStroke) _gSeq.getStroke(i);

                    pv.add(cs.getN1()); // cout << pv.back() << endl;

                    tv.add(cs.getFT1());
                    pv.add(cs.getN2()); // cout << pv.back() << endl;
                    tv.add(cs.getFT2());

                    // append stroke end point & velocity
                    pv.add(cs.getEndPos());
                    tv.add(_gSeq.getStroke(i).getEDt() + sT);
                    sT += _gSeq.getStroke(i).getEDt();
                }
                else log.warn("Trajectory::build : unknown stroke type!");
            }

            // building inner velocities
            int j = 0;
            for (int i = 0; i < _gSeq.size(); i++)
            {
                // cout << "end velocity at break point " << j << ":";
                if (_gSeq.getStroke(i) instanceof LinearGStroke) j += 1;
                else if (_gSeq.getStroke(i) instanceof CurvedGStroke) j += 3;
                vv.add(new SparseVelocityDef(j, _gSeq.getStrokeEndVelocityOf(i)));
                // cout << _gSeq->getStrokeEndVelocityOf(i) << endl;
            }

            _spline = new NUSSpline3(4);
            System.out.println("Times: " + Joiner.on(",").join(tv));
            System.out.println("Points: ");
            for (float[] point : pv)
            {
                System.out.print(Vec3f.toString(point) + ",");
            }
            System.out.println("");

            _spline.interpolate3(pv, tv, vv);
        }

        return _spline;
    }

    @Override
    public double getStartTime()
    {
        TimePeg tpStart = pegBoard.getTimePeg(this.getBMLId(), this.getId(), "start");
        if (tpStart != null)
        {
            return tpStart.getGlobalValue();
        }

        return TimePeg.VALUE_UNKNOWN;
    }

    @Override
    public double getEndTime()
    {
        TimePeg tpEnd = pegBoard.getTimePeg(this.getBMLId(), this.getId(), "end");
        if (tpEnd != null)
        {
            return tpEnd.getGlobalValue();
        }

        return TimePeg.VALUE_UNKNOWN;
    }

    @Override
    public double getRelaxTime()
    {
        TimePeg relaxPeg = pegBoard.getTimePeg(getBMLId(), getId(), "relax");

        if (relaxPeg != null && relaxPeg.getGlobalValue() != TimePeg.VALUE_UNKNOWN)
        {
            return pegBoard.getTimePeg(this.getBMLId(), this.getId(), "relax").getGlobalValue();
        }
        else return getEndTime();
    }

    @Override
    public TimePeg getTimePeg(String syncId)
    {
        return pegBoard.getTimePeg(getBMLId(), getId(), syncId);
    }

    @Override
    public void setTimePeg(String syncId, TimePeg peg)
    {
        pegBoard.addTimePeg(getBMLId(), getId(), syncId, peg);
    }

    @Override
    public boolean hasValidTiming()
    {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    protected void playUnit(double time) throws TimedPlanUnitPlayException
    {
        double swivel = 0;
        if (time < getTime("strokeStart"))
        {
            double relT = (time - getStartTime()) / (getTime("strokeStart") - getStartTime());
            if (scope.equals("left_arm"))
            {
                swivel = leftArmStartSwivel + (0 - leftArmStartSwivel) * relT;
            }
            else
            {
                swivel = rightArmStartSwivel + (0 - rightArmStartSwivel) * relT;
            }
        }

        if (time < getTime("strokeEnd"))
        {
            float pos[] = getPosition(time);
            if (scope.equals("left_arm"))
            {
                // ikBody.setSwivelLeftHand(swivel);
                ikBody.setLeftHand(pos);
            }
            else
            {
                // ikBody.setSwivelRightHand(swivel);
                ikBody.setRightHand(pos);
            }
        }
    }

    @Override
    protected void stopUnit(double time) throws TimedPlanUnitPlayException
    {

    }

    @Override
    public Set<String> getKinematicJoints()
    {
        return kinematicJoints;
    }

    @Override
    public Set<String> getPhysicalJoints()
    {
        return ImmutableSet.of();
    }

    private void createMissingTimePegs()
    {
        createPegWhenMissingOnPegBoard("stroke");
        createPegWhenMissingOnPegBoard("ready");
        createPegWhenMissingOnPegBoard("relax");
        createPegWhenMissingOnPegBoard("strokeStart");
        createPegWhenMissingOnPegBoard("strokeEnd");
        createPegWhenMissingOnPegBoard("start");
        createPegWhenMissingOnPegBoard("end");
    }

    private void setInternalStrokeTiming(double time)
    {
        double defaultStrokeDuration = 0;
        GuidingStroke prevGstroke = gSeq.getStroke(0);

        for (int i = 1; i < gSeq.size(); i++)
        {
            defaultStrokeDuration += gSeq.getStroke(i).getEDt();
        }

        if (getTimePeg("strokeStart").getGlobalValue() == TimePeg.VALUE_UNKNOWN
                && getTimePeg("strokeEnd").getGlobalValue() != TimePeg.VALUE_UNKNOWN)
        {
            pegBoard.setPegTime(getBMLId(), getId(), "strokeStart", getTimePeg("strokeEnd").getGlobalValue() - defaultStrokeDuration);
        }

        if (getTimePeg("strokeEnd").getGlobalValue() == TimePeg.VALUE_UNKNOWN
                && getTimePeg("strokeStart").getGlobalValue() != TimePeg.VALUE_UNKNOWN)
        {
            pegBoard.setPegTime(getBMLId(), getId(), "strokeEnd", getTimePeg("strokeStart").getGlobalValue() + defaultStrokeDuration);
        }
    }

    private float[] getGlobalWristPosition()
    {
        VJoint vj = aniPlayer.getVCurr().getPartBySid(getWristJointSid());
        float wristCurr[] = Vec3f.getVec3f();
        vj.getPathTranslation(aniPlayer.getVCurr(), wristCurr);
        return wristCurr;
    }

    private String getWristJointSid()
    {
        String wristJoint = Hanim.l_wrist;
        if (getKinematicJoints().contains(Hanim.r_shoulder))
        {
            wristJoint = Hanim.r_wrist;
        }
        return wristJoint;
    }

    // get preparation duration, given current hand position
    private double getPreparationDuration()
    {
        GuidingStroke gstroke = gSeq.getStroke(0);
        return FittsLaw.getHandTrajectoryDuration(Vec3f.distanceBetweenPoints(gstroke.getEndPos(), getGlobalWristPosition()));
    }

    private void resolveTimePegs(double time)
    {
        createMissingTimePegs();

        // resolve start
        if (getStartTime() == TimePeg.VALUE_UNKNOWN && getTimePeg("strokeStart").getGlobalValue() != TimePeg.VALUE_UNKNOWN)
        {
            pegBoard.setPegTime(getBMLId(), getId(), "start", getTimePeg("strokeStart").getGlobalValue() - getPreparationDuration());
        }
        else if (getTimePeg("strokeStart").getGlobalValue() == TimePeg.VALUE_UNKNOWN && getStartTime() != TimePeg.VALUE_UNKNOWN)
        {
            pegBoard.setPegTime(getBMLId(), getId(), "strokeStart", getStartTime() + getPreparationDuration());
        }
        else if (noPegsSet())
        {
            pegBoard.getTimePeg(getBMLId(), getId(), "start").setValue(0, getBMLBlockPeg());
            pegBoard.getTimePeg(getBMLId(), getId(), "strokeStart").setValue(getPreparationDuration(), getBMLBlockPeg());
        }

        // resolve end
        if (getEndTime() == TimePeg.VALUE_UNKNOWN)
        {
            if (getTimePeg("strokeEnd").getGlobalValue() != TimePeg.VALUE_UNKNOWN)
            {
                pegBoard.setPegTime(getBMLId(), getId(), "end", getTimePeg("strokeEnd").getGlobalValue() + getPreparationDuration());
            }
        }
        else
        {
            if (getTimePeg("strokeEnd").getGlobalValue() == TimePeg.VALUE_UNKNOWN)
            {
                pegBoard.setPegTime(getBMLId(), getId(), "strokeEnd", getEndTime() - getPreparationDuration());
            }
        }

        setInternalStrokeTiming(time);
        if (getStartTime() == TimePeg.VALUE_UNKNOWN)
        {
            pegBoard.setPegTime(getBMLId(), getId(), "start", getTimePeg("strokeStart").getGlobalValue() - getPreparationDuration());
        }
        if (getEndTime() == TimePeg.VALUE_UNKNOWN)
        {
            pegBoard.setPegTime(getBMLId(), getId(), "end", getTimePeg("strokeEnd").getGlobalValue() + getPreparationDuration());
        }

        /*
         * conventions:
         * missing ready => ready = start
         * missing relax => relax = end
         * missing stroke => stroke = strokeStart
         */

        if (getTimePeg("stroke").getGlobalValue() == TimePeg.VALUE_UNKNOWN)
        {
            pegBoard.setPegTime(getBMLId(), getId(), "stroke", getTimePeg("strokeStart").getGlobalValue());
        }

        if (getTimePeg("relax").getGlobalValue() == TimePeg.VALUE_UNKNOWN)
        {
            pegBoard.setPegTime(getBMLId(), getId(), "relax", getTime("strokeEnd"));
        }

        if (getTimePeg("ready").getGlobalValue() == TimePeg.VALUE_UNKNOWN)
        {
            pegBoard.setPegTime(getBMLId(), getId(), "ready", getStartTime());
        }

        if (!isPlaying())
        {
            setTpMinimumTime(time);
        }
    }

    @Override
    public void updateTiming(double time) throws TMUPlayException
    {
        if (!isLurking()) return;
        resolveTimePegs(time);

        // TODO: should do something like resolveTimePegs multiple times, updating the preparation
        // and retraction durations timing at each run.

        // get first timing constraint and guiding stroke
        // TPConstraint startTPC = gSeq.getStartTPC();
        // GuidingStroke gstroke = gSeq.getStroke(0);

        // TODO: implement this(?)
        // if (activateFlag)
        // {
        // gSeq->setStartPos(x);
        // gSeq->setStartTPC(t);
        // //gstroke->sDt = dt;
        //
        // // complete, start execution, and set lmp state
        // refine();
        // updateState(t);
        // //cout << "LMP_WristPos::" << getName() << " defined?: " << isDefined(t) << endl;
        // }
        // else
        // // skip a single-segmented, goal-directed movement if -at the time of activation!-
        // // the target position is already reached = estimated duration is nearly zero
        // if ( t > gSeq->getStartTPC().time && duration < 0.1 && gSeq->size() == 1 ) {
        // //cout << getName() << "::switching to finish since no movement required!" << endl;
        // setState( LMP_Finish );
        // // remove overshooting (if present)
        // removeSuccessor( lmpOVS );
        // }
    }
}
