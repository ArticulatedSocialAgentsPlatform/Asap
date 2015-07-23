/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.gesturebinding;

import hmi.animation.SkeletonPose;
import hmi.math.Quat4f;
import hmi.math.Vec3f;
import hmi.neurophysics.FittsLaw;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import asap.animationengine.ace.PostureConstraint;
import asap.animationengine.ace.lmp.LMP;
import asap.animationengine.ace.lmp.LMPHandMove;
import asap.animationengine.ace.lmp.LMPParallel;
import asap.animationengine.ace.lmp.LMPPoRot;
import asap.animationengine.ace.lmp.LMPSequence;
import asap.animationengine.ace.lmp.LMPWristPos;
import asap.animationengine.ace.lmp.LMPWristRot;
import asap.animationengine.ace.lmp.MotorControlProgram;
import asap.animationengine.keyframe.MURMLKeyframeMU;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.motionunit.TMUSetupException;
import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.hns.Hns;
import asap.hns.ShapeSymbols;
import asap.motionunit.keyframe.CubicQuatFloatInterpolator;
import asap.motionunit.keyframe.Interpolator;
import asap.motionunit.keyframe.KeyFrame;
import asap.motionunit.keyframe.LinearQuatFloatInterpolator;
import asap.murml.Dynamic;
import asap.murml.DynamicElement;
import asap.murml.DynamicElement.Type;
import asap.murml.Frame;
import asap.murml.JointValue;
import asap.murml.Keyframing;
import asap.murml.Keyframing.Mode;
import asap.murml.MURMLDescription;
import asap.murml.MovementConstraint;
import asap.murml.Parallel;
import asap.murml.Phase;
import asap.murml.Posture;
import asap.murml.Sequence;
import asap.murml.Slot;
import asap.murml.Static;
import asap.murml.Value;
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
    private final HnsHandshape hnsHandshapes;

    public MURMLMUBuilder(Hns hns, HnsHandshape handshapes)
    {
        this.hns = hns;
        this.hnsHandshapes = handshapes;
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

    /**
     * return start position [0..2] and swivel [3] of the wrist for this movement constraint in
     * body root coordinates.
     */
    private boolean getStartConf(float[] result, Static staticElem, Hns hns)
    {
        float startconf[] = new float[4];
        startconf[3] = 0;
        return hns.getHandLocation(staticElem.getValue(), result);
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
    private void appendSubTrajectory(Dynamic dyn, GuidingSequence traj, TimedAnimationUnit tmu, FeedbackManager bbf,
            BMLBlockPeg bmlBlockPeg, String bmlId, String id, PegBoard pegBoard, AnimationPlayer aniPlayer) throws TMUSetupException
    {
        double tEst = 0;
        float swivel = -99;
        List<GuidingStroke> sSeq = new ArrayList<>();

        float sPos[] = traj.getEndPos();

        for (DynamicElement segment : dyn.getDynamicElements())
        {

            // -- create guiding strokes for linear movement segment --------------------------------------
            if (segment.getType() == Type.LINEAR)
            {
                float[] dir = Vec3f.getVec3f();
                float[] ePos = Vec3f.getVec3f();

                // cout << "appending linear segment" << endl;
                double d = 0;

                // linear movement given absolutely in terms of start and end points
                if (segment.getName("end") != null && hns.getHandLocation(segment.getName("end"), ePos))
                {
                    hns.transFormLocation(ePos, dyn.getSymmetryTransform());
                    Vec3f.sub(dir, ePos, sPos);
                    Vec3f.normalize(dir);
                }
                // linear movement given in terms of start point end direction
                else if (segment.getName("direction") != null && segment.getName("distance") != null
                        && hns.getAbsoluteDirection(segment.getName("direction"), dir))
                {
                    Vec3f.normalize(dir);
                    double dist = hns.getDistance(segment.getName("distance"));

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
                    throw new TMUSetupException(
                            "ArmMotorControl::appendSubtrajectory : invalid definition of for linear segment, linear segment "
                                    + "requires either an end definition or a direction and distance.", null);
                }

                // -- create guiding stroke, estimate duration and add to subtrajectory

                // cout << "NEW LINEAR Guiding Stroke to " << eT << ":" << ePos << endl;

                LinearGStroke lgs = new LinearGStroke(GStrokePhaseID.STP_STROKE, ePos);
                double dur = FittsLaw.getHandTrajectoryDuration(lgs.getArcLengthFrom(sPos));
                
                if(dur<0.001)dur = 0.001;//HACK HACK: minimum duration 1 ms
                
                lgs.setEDt(dur);
                tEst += lgs.getEDt();
                // cout << "est. duration=" << lgs->eDt << " -> est. end time=" << tEst << endl;

                sSeq.add(lgs);
                sPos = lgs.getEndPos();
            }

            // -- create guiding strokes for curvilinear movement segment ---------------------
            else if (segment.getType() == Type.CURVE)
            {
                float[] ePos = Vec3f.getVec3f();
                float[] nVec = Vec3f.getVec3f();
                ShapeSymbols shape = ShapeSymbols.RightS;

                if (segment.getName("end") == null || !hns.getHandLocation(segment.getName("end"), ePos))
                {
                    throw new TMUSetupException("ArmMotorControl::appendSubtrajectory : invalid or missing parameter end"
                            + segment.getName("end") + "for curved guiding stroke!", null);
                }

                if (segment.getName("normal") == null || !hns.getAbsoluteDirection(segment.getName("normal"), nVec))
                {
                    throw new TMUSetupException(
                            "ArmMotorControl::appendSubtrajectory : invalid or missing parameter normal for curved guiding stroke!, normal="
                                    + segment.getName("normal"), null);
                }

                if (segment.getName("shape") != null)
                {
                    shape = hns.getElementShape(segment.getName("shape"));
                }

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
                CurvedGStroke cgs = new CurvedGStroke(GStrokePhaseID.STP_STROKE, ePos, nVec, shape, extent, roundness, skewedness);
                // build geometric parameters for given start position
                // (necessary for estimating duration!)

                // XXX not needed here, done in refine (?)
                // do this dynamically
                // /cgs.formAt(sPos, sT);

                // cgs->vGain = 0.;

                // -- stroke time smaller than affiliate duration, i.e., post-stroke hold required?
                double dur = FittsLaw.getHandTrajectoryDuration(cgs.getArcLengthFrom(sPos));
                if(dur<0.001)dur = 0.001;//HACK HACK: minimum duration 1 ms
                cgs.setEDt(dur);

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
            // TODO: this should happen automatically in LMPWristPos
            // // -- intra-stroke scheduling:
            // // estimated stroke duration smaller than affiliate duration,
            // // i.e. post-stroke hold required?
            // // cout << "required time:" << tEst << " < " << eT << "?" << endl;
            // if (tEst < eT)
            // {
            // // cout << "yes -> re-scheduling!";
            // // -- re-schedule all inner elements and append to subtrajectory
            // double ssT = traj.getEndTime();
            // for (int i = 0; i < sSeq.size(); i++)
            // {
            // // cout << ssT << "->";
            // ssT += sSeq.get(i).getEDt();
            // // cout << ssT << endl;
            // sSeq.get(i).setET(new TPConstraint(ssT));
            // traj.addGuidingStroke(sSeq.get(i));
            // }
            //
            // // -- create respective motor program
            // // cout << "creating lmp from:"; traj.writeTo(cout); cout << endl;
            //
            // // TODO: implement proper scope selection when no scope is provided.
            // I think this is handled through MotorControlFigure::suggestMotorScopeFor in ACE.
            // if (scope == null)
            // {
            // scope = "left_arm";
            // }
            // if (!(scope.equals("left_arm") || scope.equals("right_arm")))
            // {
            // scope = "left_arm";
            // }
            // LMPWristPos wristMove = new LMPWristPos(scope, bbf, bmlBlockPeg, bmlId, id, pegBoard, traj, aniPlayer);
            //
            // // XXX needed?
            // // wristMove->setBaseFrame( mp->getBase2Root() );
            //
            // if (tmu == null)
            // {
            // mp.addLMP(wristMove);
            // }
            // else
            // {
            // // TODO: solve this with TimePegs
            // // lmp->activateSuccessorAt( wristMove, traj.getStartTPC() );
            // }
            // tmu = wristMove;
            //
            // // -- reset guiding sequence for subsequent movement phase(s)
            // traj.clear();
            // traj.setST(new TPConstraint(eT, TPConstraint.Mode.Rigorous));
            // traj.setStartPos(ePos);
            //
            // // XXX needed?
            // // rescheduled = true;
            // // tEndNew = tEst;
            // }
            // else
            // -- no, just insert completed trajectory as planned
            {
                for (GuidingStroke gs : sSeq)
                {
                    traj.addGuidingStroke(gs);
                }
            }
        }
    }

    private List<OrientConstraint> formWristMovement(String scope, Static staticElem, FeedbackManager bbm, BMLBlockPeg bmlBlockPeg,
            String bmlId, String id, PegBoard pb, AnimationPlayer aniPlayer) throws TMUSetupException
    {
        OrientConstraint oc1 = new OrientConstraint("strokeStart", GStrokePhaseID.STP_STROKE);
        OrientConstraint oc2 = new OrientConstraint("strokeEnd", GStrokePhaseID.STP_RETRACT);

        float[] vec = Vec3f.getVec3f();

        if (staticElem.getSlot() == Slot.ExtFingerOrientation && hns.getAbsoluteDirection(staticElem.getValue(), vec))
        {
            hns.transFormDirection(vec, staticElem.getSymmetryTransform());
            oc1.setD(vec);
            oc2.setD(vec);
        }
        else if (staticElem.getSlot() == Slot.PalmOrientation && hns.getAbsoluteDirection(staticElem.getValue(), vec))
        {
            hns.transFormDirection(vec, staticElem.getSymmetryTransform());
            oc1.setP(vec);
            oc2.setP(vec);
        }
        else if (staticElem.getSlot() == Slot.ExtFingerOrientation)
        {
            throw new TMUSetupException("Invalid ExtFingerOrientation " + staticElem.getValue(), null);
        }
        else
        {
            return ImmutableList.of();
        }
        return ImmutableList.of(oc1, oc2);
    }

    private int getNumberOfValues(Dynamic dyn)
    {
        int numValues = 0;
        for (DynamicElement dynElem : dyn.getDynamicElements())
        {
            numValues += dynElem.getValues().size();
        }
        return numValues;
    }

    private List<OrientConstraint> formWristMovement(Dynamic dyn, FeedbackManager bbm, BMLBlockPeg bmlBlockPeg, String bmlId, String id,
            PegBoard pb, AnimationPlayer aniPlayer) throws TMUSetupException
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

        int i = 0;
        int lastValue = getNumberOfValues(dyn);

        for (DynamicElement dynElem : dyn.getDynamicElements())
        {
            if (dynElem.getValues().size() < 2)
            {
                log.warn("form dynamic constraint: insufficient number of values!");
                return ocVec;
            }

            for (Value v : dynElem.getValues())
            {
                String cid = v.getId();
                if (cid.isEmpty())
                {
                    cid = "stroke" + i;
                }
                // force first and last ids to be strokeStart and strokeEnd respectively
                if (i == 0) cid = "strokeStart";
                if (i == lastValue - 1) cid = "strokeEnd";

                OrientConstraint oc = new OrientConstraint(cid, GStrokePhaseID.STP_STROKE);
                vec = Vec3f.getVec3f();
                if (hns.getAbsoluteDirection(v.getName(), vec))
                {
                    switch (dyn.getSlot())
                    {
                    case PalmOrientation:
                        hns.transFormDirection(vec, dyn.getSymmetryTransform());
                        oc.setP(vec);
                        break;
                    case ExtFingerOrientation:
                        hns.transFormDirection(vec, dyn.getSymmetryTransform());
                        oc.setD(vec);
                        break;
                    default:
                        log.warn("cannot construct wrist movement from " + dyn.getSlot() + " slot.");
                    }
                    ocVec.add(oc);
                }
                else if (dyn.getSlot() == Slot.ExtFingerOrientation)
                {
                    throw new TMUSetupException("Invalid ExtFingerOrientation " + v.getName(), null);
                }
                i++;
            }
        }
        return ocVec;
    }

    private LMP createAndAppendLMPWrist(String scope, FeedbackManager bbm, BMLBlockPeg bmlBlockPeg, String bmlId, String id, PegBoard pb,
            AnimationPlayer aniPlayer, List<OrientConstraint> ocVec)
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
                return lmp;
            }
        }
        return null;
    }

    private String createLMPId(String id)
    {
        return id + "_lmp" + UUID.randomUUID().toString().replaceAll("-", "");
    }

    private LMPWristRot createWristRotLMP(String scope, List<OrientConstraint> ocVec, FeedbackManager bbm, BMLBlockPeg bmlBlockPeg,
            String bmlId, String id, PegBoard pb, AnimationPlayer aniPlayer)
    {
        if (ocVec.isEmpty())
        {
            return null;
        }
        LMPWristRot lmp = new LMPWristRot(scope, ocVec, bbm, bmlBlockPeg, bmlId, createLMPId(id), pb, aniPlayer);

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

    private LMP formPOMovement(String scope, Static staticElem, FeedbackManager bbm, BMLBlockPeg bmlBlockPeg, String bmlId, String id,
            PegBoard pb, AnimationPlayer aniPlayer)
    {
        List<PoConstraint> poVec = new ArrayList<>();

        if (!hns.isPalmOrientation(staticElem.getValue()))
        {
            return null;
        }

        double po = hns.getPalmOrientation(staticElem.getValue(), scope);

        poVec.add(new PoConstraint(po, GStrokePhaseID.STP_STROKE, "strokeStart"));
        return addLMPPoRot(scope, bbm, bmlBlockPeg, bmlId, id, pb, poVec, aniPlayer);
    }

    private LMP addLMPPoRot(String scope, FeedbackManager bbm, BMLBlockPeg bmlBlockPeg, String bmlId, String id, PegBoard pb,
            List<PoConstraint> poVec, AnimationPlayer aniPlayer)
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
        LMPPoRot lmp = new LMPPoRot(scope, poVec, bbm, bmlBlockPeg, bmlId, createLMPId(id), pb, aniPlayer);
        lmp.setPoConstraint(poVec);
        return lmp;
    }

    private LMP formPOMovement(Dynamic dyn, FeedbackManager bbm, BMLBlockPeg bmlBlockPeg, String bmlId, String id, PegBoard pb,
            AnimationPlayer aniPlayer)
    {
        List<PoConstraint> poVec = new ArrayList<>();
        int i = 0;
        int lastValue = getNumberOfValues(dyn);

        for (DynamicElement dynElem : dyn.getDynamicElements())
        {
            if (dynElem.getValues().size() >= 2)
            {
                for (Value v : dynElem.getValues())
                {
                    if (hns.isPalmOrientation(v.getName()))
                    {
                        double po = hns.getPalmOrientation(v.getName(), dyn.getScope());
                        String cid = v.getId();
                        if (i == 0) cid = "strokeStart";
                        if (i == lastValue - 1) cid = "strokeEnd";
                        if (cid.isEmpty())
                        {
                            cid = "stroke" + i;
                        }
                        poVec.add(new PoConstraint(po, GStrokePhaseID.STP_STROKE, cid));
                        i++;
                    }
                }
            }
            else
            {
                log.warn("formPO: insufficient number of values in dynamic element");
            }
        }
        if (poVec.size() == 0) return null;
        return addLMPPoRot(dyn.getScope(), bbm, bmlBlockPeg, bmlId, id, pb, poVec, aniPlayer);
    }

    public List<OrientConstraint> getDynamicPalmOrientationElementsTMU(Dynamic dyn, FeedbackManager bbm, BMLBlockPeg bmlBlockPeg,
            String bmlId, String id, PegBoard pb, AnimationPlayer aniPlayer) throws TMUSetupException
    {
        return formWristMovement(dyn, bbm, bmlBlockPeg, bmlId, id, pb, aniPlayer);
    }

    public List<OrientConstraint> getExtFingerOrientationnElementsTMU(Dynamic dyn, FeedbackManager bbm, BMLBlockPeg bmlBlockPeg,
            String bmlId, String id, PegBoard pb, AnimationPlayer aniPlayer) throws TMUSetupException
    {
        return formWristMovement(dyn, bbm, bmlBlockPeg, bmlId, id, pb, aniPlayer);
    }

    public List<OrientConstraint> getStaticPalmOrientationTMU(String scope, Static staticElem, FeedbackManager bbm,
            BMLBlockPeg bmlBlockPeg, String bmlId, String id, PegBoard pb, AnimationPlayer aniPlayer) throws TMUSetupException
    {
        return formWristMovement(scope, staticElem, bbm, bmlBlockPeg, bmlId, id, pb, aniPlayer);
    }

    public List<OrientConstraint> getStaticExtFingerOrientationOrientationTMU(String scope, Static staticElem, FeedbackManager bbm,
            BMLBlockPeg bmlBlockPeg, String bmlId, String id, PegBoard pb, AnimationPlayer aniPlayer) throws TMUSetupException
    {
        return formWristMovement(scope, staticElem, bbm, bmlBlockPeg, bmlId, id, pb, aniPlayer);
    }

    public LMP getDynamicHandShapeTMU(Dynamic dyn, FeedbackManager bbm, BMLBlockPeg bmlBlockPeg, String bmlId, String id, PegBoard pb,
            AnimationPlayer aniPlayer) throws TMUSetupException
    {
        List<PostureConstraint> phaseVec = new ArrayList<>();

        int i = 0;
        int lastValue = getNumberOfValues(dyn);

        for (DynamicElement dynElem : dyn.getDynamicElements())
        {
            for (Value v : dynElem.getValues())
            {
                String cid = v.getId();
                if (cid.isEmpty())
                {
                    cid = "stroke" + i;
                }
                if (i == 0) cid = "strokeStart";
                if (i == lastValue - 1) cid = "strokeEnd";
                SkeletonPose pose = hnsHandshapes.getHNSHandShape(v.getName());
                if (pose == null)
                {
                    throw new TMUSetupException("Cannot find HandShape " + v.getName(), null);
                }

                phaseVec.add(new PostureConstraint(cid, pose));
                i++;
            }
        }

        return new LMPHandMove(dyn.getScope(), phaseVec, bbm, bmlBlockPeg, bmlId, createLMPId(id), pb, aniPlayer, false);
    }

    public LMP getStaticHandShapeElementTMU(String scope, Static staticElem, FeedbackManager bbm, BMLBlockPeg bmlBlockPeg, String bmlId,
            String id, PegBoard pb, AnimationPlayer aniPlayer) throws TMUSetupException
    {

        // // --- preparations
        // FrameData *frame;
        // LMP_HandMove *lmp = new LMP_HandMove ("HF_Stroke", scope);
        // list<pair<MgcReal,GuidingStroke::GStrokePhaseID> > phaseVec;

        List<PostureConstraint> phaseVec = new ArrayList<>();
        SkeletonPose pose = hnsHandshapes.getHNSHandShape(staticElem.getValue());
        if (pose == null)
        {
            throw new TMUSetupException("Cannot find HandShape " + staticElem.getValue(), null);
        }

        phaseVec.add(new PostureConstraint("strokeStart", pose));
        pose = hnsHandshapes.getHNSHandShape(staticElem.getValue()); // make sure this is a copy!
        phaseVec.add(new PostureConstraint("strokeEnd", pose));

        // // get corresponding hand shape
        // if (getHNSHandShape(sc->getValue(), pose)) {
        // // constraint start conf
        // frame = new FrameData (pose, sc->getStartTPC().time);
        // lmp->appendTransitionTo(frame, (int)figure->updateRate, 15, 0.5);
        // phaseVec.push_back(make_pair( sc->getStartTPC().time, GuidingStroke::STP_STROKE ));
        //
        // // --- VL-HACK ------------------------------------------
        // //if ( !fullRetract && !interimRetract )
        // //{
        // //phaseVec.back().second = STP_FINISH;
        // //eT = sc->getStartTPC().time;
        // //}
        // //else {
        // // --- VL-HACK ------------------------------------------
        //
        // // constraint end conf
        // frame = new FrameData (pose, sc->getEndTPC().time);
        // lmp->appendTransitionTo(frame, (int)figure->updateRate);
        // phaseVec.push_back(make_pair( sc->getEndTPC().time, GuidingStroke::STP_RETRACT ));
        // eT = sc->getEndTPC().time;
        // }
        // else {
        // //dbout.Error("HandMotorControl : invalid hand shape spec %s\n",
        // // sc->getValue().c_str());
        // delete lmp;
        // return;
        // }
        // }
        //

        // else {
        // dbout.Error("HandMotorControl : invalid movement constraint %s (%s)\n",
        // (*it)->getSlot().c_str(), (*it)->getScope().c_str());
        // delete lmp;
        // return;
        // }
        // }
        //
        //
        // // -- if retraction is planned at all
        // if ( retrMode != RTRCT_NO )
        // {
        // // insert post-stroke hold if retraction is to start later than 'eT'
        // float fRetrStartT = mp->getRetractionStartTime();
        // if ( fRetrStartT > eT )
        // {
        // eT = fRetrStartT;
        // frame = new FrameData (pose, eT);
        // phaseVec.push_back(make_pair( eT, GuidingStroke::STP_RETRACT ));
        // lmp->appendTransitionTo(frame, (int)figure->updateRate, 15,0.5);
        // }
        //
        // // --- 3. retraction to neutral hand pose
        // frame = new FrameData (restPose, eT+handTransitionTime);
        // phaseVec.push_back(make_pair( eT+handTransitionTime, GuidingStroke::STP_FINISH ));
        // lmp->appendTransitionTo(frame, (int)figure->updateRate, 15,0.5);
        // }
        // else
        // phaseVec.back().second = GuidingStroke::STP_FINISH;
        //
        return new LMPHandMove(scope, phaseVec, bbm, bmlBlockPeg, bmlId, createLMPId(id), pb, aniPlayer, true);
    }

    public LMP getStaticHandLocationElementTMU(String scope, Static staticElem, FeedbackManager bbm, BMLBlockPeg bmlBlockPeg, String bmlId,
            String id, PegBoard pb, AnimationPlayer aniPlayer) throws TMUSetupException
    {
        GuidingSequence trajectory = new GuidingSequence();
        float[] ePos = Vec3f.getVec3f();

        TimedAnimationUnit tmu = null;

        if (getStartConf(ePos, staticElem, hns))
        {
            hns.transFormLocation(ePos, staticElem.getSymmetryTransform());

            // --- create linear guiding stroke for the preparatory movement which
            // ends with the constraints start configuration
            trajectory.addGuidingStroke(new LinearGStroke(GStrokePhaseID.STP_PREP, ePos));

            // create local motor program for anticipated stroke sequence
            LMPWristPos wristMove = new LMPWristPos(scope, bbm, bmlBlockPeg, bmlId, createLMPId(id), pb, trajectory, hns.getBaseJoint(),
                    aniPlayer, GestureBinding.constructAutoSwivel(scope));

            // //cout << "creating lmp from guiding sequence:" << endl; trajectory.writeTo(cout);
            // //lmp->overshoot(mcLoc->getEndTPC().time);

            return wristMove;
            //
            // // optionally, add special lmp for swivel movement
            // if (swivel != 0.)
            // {
            // MgcVectorN q (1);
            // q[0] = swivel;
            // deque<MgcVectorN> goalVec;
            // deque<MgcReal> timeVec;
            // goalVec.push_back(q);
            // timeVec.push_back( trajectory.getEndTime() );
            // LMP_Swivel *lmpSwiv = new LMP_Swivel ( "SW_Prep", scope );
            // lmpSwiv->setSwivelVec(goalVec);
            // lmpSwiv->setTimeVec(timeVec);
            // lmp->activatePeerAt( lmpSwiv, trajectory.getEndTime()-0.2 );
            // mp->addLMP( lmpSwiv );
            // }
            //

            // clear trajectory plan and set origin for subsequent trajectory
            // trajectory.clear();
            // trajectory.setStartPos( ePos );
            // trajectory.setStartTPC( TPConstraint( scLoc->getEndTPC().time,
            // TPConstraint::Rigorous ) ); // timing is mandatory
        }
        else
        {
            throw new TMUSetupException("Invalid location " + staticElem.getValue(), null);
        }
    }

    public LMP getDynamicHandLocationElementsTMU(Dynamic dyn, FeedbackManager bbm, BMLBlockPeg bmlBlockPeg, String bmlId, String id,
            PegBoard pb, AnimationPlayer aniPlayer) throws TMUSetupException
    {
        if (dyn.getDynamicElements().isEmpty())
        {
            return null;
        }
        GuidingSequence trajectory = new GuidingSequence();
        float[] ePos = Vec3f.getVec3f();
        float swivel = 0;
        LMP tmu = null;

        if (getStartConf(ePos, dyn.getDynamicElements().get(0), hns))
        {
            // --- create linear guiding stroke for the preparatory movement which
            // ends with the constraints start configuration
            hns.transFormLocation(ePos, dyn.getSymmetryTransform());
            trajectory.addGuidingStroke(new LinearGStroke(GStrokePhaseID.STP_PREP, ePos));

            appendSubTrajectory(dyn, trajectory, tmu, bbm, bmlBlockPeg, bmlId, id, pb, aniPlayer);
        }
        else
        {
            throw new TMUSetupException("Invalid location " + dyn.getDynamicElements().get(0).getName("start"), null);
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
        return createPosLMP(dyn.getScope(), trajectory, tmu, bbm, bmlBlockPeg, bmlId, id, pb, aniPlayer);
    }

    private LMP createPosLMP(String scope, GuidingSequence traj, LMP lmp, FeedbackManager bbf, BMLBlockPeg bmlBlockPeg, String bmlId,
            String id, PegBoard pegBoard, AnimationPlayer aniPlayer)
    {
        if (!traj.isEmpty())
        {
            // -- create lmp for wrist trajectory
            // cout << "==== creating lmp from: "; traj.writeTo(cout); cout << endl;

            LMPWristPos wristMove = new LMPWristPos(scope, bbf, bmlBlockPeg, bmlId, createLMPId(id), pegBoard, traj, hns.getBaseJoint(),
                    aniPlayer, GestureBinding.constructAutoSwivel(scope));

            // -- absolutely new movement, or should we append to previous LMP?

            // TODO: solve with TimePegs
            /*
             * if (lmp != null)
             * {
             * lmp->activateSuccessorAt( wristMove,traj.getStartTPC() );
             * }
             */

            lmp = wristMove;

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

            return lmp;
        }
        return null;
    }

    public TimedAnimationUnit getKeyFramingTMU(Keyframing kf, FeedbackManager bbm, BMLBlockPeg bmlBlockPeg, String bmlId, String id,
            PegBoard pb, AnimationPlayer aniPlayer) throws TMUSetupException
    {
        AnimationUnit mu = getKeyFramingMU(kf);
        AnimationUnit muCopy;
        try
        {
            muCopy = mu.copy(aniPlayer);
        }
        catch (MUSetupException e)
        {
            throw new TMUSetupException(e.getMessage(), null, e);
        }
        return muCopy.createTMU(bbm, bmlBlockPeg, bmlId, id, pb);
    }

    private List<Frame> mergeFrames(List<Phase> phases)
    {
        List<Frame> frames = new ArrayList<Frame>();
        double offset = 0;
        double currOffset = 0;
        for (Phase ph : phases)
        {
            for (Frame f : ph.getFrames())
            {
                currOffset = f.getFtime();
                frames.add(new Frame(f.getFtime() + offset, f.getPosture()));
            }
            offset += currOffset;
        }
        return frames;
    }

    public AnimationUnit getKeyFramingMU(Keyframing kf)
    {
        // Phase ph = kf.getPhases().get(0);
        List<Frame> frames = mergeFrames(kf.getPhases());

        if (frames.size() > 0)
        {
            Posture p0 = frames.get(0).getPosture();

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
            for (Frame f : frames)
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

            Interpolator interp;
            if (kf.getMode() == Mode.SQUAD)
            {
                interp = new CubicQuatFloatInterpolator();
            }
            else
            {
                interp = new LinearQuatFloatInterpolator();
            }

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
            AnimationPlayer aniPlayer) throws TMUSetupException
    {
        MURMLDescription def = new MURMLDescription();
        def.readXML(murmlStr);
        return setupTMU(def, bbm, bmlBlockPeg, bmlId, id, pb, aniPlayer);
    }

    private static final float EPSILON = 0.001f;

    private void setConstraint(OrientConstraint src, OrientConstraint c)
    {
        if (!Vec3f.epsilonEquals(c.getD(), Vec3f.getZero(), EPSILON))
        {
            src.setD(c.getD());
        }
        if (!Vec3f.epsilonEquals(c.getP(), Vec3f.getZero(), EPSILON))
        {
            src.setP(c.getP());
        }
    }

    private void mergeStaticConstraints(List<OrientConstraint> ocVec, List<OrientConstraint> staticConstraints)
    {
        assert (staticConstraints.size() == 2);
        OrientConstraint ocFirst = ocVec.get(0);
        OrientConstraint ocLast = ocVec.get(ocVec.size() - 1);
        OrientConstraint cFirst = staticConstraints.get(0);
        OrientConstraint cLast = staticConstraints.get(1);
        setConstraint(ocFirst, cFirst);
        setConstraint(ocLast, cLast);
    }

    private void mergeDynamicConstraints(List<OrientConstraint> ocVec, List<OrientConstraint> dynamicConstraints)
    {
        List<OrientConstraint> ocNewList = new ArrayList<>();
        for (OrientConstraint ocNew : dynamicConstraints)
        {
            boolean newConstraint = true;
            for (OrientConstraint oc : ocVec)
            {
                if (oc.getId().equals(ocNew.getId()))
                {
                    setConstraint(oc, ocNew);
                    newConstraint = false;
                    break;
                }
            }
            if (newConstraint)
            {
                ocNewList.add(ocNew);
            }
        }
        ocVec.addAll(ocNewList);
    }

    public TimedAnimationUnit setupTMU(MURMLDescription murmlDescription, FeedbackManager bbm, BMLBlockPeg bmlBlockPeg, String bmlId,
            String id, PegBoard pb, AnimationPlayer aniPlayer) throws TMUSetupException
    {
        PegBoard localPegBoard = new PegBoard();

        localPegBoard.addBMLBlockPeg(bmlBlockPeg);
        LMP lmp = null;

        if (murmlDescription.getDynamic() != null)
        {
            Dynamic dyn = murmlDescription.getDynamic();
            List<OrientConstraint> ocVec = new ArrayList<OrientConstraint>();
            if (dyn.getKeyframing() != null)
            {
                TimedAnimationUnit tmu = getKeyFramingTMU(dyn.getKeyframing(), bbm, bmlBlockPeg, bmlId, id, pb, aniPlayer);
                tmu.setPriority(murmlDescription.getPriority());
                return tmu;
            }
            else if (dyn.getDynamicElements().size() > 0)
            {
                lmp = parseProceduralDynamic(bbm, bmlBlockPeg, bmlId, id, aniPlayer, localPegBoard, dyn, ocVec);
            }
            if (lmp == null)
            {
                lmp = createAndAppendLMPWrist(dyn.getScope(), bbm, bmlBlockPeg, bmlId, id, localPegBoard, aniPlayer, ocVec);
            }
        }
        else if (murmlDescription.getStaticElement() != null)
        {
            Static staticElem = murmlDescription.getStaticElement();
            List<OrientConstraint> ocVec = new ArrayList<OrientConstraint>();
            lmp = parseStaticElement(bbm, bmlBlockPeg, bmlId, id, aniPlayer, localPegBoard, staticElem, ocVec);
            if (lmp == null) // only ocVec added
            {
                lmp = createAndAppendLMPWrist(staticElem.getScope(), bbm, bmlBlockPeg, bmlId, id, localPegBoard, aniPlayer, ocVec);
            }
        }
        else if (murmlDescription.getParallel() != null)
        {
            lmp = parseParallel(murmlDescription.getParallel(), bbm, bmlBlockPeg, bmlId, id, aniPlayer, localPegBoard);
        }
        else if (murmlDescription.getSequence() != null)
        {
            lmp = parseSequence(murmlDescription.getSequence(), bbm, bmlBlockPeg, bmlId, id, aniPlayer, localPegBoard);
        }
        if (lmp != null)
        {
            MotorControlProgram tmu = new MotorControlProgram(bbm, bmlBlockPeg, bmlId, id, pb, localPegBoard, aniPlayer, lmp);
            tmu.setPriority(murmlDescription.getPriority());
            return tmu;
        }
        else
        {
            throw new TMUSetupException("No LMP specified in MURML ", null);
        }
    }

    private LMP parseParallel(Parallel par, FeedbackManager bbm, BMLBlockPeg bmlBlockPeg, String bmlId, String id,
            AnimationPlayer aniPlayer, PegBoard localPegBoard) throws TMUSetupException
    {
        Map<String, List<OrientConstraint>> ocMap = new HashMap<>(); // scope->ocVec map

        List<TimedAnimationUnit> lmps = new ArrayList<>();
        for (Static staticElem : par.getStatics())
        {
            List<OrientConstraint> ocVec = new ArrayList<OrientConstraint>();
            LMP lmpx = parseStaticElement(bbm, bmlBlockPeg, bmlId, id, aniPlayer, localPegBoard, staticElem, ocVec);
            if (lmpx != null)
            {
                lmps.add(lmpx);
            }
            if (ocVec.size() > 0) // if oc constraint, merge with other oc constraints
            {
                if (ocMap.containsKey(staticElem.getScope()))
                {
                    List<OrientConstraint> ocCur = ocMap.get(staticElem.getScope());
                    mergeStaticConstraints(ocCur, ocVec);
                }
                else
                {
                    ocMap.put(staticElem.getScope(), ocVec);
                }
            }
        }
        for (Dynamic dynamicElem : par.getDynamics())
        {
            if (dynamicElem.getKeyframing() != null)
            {
                // TODO
            }
            else
            {
                List<OrientConstraint> ocVec = new ArrayList<OrientConstraint>();
                LMP lmpx = parseProceduralDynamic(bbm, bmlBlockPeg, bmlId, id, aniPlayer, localPegBoard, dynamicElem, ocVec);
                if (lmpx != null)
                {
                    lmps.add(lmpx);
                }
                if (ocVec.size() > 0) // if oc constraint, merge with other oc constraints
                {
                    if (ocMap.containsKey(dynamicElem.getScope()))
                    {
                        List<OrientConstraint> ocCur = ocMap.get(dynamicElem.getScope());
                        mergeDynamicConstraints(ocCur, ocVec);
                    }
                    else
                    {
                        ocMap.put(dynamicElem.getScope(), ocVec);
                    }
                }
            }
        }
        for (Sequence seq : par.getSequences())
        {
            lmps.add(parseSequence(seq, bbm, bmlBlockPeg, bmlId, id, aniPlayer, localPegBoard));
        }

        for (Entry<String, List<OrientConstraint>> entry : ocMap.entrySet())
        {
            lmps.add(createAndAppendLMPWrist(entry.getKey(), bbm, bmlBlockPeg, bmlId, id, localPegBoard, aniPlayer, entry.getValue()));
        }
        return new LMPParallel(bbm, bmlBlockPeg, bmlId, id + "_lmppar" + UUID.randomUUID().toString().replaceAll("-", ""), localPegBoard,
                lmps);
    }

    private LMP parseSequence(Sequence seq, FeedbackManager bbm, BMLBlockPeg bmlBlockPeg, String bmlId, String id,
            AnimationPlayer aniPlayer, PegBoard localPegBoard) throws TMUSetupException
    {
        List<TimedAnimationUnit> lmps = new ArrayList<>();

        for (MovementConstraint mc : seq.getSequence())
        {
            if (mc instanceof Static)
            {
                Static staticElem = (Static) mc;
                List<OrientConstraint> ocVec = new ArrayList<OrientConstraint>();
                LMP lmpx = parseStaticElement(bbm, bmlBlockPeg, bmlId, id, aniPlayer, localPegBoard, staticElem, ocVec);
                if (lmpx != null)
                {
                    lmps.add(lmpx);
                }
                else
                {
                    lmps.add(createAndAppendLMPWrist(staticElem.getScope(), bbm, bmlBlockPeg, bmlId, id, localPegBoard, aniPlayer, ocVec));
                }
            }
            if (mc instanceof Dynamic)
            {
                Dynamic dynamicElem = (Dynamic) mc;
                if (dynamicElem.getKeyframing() != null)
                {
                    // TODO
                }
                else
                {
                    List<OrientConstraint> ocVec = new ArrayList<OrientConstraint>();
                    LMP lmpx = parseProceduralDynamic(bbm, bmlBlockPeg, bmlId, id, aniPlayer, localPegBoard, dynamicElem, ocVec);
                    if (lmpx != null)
                    {
                        lmps.add(lmpx);
                    }
                    else
                    {
                        lmps.add(createAndAppendLMPWrist(dynamicElem.getScope(), bbm, bmlBlockPeg, bmlId, id, localPegBoard, aniPlayer,
                                ocVec));
                    }
                }
            }
            if (mc instanceof Parallel)
            {
                lmps.add(parseParallel((Parallel) mc, bbm, bmlBlockPeg, bmlId, id, aniPlayer, localPegBoard));
            }
        }
        return new LMPSequence(bbm, bmlBlockPeg, bmlId, id + "_lmpseq" + UUID.randomUUID().toString().replaceAll("-", ""), localPegBoard,
                lmps);
    }

    private LMP parseStaticElement(FeedbackManager bbm, BMLBlockPeg bmlBlockPeg, String bmlId, String id, AnimationPlayer aniPlayer,
            PegBoard localPegBoard, Static staticElem, List<OrientConstraint> ocVec) throws TMUSetupException
    {
        switch (staticElem.getSlot())
        {
        case GazeDirection:
            break;
        case Neck:
            break;
        case HandLocation:
            return getStaticHandLocationElementTMU(staticElem.getScope(), staticElem, bbm, bmlBlockPeg, bmlId, id, localPegBoard, aniPlayer);
        case HandShape:
            return getStaticHandShapeElementTMU(staticElem.getScope(), staticElem, bbm, bmlBlockPeg, bmlId, id, localPegBoard, aniPlayer);
        case ExtFingerOrientation:
            ocVec.addAll(getStaticExtFingerOrientationOrientationTMU(staticElem.getScope(), staticElem, bbm, bmlBlockPeg, bmlId, id,
                    localPegBoard, aniPlayer));
            return null;
        case PalmOrientation:
            List<OrientConstraint> ocVecNew = getStaticPalmOrientationTMU(staticElem.getScope(), staticElem, bbm, bmlBlockPeg, bmlId, id,
                    localPegBoard, aniPlayer);
            ocVec.addAll(ocVecNew);
            LMP lmp = formPOMovement(staticElem.getScope(), staticElem, bbm, bmlBlockPeg, bmlId, id, localPegBoard, aniPlayer);
            if (ocVecNew.size() == 0 && lmp == null)
            {
                throw new TMUSetupException("Invalid PalmOrientation: " + staticElem.toXMLString(), null);
            }
            return lmp;
        }
        throw new TMUSetupException("Invalid slot " + staticElem.getSlot() + " in static.", null);
    }

    private LMP parseProceduralDynamic(FeedbackManager bbm, BMLBlockPeg bmlBlockPeg, String bmlId, String id, AnimationPlayer aniPlayer,
            PegBoard localPegBoard, Dynamic dyn, List<OrientConstraint> ocVec) throws TMUSetupException
    {
        switch (dyn.getSlot())
        {
        case GazeDirection:
            break;
        case Neck:
            break;
        case HandLocation:
            return getDynamicHandLocationElementsTMU(dyn, bbm, bmlBlockPeg, bmlId, id, localPegBoard, aniPlayer);
        case HandShape:
            return getDynamicHandShapeTMU(dyn, bbm, bmlBlockPeg, bmlId, id, localPegBoard, aniPlayer);
        case ExtFingerOrientation:
            ocVec.addAll(getExtFingerOrientationnElementsTMU(dyn, bbm, bmlBlockPeg, bmlId, id, localPegBoard, aniPlayer));
            return null;
        case PalmOrientation:
            List<OrientConstraint> ocVecNew = getDynamicPalmOrientationElementsTMU(dyn, bbm, bmlBlockPeg, bmlId, id, localPegBoard,
                    aniPlayer);
            ocVec.addAll(ocVecNew);
            LMP lmp = formPOMovement(dyn, bbm, bmlBlockPeg, bmlId, id, localPegBoard, aniPlayer);
            if (ocVecNew.size() == 0 && lmp == null)
            {
                throw new TMUSetupException("Invalid PalmOrientation: " + dyn.toXMLString(), null);
            }
            return lmp;
        }
        throw new TMUSetupException("Invalid slot " + dyn.getSlot() + " in dynamic.", null);
    }
}
