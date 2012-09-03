package asap.animationengine.gesturebinding;

import hmi.math.Quat4f;
import hmi.math.Vec3f;
import hmi.neurophysics.FittsLaw;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.ace.CurvedGStroke;
import asap.animationengine.ace.GStrokePhaseID;
import asap.animationengine.ace.GuidingSequence;
import asap.animationengine.ace.GuidingStroke;
import asap.animationengine.ace.LinearGStroke;
import asap.animationengine.ace.OrientConstraint;
import asap.animationengine.ace.PoConstraint;
import asap.animationengine.ace.TPConstraint;
import asap.animationengine.ace.lmp.LMPPoRot;
import asap.animationengine.ace.lmp.LMPWristPos;
import asap.animationengine.ace.lmp.LMPWristRot;
import asap.animationengine.ace.lmp.MotorControlProgram;
import asap.animationengine.keyframe.MURMLKeyframeMU;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.hns.Hns;
import asap.hns.ShapeSymbols;
import asap.motionunit.keyframe.KeyFrame;
import asap.motionunit.keyframe.LinearQuatFloatInterpolator;
import asap.murml.Dynamic;
import asap.murml.DynamicElement;
import asap.murml.DynamicElement.Type;
import asap.murml.Frame;
import asap.murml.JointValue;
import asap.murml.Keyframing;
import asap.murml.MURMLDescription;
import asap.murml.Parallel;
import asap.murml.Phase;
import asap.murml.Posture;
import asap.murml.Slot;
import asap.murml.Static;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.timemanipulator.EaseInEaseOutManipulator;

import com.google.common.collect.ImmutableList;

/**
 * Creates an animation unit from a MURML description
 * @author hvanwelbergen
 */
@Slf4j
public final class MURMLMUBuilder
{
    private final Hns hns;

    public MURMLMUBuilder(Hns hns)
    {
        this.hns = hns;
    }

    public AnimationUnit setup(String murml)
    {
        MURMLDescription def = new MURMLDescription();
        def.readXML(murml);
        return setup(def);
    }

    /**
     * return start position [0..2] and swivel [3] of the wrist for this movement constraint in
     * body root coordinates.
     */
    private boolean getStartConf(float[] result, DynamicElement elem, Hns hns)
    {
        float startconf[] = new float[4];
        startconf[3] = 0;
        return hns.getHandLocation(elem.getName("start"), result);
    }

    /*
     * Vorgehen:
     * Die Segmente werden zusammenhaengend(!) ausgefuehrt. Dazu wird eine
     * GuidingSequence erstellt. Ist die geschaetzte Dauer der Sequenz
     * kuerzer als die Strokezeit, dann wird ein post-stroke-hold am Ende
     * eingefuegt, d.h. die Retraction wird eigens ausgefuehrt.
     * 
     * Timing-Strategien bei insgesamt vorgegebener Start- und Endzeit:
     * (BEACHTE: Startzeit harter Constraint, Endzeit weniger!)
     * 1. Lokale Isochronitaet
     * 2. Segment-Dauer abhaengig von Segment-Laenge
     * => Skalierungsstrategie?!
     */
    private void appendSubTrajectory(List<DynamicElement> elements, String scope, GuidingSequence traj, TimedAnimationUnit tmu,
            FeedbackManager bbf, BMLBlockPeg bmlBlockPeg, String bmlId, String id, PegBoard pegBoard, MotorControlProgram mp)
    {
        float[] ePos = Vec3f.getVec3f();
        double tEst = 0;
        float swivel = -99;
        List<GuidingStroke> sSeq = new ArrayList<>();

        float sPos[] = traj.getEndPos();
        double sT, eT = traj.getEndTime();
        float[] dir = Vec3f.getVec3f();

        tEst = eT;

        for (DynamicElement segment : elements)
        {
            sT = eT;
            // TODO
            // eT = segment.getEndTPC().time;

            // -- create guiding strokes for linear movement segment --------------------------------------
            if (segment.getType() == Type.LINEAR)
            {
                // cout << "appending linear segment" << endl;
                double d = 0;

                // linear movement given absolutely in terms of start and end points
                if (hns.getHandLocation(segment.getName("end"), ePos))
                {
                    Vec3f.sub(dir, ePos, sPos);
                    Vec3f.normalize(dir);
                }
                // linear movement given in terms of start point end direction
                else if (hns.getAbsoluteDirection(segment.getName("direction"), dir))
                {
                    Vec3f.normalize(dir);
                    double dist = hns.getDistance(segment.getName("disance"));

                    if (dist > 0)
                    {
                        d = dist;
                    }
                    else
                    {

                        d = hns.getDistance("DistNorm");
                    }
                    // ePos = sPos + d*dir;
                    float tmp[] = Vec3f.getVec3f(dir);
                    Vec3f.scale((float) d, tmp);
                    Vec3f.add(ePos, sPos, tmp);
                }
                // no valid definition, ignoring movement segment
                else
                {
                    log.warn("ArmMotorControl::appendSubtrajectory : invalid definition of for linear segment");
                    return;
                }

                // -- create guiding stroke, estimate duration and add to subtrajectory

                // cout << "NEW LINEAR Guiding Stroke to " << eT << ":" << ePos << endl;

                LinearGStroke lgs = new LinearGStroke(GStrokePhaseID.STP_STROKE, new TPConstraint(eT), ePos);
                lgs.setEDt(FittsLaw.getHandTrajectoryDuration(lgs.getArcLengthFrom(sPos)));
                tEst += lgs.getEDt();
                // cout << "est. duration=" << lgs->eDt << " -> est. end time=" << tEst << endl;

                sSeq.add(lgs);
                sPos = lgs.getEndPos();
            }

            // -- create guiding strokes for curvilinear movement segment ---------------------
            else if (segment.getType() == Type.CURVE)
            {
                float[] nVec = Vec3f.getVec3f();
                ShapeSymbols shape = ShapeSymbols.RightS;

                // parse obligatory parameters
                boolean valid = true;

                if (!hns.getHandLocation(segment.getName("end"), ePos))
                {
                    log.warn("ArmMotorControl::appendSubtrajectory : invalid parameter end={} for curved guiding stroke!",
                            segment.getName("end"));
                    return;
                }

                if (!hns.getAbsoluteDirection(segment.getName("normal"), nVec))
                {
                    log.warn("ArmMotorControl::appendSubtrajectory : invalid parameter normal={} for curved guiding stroke!",
                            segment.getName("normal"));
                    return;
                }

                /*
                 * if ( vn = segment->getValueFor("shape") ) {
                 * 
                 * // shape
                 * valid &= figure->HNStranslator.getElementShape( vn->getValue(), shape );
                 * }
                 */
                shape = hns.getElementShape(segment.getName("shape"));

                // parse optional parameters
                float extent = 0.2f;
                float roundness = 0f;
                float skewedness = 0f;
                if (segment.getName("extent") != null)
                {
                    extent = (float) hns.getElementExtent(segment.getName("extent"));
                }
                if (segment.getName("roundness") != null)
                {
                    roundness = (float) hns.getElementRoundness(segment.getName("roundness"));
                }
                if (segment.getName("skewedness") != null)
                {
                    roundness = (float) hns.getElementRoundness(segment.getName("skewedness"));
                }

                // create curved stroke
                CurvedGStroke cgs = new CurvedGStroke(GStrokePhaseID.STP_STROKE, new TPConstraint(eT), ePos, nVec, shape, extent,
                        roundness, skewedness);
                // build geometric parameters for given start position
                // (necessary for estimating duration!)
                cgs.formAt(sPos, sT);
                // cgs->vGain = 0.;

                // -- stroke time smaller than affiliate duration, i.e., post-stroke hold required?
                cgs.setEDt(FittsLaw.getHandTrajectoryDuration(cgs.getArcLengthFrom(sPos)));

                tEst += cgs.getEDt();
                sSeq.add(cgs);
                sPos = cgs.getEndPos();

                /*
                 * TODO: handle chops. In addition to the code below, some data from utterance_visitors_ChopResolver needs to be inserted
                 * // if curve is a chop, force swivel to be 0
                 * if (segment->hasAttribute("chop"))
                 * {
                 * swivel = 0;
                 * }
                 */

            }

        }

        // -- append subtrajectory to overall guiding sequence
        if (!sSeq.isEmpty())
        {
            // -- intra-stroke scheduling:
            // estimated stroke duration smaller than affiliate duration,
            // i.e. post-stroke hold required?
            // cout << "required time:" << tEst << " < " << eT << "?" << endl;
            if (tEst < eT)
            {
                // cout << "yes -> re-scheduling!";
                // -- re-schedule all inner elements and append to subtrajectory
                double ssT = traj.getEndTime();
                for (int i = 0; i < sSeq.size(); i++)
                {
                    // cout << ssT << "->";
                    ssT += sSeq.get(i).getEDt();
                    // cout << ssT << endl;
                    sSeq.get(i).setET(new TPConstraint(ssT));
                    traj.addGuidingStroke(sSeq.get(i));
                }

                // -- create respective motor program
                // cout << "creating lmp from:"; traj.writeTo(cout); cout << endl;

                // TODO: implement proper scope selection when no scope is provided. I think this is handled through MotorControlFigure::suggestMotorScopeFor in ACE.
                if (scope == null)
                {
                    scope = "left_arm";
                }
                if (!(scope.equals("left_arm") || scope.equals("right_arm")))
                {
                    scope = "left_arm";
                }
                LMPWristPos wristMove = new LMPWristPos(scope, bbf, bmlBlockPeg, bmlId, id, pegBoard);

                // XXX needed?
                // wristMove->setBaseFrame( mp->getBase2Root() );

                wristMove.setGuidingSeq(traj);
                if (tmu == null)
                {
                    mp.addLMP(wristMove);
                }
                else
                {
                    // TODO: solve this with TimePegs
                    // lmp->activateSuccessorAt( wristMove, traj.getStartTPC() );
                }
                tmu = wristMove;

                // -- reset guiding sequence for subsequent movement phase(s)
                traj.clear();
                traj.setST(new TPConstraint(eT, TPConstraint.Mode.Rigorous));
                traj.setStartPos(ePos);

                // XXX needed?
                // rescheduled = true;
                // tEndNew = tEst;
            }
            else
            // -- no, just insert completed trajectory as planned
            {
                for (GuidingStroke gs : sSeq)
                {
                    traj.addGuidingStroke(gs);
                }
            }

            /*
             * TODO add swivel LMP when needed (as in code below)
             * //=== add LMP for moving swivel to zero during prep AND stroke
             * if (swivel > -99)
             * {
             * MgcVectorN q (1);
             * q[0] = swivel;
             * deque<MgcVectorN> goalVec;
             * deque<MgcReal> timeVec;
             * // start
             * goalVec.push_back(q);
             * timeVec.push_back(sT);
             * // end
             * goalVec.push_back(q);
             * timeVec.push_back(eT);
             * LMP_Swivel *lmpSwiv = new LMP_Swivel ("SW_Prep", scope);
             * lmpSwiv->setSwivelVec(goalVec);
             * lmpSwiv->setTimeVec(timeVec);
             * lmp->activatePeerAt(lmpSwiv, sT-0.2);
             * mp->addLMP(lmpSwiv);
             * }
             * }
             */
        }
    }

    private void formWristMovement(String scope, Static staticElem, FeedbackManager bbm, BMLBlockPeg bmlBlockPeg, String bmlId, String id,
            PegBoard pb, MotorControlProgram mcp, AnimationPlayer aniPlayer)
    {
        OrientConstraint oc1 = new OrientConstraint("strokeStart");
        OrientConstraint oc2 = new OrientConstraint("strokeEnd");
        oc1.setPhase(GStrokePhaseID.STP_STROKE);
        oc2.setPhase(GStrokePhaseID.STP_RETRACT);

        float[] vec = Vec3f.getVec3f();

        if (staticElem.getSlot() == Slot.ExtFingerOrientation && hns.getAbsoluteDirection(staticElem.getValue(), vec))
        {
            oc1.setD(vec);
            oc2.setD(vec);
            // insertConstantConstraint(oc1,oc2);
        }
        else if (staticElem.getSlot() == Slot.PalmOrientation && hns.getAbsoluteDirection(staticElem.getValue(), vec))
        {
            oc1.setP(vec);
            oc2.setP(vec);
            // insertConstantConstraint(oc1,oc2);
        }
        else
        {
            return;
        }

        createAndAppendLMPWrist(scope, bbm, bmlBlockPeg, bmlId, id, pb, mcp, aniPlayer, ImmutableList.of(oc1, oc2));

    }

    private void formWristMovement(String scope, List<DynamicElement> elements, FeedbackManager bbm, BMLBlockPeg bmlBlockPeg, String bmlId,
            String id, PegBoard pb, MotorControlProgram mcp, AnimationPlayer aniPlayer)
    {
        float vec[] = Vec3f.getVec3f();
        List<OrientConstraint> ocVec = new ArrayList<>();

        // TODO: check mode and make sure its absolute?
        // // -- collect all relevant constraints (efo or absolute po)
        // vector<MovementConstraintBranchNode*> ocNodes;
        // for (int i=0; i<mcMap.size(); i++)
        // {
        // if ( mcMap[i]->getSlot() == "ExtFingerOrientation" ||
        // (mcMap[i]->getSlot() == "PalmOrientation" &&
        // mcMap[i]->hasAttribute("mode") &&
        // mcMap[i]->getAttribute("mode") == "absolute") )
        // ocNodes.push_back(mcMap[i]);
        // }

        for (DynamicElement dynElem : elements)
        {
            if (dynElem.getValueNodes().size() < 2)
            {
                log.warn("form dynamic constraint: insufficient number of values!");
                return;
            }

            int i = 0;
            for (Entry<String, String> vn : dynElem.getValueNodes())
            {
                String cid = vn.getKey();

                // force first and last ids to be stroke_start and stroke_end respectively
                if (i == 0) cid = "strokeStart";
                if (i == dynElem.getValueNodes().size() - 1) cid = "strokeEnd";

                OrientConstraint oc = new OrientConstraint(cid);
                oc.setPhase(GStrokePhaseID.STP_STROKE);
                vec = Vec3f.getVec3f();
                if (hns.getAbsoluteDirection(vn.getValue(), vec))
                {
                    oc.setP(vec);
                    ocVec.add(oc);
                }
                i++;
            }
        }

        createAndAppendLMPWrist(scope, bbm, bmlBlockPeg, bmlId, id, pb, mcp, aniPlayer, ocVec);

    }

    private void createAndAppendLMPWrist(String scope, FeedbackManager bbm, BMLBlockPeg bmlBlockPeg, String bmlId, String id, PegBoard pb,
            MotorControlProgram mcp, AnimationPlayer aniPlayer, List<OrientConstraint> ocVec)
    {
        // -- create lmp and append to motor program
        if (!ocVec.isEmpty())
        {

            // TODO: set up retraction+holds
            // float fRetrStartT = mp->getRetractionStartTime();
            // if ( fRetrStartT > ocVec.back().t )
            // {
            // ocVec.back().phase = GuidingStroke::STP_HOLD;
            // OrientConstraint oc;
            // oc.d = ocVec.back().d;
            // oc.t = fRetrStartT;
            // ocVec.push_back(oc);
            // }
            // else {
            // // make sure that the last constraint marks stroke end, i.e., retraction onset!
            // ocVec.back().phase = GuidingStroke::STP_RETRACT;
            // }

            // build lmp
            LMPWristRot lmp = createWristRotLMP(scope, ocVec, bbm, bmlBlockPeg, bmlId, id, pb, aniPlayer);

            if (lmp != null)
            {
                // TODO(?)
                // lmp->transform(mp->getBase2Root());
                mcp.addLMP(lmp);
            }
        }
    }

    private LMPWristRot createWristRotLMP(String scope, List<OrientConstraint> ocVec, FeedbackManager bbm, BMLBlockPeg bmlBlockPeg,
            String bmlId, String id, PegBoard pb, AnimationPlayer aniPlayer)
    {
        if (ocVec.isEmpty())
        {
            return null;
        }
        LMPWristRot lmp = new LMPWristRot(scope, ocVec, bbm, bmlBlockPeg, bmlId, id + "_lmp"
                + UUID.randomUUID().toString().replaceAll("-", ""), pb, aniPlayer);

        // if ( retrMode == RTRCT_NO )
        // {
        // ocVec.back().phase = GuidingStroke::STP_FINISH;
        // lmp->setOrientConstraints(ocVec);
        // }
        // else
        // {
        // lmp->setOrientConstraints(ocVec);
        //
        // // -- prepare internal retraction from wrist movement
        // LMP_JAngleNSN *lmpRetract = new LMP_JAngleNSN ("HGO_Retract",scope);
        //
        // // will eventually end in 'restAngles'
        // deque<MgcVectorN> goalVec;
        // goalVec.push_back(restAngles);
        //
        // // will arrive there at a certain time
        // deque<MgcReal> timeVec;
        // timeVec.push_back(ocVec.back().t + retractionTime);
        //
        // // goes into 'finish'
        // deque<GuidingStroke::GStrokePhaseID> phaseVec;
        // phaseVec.push_back(GuidingStroke::STP_FINISH);
        //
        // // the angles/time/phase before going into 'retract' will be set at activation time
        // lmp->activateSuccessorAt(lmpRetract);
        //
        // // Note: According to Hoffmann & Hammel, we apply an
        // // isochronous strategy for goal directed wrist movement!
        // lmpRetract->setAngleVec(goalVec);
        // lmpRetract->setTimeVec(timeVec);
        // lmpRetract->setPhaseVec(phaseVec);
        // }

        return lmp;
    }

    private void formPOMovement(String scope, Static staticElem, FeedbackManager bbm, BMLBlockPeg bmlBlockPeg, String bmlId, String id,
            PegBoard pb, MotorControlProgram mcp, AnimationPlayer aniPlayer)
    {
        List<PoConstraint> poVec = new ArrayList<>();        

        if (!hns.isPalmOrientation(staticElem.getValue()))
        {
            return;
        }

        double po = hns.getPalmOrientation(staticElem.getValue(), scope);

        poVec.add(new PoConstraint(po, GStrokePhaseID.STP_STROKE,"strokeStart"));
        addLMPPoRot(scope, bbm, bmlBlockPeg, bmlId, id, pb, mcp, poVec);
    }

    private void addLMPPoRot(String scope, FeedbackManager bbm, BMLBlockPeg bmlBlockPeg, String bmlId, String id, PegBoard pb,
            MotorControlProgram mcp, List<PoConstraint>poVec)
    {
        // // --- FIX-ME?: ---
        // // PO: retraction is NOT sync'ed with arm retraction movement!!
        // // But maybe should be???

        // TODO set up retraction+holds(?)
        // if ( retrMode != RTRCT_NO )
        // {
        // // post-stroke hold?
        // float fRetrStartT = mp->getRetractionStartTime();
        // if ( fRetrStartT > eT )
        // {
        // eT = fRetrStartT;
        // timeVec.push_back(eT);
        // poVec.push_back(po);
        // phaseVec.back() = GuidingStroke::STP_HOLD;
        // }
        //
        // timeVec.push_back(eT + LMP_PoRot::getPODurationFromAmplitude(po[0]));
        //
        // MgcVectorN v(1);
        // v[0] = restAngles[2];
        // poVec.push_back(v);
        //
        // phaseVec.back() = GuidingStroke::STP_RETRACT;
        // phaseVec.push_back(GuidingStroke::STP_FINISH);
        // }
        // else
        // {
        // phaseVec.back() = GuidingStroke::STP_FINISH;
        // }
        //
        // -- create lmp and append to motor program
        LMPPoRot lmp = new LMPPoRot(scope, poVec, bbm, bmlBlockPeg, bmlId, id, pb);
        lmp.setPoConstraint(poVec);
        mcp.addLMP(lmp);
    }

    private void formPOMovement(String scope, List<DynamicElement> elements, FeedbackManager bbm, BMLBlockPeg bmlBlockPeg, String bmlId,
            String id, PegBoard pb, MotorControlProgram mcp, AnimationPlayer aniPlayer)
    {
        List<PoConstraint> poVec = new ArrayList<>();
        for (DynamicElement dynElem : elements)
        {
            if (dynElem.getValueNodes().size() >= 2)
            {
                int i = 0;
                for (Entry<String, String> vn : dynElem.getValueNodes())
                {
                    if (hns.isPalmOrientation(vn.getValue()))
                    {
                        double po = hns.getPalmOrientation(vn.getValue(), scope);
                        String cid = vn.getKey();
                        if (i == 0) cid = "strokeStart";
                        if (i == dynElem.getValueNodes().size() - 1) cid = "strokeEnd";
                        poVec.add(new PoConstraint(po,GStrokePhaseID.STP_STROKE, cid));   
                        i++;
                    }
                }
            }
            else
            {
                log.warn("formPO: insufficient number of values in dynamic element");
            }
        }
        if(poVec.size()==0)return;
        addLMPPoRot(scope, bbm, bmlBlockPeg, bmlId, id, pb, mcp, poVec);
    }

    public void getDynamicPalmOrientationElementsTMU(String scope, List<DynamicElement> elements, FeedbackManager bbm,
            BMLBlockPeg bmlBlockPeg, String bmlId, String id, PegBoard pb, MotorControlProgram mcp, AnimationPlayer aniPlayer)
    {
        formWristMovement(scope, elements, bbm, bmlBlockPeg, bmlId, id, pb, mcp, aniPlayer);
        formPOMovement(scope, elements, bbm, bmlBlockPeg, bmlId, id, pb, mcp, aniPlayer);
    }

    public void getStaticPalmOrientationTMU(String scope, Static staticElem, FeedbackManager bbm, BMLBlockPeg bmlBlockPeg, String bmlId,
            String id, PegBoard pb, MotorControlProgram mcp, AnimationPlayer aniPlayer)
    {
        formWristMovement(scope, staticElem, bbm, bmlBlockPeg, bmlId, id, pb, mcp, aniPlayer);
        formPOMovement(scope, staticElem, bbm, bmlBlockPeg, bmlId, id, pb, mcp, aniPlayer);
    }

    public void getDynamicHandLocationElementsTMU(String scope, List<DynamicElement> elements, FeedbackManager bbm,
            BMLBlockPeg bmlBlockPeg, String bmlId, String id, PegBoard pb, MotorControlProgram mcp)
    {
        if (elements.isEmpty())
        {
            return;
        }
        GuidingSequence trajectory = new GuidingSequence();
        TPConstraint eT = new TPConstraint();
        TPConstraint sT = new TPConstraint();
        float[] ePos = Vec3f.getVec3f();
        trajectory.setST(sT);
        float swivel = 0;
        TimedAnimationUnit tmu = null;

        // TODO:
        // eT = mcLoc->getStartTPC();

        if (getStartConf(ePos, elements.get(0), hns))
        {
            // --- create linear guiding stroke for the preparatory movement which
            // ends with the constraints start configuration
            trajectory.addGuidingStroke(new LinearGStroke(GStrokePhaseID.STP_PREP, eT, ePos));

            appendSubTrajectory(elements, scope, trajectory, tmu, bbm, bmlBlockPeg, bmlId, id, pb, mcp);
        }

        // TODO: implement retraction, retraction modes
        // // --- shall we retract at all?
        // if ( retrMode == RTRCT_FULL || retrMode == RTRCT_INTERMEDIATE )
        // {
        // // -- complete ongoing guiding sequence by appending a retracting stroke
        // // (estimate retraction duration by applying Fitts' law)
        // ePos = restPos;
        // MgcReal duration = LMP_WristPos::getPosDurationFromAmplitude( (ePos-sPos).Length()); // * 0.9;
        // eT = trajectory.getEndTime() + duration;
        // eT.mode = TPConstraint::Soft; // end time of retraction not of great importance
        // trajectory.addGuidingStroke( new LinearGStroke (GuidingStroke::STP_RETRACT, eT,ePos) );
        // }

        // -- build and append retracting lmp(s)
        createPosLMP(scope, trajectory, mcp, tmu, bbm, bmlBlockPeg, id, id, pb);
    }

    private static void createPosLMP(String scope, GuidingSequence traj, MotorControlProgram mp, TimedAnimationUnit lmp,
            FeedbackManager bbf, BMLBlockPeg bmlBlockPeg, String bmlId, String id, PegBoard pegBoard)
    {
        if (!traj.isEmpty() && mp != null)
        {
            // -- create lmp for wrist trajectory
            // cout << "==== creating lmp from: "; traj.writeTo(cout); cout << endl;

            LMPWristPos wristMove = new LMPWristPos(scope, bbf, bmlBlockPeg, bmlId, id, pegBoard);

            // TODO
            // -- set transformation for converting positions into base coordinates
            // wristMove->setBaseFrame( mp->getBase2Root() );

            // -- set guiding sequence -> will transform the seq into base coords!
            wristMove.setGuidingSeq(traj);

            // -- absolutely new movement, or should we append to previous LMP?
            if (lmp == null)
            {
                mp.addLMP(wristMove);

            }
            else
            {
                // TODO: solve with TimePegs
                // lmp->activateSuccessorAt( wristMove,traj.getStartTPC() );
                lmp = wristMove;
            }

            // TODO
            // // -- extend movement with retractory parts, e.g., overshooting
            // if ( retrMode == RTRCT_FULL || retrMode == RTRCT_INTERMEDIATE )
            // {
            // // -- prepapre swivel retraction
            // // According to Soechting et al, wrist and arm control are relatively independent. In
            // // consequence, elbow swivel can be defined mainly on basis of the wrist position. The
            // // retraction of the overall wrist movement hence coactivates the swivel retraction.
            // MgcVectorN q (1);
            // q[0] = restSwivel;
            // deque<MgcVectorN> goalVec;
            // deque<MgcReal> timeVec;
            // goalVec.push_back(q);
            // timeVec.push_back( traj.getEndTime() );
            // LMP_Swivel *lmpSwiv = new LMP_Swivel ( "SW_Retract", scope );
            // lmpSwiv->setSwivelVec(goalVec);
            // lmpSwiv->setTimeVec(timeVec);
            // lmp->activatePeerAt( lmpSwiv, traj.getEndTime()-0.2 );
            // mp->addLMP( lmpSwiv );
            //
            // // -- create lmp for overshooting the retraction
            // if ( retrMode = RTRCT_FULL )
            // {
            // // lpm->overshoot();
            // }
            // }
        }
    }

    public TimedAnimationUnit getKeyFramingTMU(Keyframing kf, FeedbackManager bbm, BMLBlockPeg bmlBlockPeg, String bmlId, String id,
            PegBoard pb, AnimationPlayer aniPlayer) throws MUSetupException
    {
        AnimationUnit mu = getKeyFramingMU(kf);
        AnimationUnit muCopy = mu.copy(aniPlayer);
        return muCopy.createTMU(bbm, bmlBlockPeg, bmlId, id, pb);
    }

    public AnimationUnit getKeyFramingMU(Keyframing kf)
    {

        // XXX for now just generates a MU for the first phase
        Phase ph = kf.getPhases().get(0);

        if (ph.getFrames().size() > 0)
        {
            Posture p0 = ph.getFrames().get(0).getPosture();

            List<String> targets = new ArrayList<String>();
            List<KeyFrame> keyFrames = new ArrayList<KeyFrame>();

            int nrOfDofs = 0;
            // XXX assumes that all frames have the same interpolation targets
            for (JointValue jv : p0.getJointValues())
            {
                targets.add(jv.jointSid);
                nrOfDofs += jv.getDofs().length;
            }

            nrOfDofs = (nrOfDofs * 4) / 3;
            for (Frame f : ph.getFrames())
            {
                int size = 0;
                for (JointValue jv : f.getPosture().getJointValues())
                {
                    size += jv.getDofs().length;
                }
                float dofs[] = new float[(size * 4) / 3];

                int i = 0;
                for (JointValue jv : f.getPosture().getJointValues())
                {
                    float q[] = new float[4];
                    Quat4f.setFromRollPitchYawDegrees(q, jv.getDofs()[0], jv.getDofs()[1], jv.getDofs()[2]);
                    Quat4f.set(dofs, i * 4, q, 0);
                    i++;
                }
                keyFrames.add(new KeyFrame(f.getFtime(), dofs));
            }

            LinearQuatFloatInterpolator interp = new LinearQuatFloatInterpolator();
            interp.setKeyFrames(keyFrames, nrOfDofs);
            double scale = kf.getEasescale();
            double p = kf.getEaseturningpoint();
            return new MURMLKeyframeMU(targets, interp, new EaseInEaseOutManipulator(scale, p), keyFrames, nrOfDofs,
                    kf.isInsertStartframe());
        }
        return null;
    }

    public AnimationUnit setup(MURMLDescription murmlDescription)
    {
        if (murmlDescription.getDynamic() != null)
        {
            Dynamic dyn = murmlDescription.getDynamic();
            if (dyn.getKeyframing() != null)
            {
                return getKeyFramingMU(dyn.getKeyframing());
            }
        }
        return null;
    }

    public TimedAnimationUnit setupTMU(String murmlStr, FeedbackManager bbm, BMLBlockPeg bmlBlockPeg, String bmlId, String id, PegBoard pb,
            AnimationPlayer aniPlayer) throws MUSetupException
    {
        MURMLDescription def = new MURMLDescription();
        def.readXML(murmlStr);
        return setupTMU(def, bbm, bmlBlockPeg, bmlId, id, pb, aniPlayer);
    }

    public TimedAnimationUnit setupTMU(MURMLDescription murmlDescription, FeedbackManager bbm, BMLBlockPeg bmlBlockPeg, String bmlId,
            String id, PegBoard pb, AnimationPlayer aniPlayer) throws MUSetupException
    {
        PegBoard localPegBoard = new PegBoard();
        MotorControlProgram mcp = new MotorControlProgram(bbm, bmlBlockPeg, bmlId, id, pb, localPegBoard);

        if (murmlDescription.getDynamic() != null)
        {
            Dynamic dyn = murmlDescription.getDynamic();
            if (dyn.getKeyframing() != null)
            {
                return getKeyFramingTMU(dyn.getKeyframing(), bbm, bmlBlockPeg, bmlId, id, pb, aniPlayer);
            }
            else if (dyn.getDynamicElements().size() > 0)
            {
                switch (dyn.getSlot())
                {
                case GazeDirection:
                    break;
                case Neck:
                    break;
                case HandLocation:
                    getDynamicHandLocationElementsTMU(dyn.getScope(), dyn.getDynamicElements(), bbm, bmlBlockPeg, bmlId, id, localPegBoard,
                            mcp);
                    break;
                case HandShape:
                    break;
                case ExtFingerOrientation:
                    break;
                case PalmOrientation:
                    getDynamicPalmOrientationElementsTMU(dyn.getScope(), dyn.getDynamicElements(), bbm, bmlBlockPeg, bmlId, id,
                            localPegBoard, mcp, aniPlayer);
                    break;
                }
            }
        }
        else if (murmlDescription.getStaticElement() != null)
        {
            Static staticElem = murmlDescription.getStaticElement();
            switch (staticElem.getSlot())
            {
            case GazeDirection:
                break;
            case Neck:
                break;
            case HandLocation:
                break;
            case HandShape:
                break;
            case ExtFingerOrientation:
                break;
            case PalmOrientation:
                getStaticPalmOrientationTMU(staticElem.getScope(), staticElem, bbm, bmlBlockPeg, bmlId, id, localPegBoard, mcp, aniPlayer);
                break;
            }
        }
        else if (murmlDescription.getParallel() != null)
        {
            Parallel par = murmlDescription.getParallel();
            for (Static stat : par.getStatics())
            {

            }
        }
        return mcp;
    }
}
