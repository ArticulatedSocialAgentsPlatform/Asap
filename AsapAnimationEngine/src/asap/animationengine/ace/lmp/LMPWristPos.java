package asap.animationengine.ace.lmp;

import hmi.animation.Hanim;
import hmi.math.Vec3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import saiba.bml.core.Behaviour;

import com.google.common.collect.ImmutableSet;

import lombok.extern.slf4j.Slf4j;
import asap.animationengine.ace.CurvedGStroke;
import asap.animationengine.ace.GuidingSequence;
import asap.animationengine.ace.GuidingStroke;
import asap.animationengine.ace.LinearGStroke;
import asap.animationengine.ace.TPConstraint;
import asap.math.splines.NUSSpline3;
import asap.math.splines.SparseVelocityDef;
import asap.motionunit.TMUPlayException;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.scheduler.LinearStretchResolver;
import asap.realizer.scheduler.TimePegAndConstraint;
import asap.realizer.scheduler.UniModalResolver;

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

    private final UniModalResolver resolver = new LinearStretchResolver();
    
    public void resolveSynchs(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sac)throws BehaviourPlanningException
    {
        resolver.resolveSynchs(bbPeg, b, sac, this);
    }
    
    public LMPWristPos(String scope, FeedbackManager bbf, BMLBlockPeg bmlBlockPeg, String bmlId, String id, PegBoard pegBoard)
    {
        super(bbf, bmlBlockPeg, bmlId, id, pegBoard);
        this.pegBoard = pegBoard;
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
            return spline.getPosition(t);
        }
        else
        {
            return Vec3f.getVec3f(0, 0, 0);
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
    public void startUnit(double time)
    {
        if (gSeq != null && !gSeq.isEmpty())
        {
            // get first timing constraint and guiding stroke
            TPConstraint startTPC = gSeq.getStartTPC();
            GuidingStroke gstroke = gSeq.getStroke(0);

            // TODO: implement this
            // // determine the duration of the currently needed prep movement
            // duration = getPosDurationFromAmplitude((x - gstroke.getEndPos()).Length());
            //
            // // check whether movement needs to start now or not yet
            // activateFlag = false;
            // if (tActivation > -1) {
            // activateFlag = (tActivation <= t);
            // }
            // else
            // // or activate just in time for target position(s)? --
            // activateFlag = (( startTPC.mode == TPConstraint::Rigorous && startTPC.time <= t ) ||
            // ( startTPC.mode != TPConstraint::Rigorous && (gstroke->eT.time - t) <= duration ));
            //
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
        else
        {
            log.warn("LMP_WristPos::activate : trajectory empty or already active!");
        }
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
            tv.add( _gSeq.getStartTPC().getTime());
            vv.add(new SparseVelocityDef(0, Vec3f.getVec3f(0, 0, 0))); // v

            // MgcVector3 vStart = _gSeq->getStartDirOfStroke(0);
            // vv.push_back(make_pair(0,vStart));

            // complete curvilinear guiding strokes
            // cout << "completing curvilinear strokes..." << endl;
            double sT = _gSeq.getStartTPC().getTime();
            for (int i = 0; i < _gSeq.size(); i++)
            {
                if (_gSeq.getStroke(i) instanceof CurvedGStroke)
                {
                    CurvedGStroke cs = (CurvedGStroke) _gSeq.getStroke(i);
                    cs.formAt(_gSeq.getStartPosOfStroke(i), sT);
                }
                sT = _gSeq.getStroke(i).getEndTime();
            }

            // append guiding strokes
            // cout << "setting up trajectory constraints..." << endl;
            float[] p, v;
            for (int i = 0; i < _gSeq.size(); i++)
            {
                sT = tv.get(tv.size() - 1);
                if (_gSeq.getStroke(i) instanceof LinearGStroke)
                {
                    // cout << "appending linear stroke for t=" << sT << "-"
                    // << _gSeq->getStroke(i)->eT << endl;
                    pv.add(_gSeq.getStroke(i).getEndPos());
                    tv.add( _gSeq.getStroke(i).getEndTime());
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
                    pv.add(cs.getEndPos()); // cout << pv.back() << endl;
                    tv.add( cs.getEndTime());
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
        // TODO Auto-generated method stub

    }

    @Override
    protected void stopUnit(double time) throws TimedPlanUnitPlayException
    {
        // TODO Auto-generated method stub

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

    @Override
    public void updateTiming(double time) throws TMUPlayException
    {
        // TODO Auto-generated method stub

    }
}
