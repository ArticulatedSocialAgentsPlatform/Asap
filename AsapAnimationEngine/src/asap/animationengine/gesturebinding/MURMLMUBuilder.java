package asap.animationengine.gesturebinding;

import hmi.math.Quat4f;
import hmi.math.Vec3f;
import hmi.neurophysics.FittsLaw;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import asap.animationengine.ace.CurvedGStroke;
import asap.animationengine.ace.GStrokePhaseID;
import asap.animationengine.ace.GuidingSequence;
import asap.animationengine.ace.GuidingStroke;
import asap.animationengine.ace.LinearGStroke;
import asap.animationengine.ace.TPConstraint;
import asap.animationengine.ace.lmp.LMPWristPos;
import asap.animationengine.ace.lmp.MotorControlProgram;
import asap.animationengine.keyframe.MURMLKeyframeMU;
import asap.animationengine.motionunit.AnimationUnit;
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
import asap.murml.Phase;
import asap.murml.Posture;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.timemanipulator.EaseInEaseOutManipulator;

/**
 * Creates an animation unit from a MURML description
 * @author hvanwelbergen
 */
@Slf4j
public final class MURMLMUBuilder
{
    private MURMLMUBuilder()
    {
    }

    public static AnimationUnit setup(String murml)
    {
        MURMLDescription def = new MURMLDescription();
        def.readXML(murml);
        return setup(def);
    }

    /**
     * return start position [0..2] and swivel [3] of the wrist for this movement constraint in
     * body root coordinates.
     */
    private static boolean getStartConf(float[] result, DynamicElement elem, Hns hns)
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
    private static void appendSubTrajectory(List<DynamicElement> elements, String scope, GuidingSequence traj, TimedAnimationUnit tmu,
            Hns hns, FeedbackManager bbf, BMLBlockPeg bmlBlockPeg, String bmlId, String id, PegBoard pegBoard, MotorControlProgram mp)
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

                if (hns.getHandLocation(segment.getName("end"), ePos))
                {
                    // end position

                }
                else
                {
                    valid = false;
                }
                if (hns.getAbsoluteDirection(segment.getName("normal"), nVec))
                {

                }
                else
                {
                    valid = false;
                }

                /*
                 * if ( vn = segment->getValueFor("shape") ) {
                 * 
                 * // shape
                 * valid &= figure->HNStranslator.getElementShape( vn->getValue(), shape );
                 * }
                 */
                shape = hns.getElementShape(segment.getName("shape"));

                if (valid)
                {
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
                else
                {
                    log.warn("ArmMotorControl::appendSubtrajectory : invalid parameters for curved guiding stroke!");
                    return;
                }
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

    public static void getDynamicHandLocationElementsTMU(String scope, List<DynamicElement> elements, FeedbackManager bbm,
            BMLBlockPeg bmlBlockPeg, String bmlId, String id, PegBoard pb, Hns hns, MotorControlProgram mcp)
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

            appendSubTrajectory(elements, scope, trajectory, tmu, hns, bbm, bmlBlockPeg, bmlId, id, pb, mcp);
        }

        // TODO
        // // --- shall we retract at all?
        // if ( retrMode == RTRCT_FULL || retrMode == RTRCT_INTERMEDIATE )
        // {
        // //cout << ">>>> ArmMotorControl: Retraction mode = " << retrMode << endl;
        // // -- complete ongoing guiding sequence by appending a retracting stroke
        // // (estimate retraction duration by applying Fitts' law)
        // ePos = restPos;
        // MgcReal duration = LMP_WristPos::getPosDurationFromAmplitude( (ePos-sPos).Length()); // * 0.9;
        // eT = trajectory.getEndTime() + duration;
        // eT.mode = TPConstraint::Soft; // end time of retraction not of great importance
        // trajectory.addGuidingStroke( new LinearGStroke (GuidingStroke::STP_RETRACT, eT,ePos) );
        // }
        //
        // // -- build and append retracting lmp(s)
        // createPosLMP( trajectory, mp, lmp );
    }

    public static TimedAnimationUnit getKeyFramingTMU(Keyframing kf, FeedbackManager bbm, BMLBlockPeg bmlBlockPeg, String bmlId, String id,
            PegBoard pb)
    {
        AnimationUnit mu = getKeyFramingMU(kf);
        return mu.createTMU(bbm, bmlBlockPeg, bmlId, id, pb);
    }

    public static AnimationUnit getKeyFramingMU(Keyframing kf)
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

    public static AnimationUnit setup(MURMLDescription murmlDescription)
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

    public static TimedAnimationUnit setupTMU(MURMLDescription murmlDescription, FeedbackManager bbm, BMLBlockPeg bmlBlockPeg,
            String bmlId, String id, PegBoard pb, Hns hns)
    {
        MotorControlProgram mcp = new MotorControlProgram(bbm, bmlBlockPeg, bmlId, id);
        if (murmlDescription.getDynamic() != null)
        {
            Dynamic dyn = murmlDescription.getDynamic();
            if (dyn.getKeyframing() != null)
            {
                return getKeyFramingTMU(dyn.getKeyframing(), bbm, bmlBlockPeg, id, id, pb);
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
                    getDynamicHandLocationElementsTMU(dyn.getScope(), dyn.getDynamicElements(), bbm, bmlBlockPeg, id, id, pb, hns, mcp);
                case HandShape:
                    break;
                case ExtFingerOrientation:
                    break;
                case PalmOrientation:
                    break;
                }
            }
        }
        return mcp;
    }
}
