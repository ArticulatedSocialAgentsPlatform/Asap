/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.procanimation;

import hmi.animation.Hanim;
import hmi.animation.SkeletonInterpolator;
import hmi.animation.VJoint;
import hmi.animation.VJointUtils;
import hmi.math.Quat4f;
import hmi.math.Vec3f;
import hmi.util.Id;
import hmi.util.StringUtil;
import hmi.xml.XMLFormatting;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.lsmp.djep.xjep.XJep;
import org.nfunk.jep.ParseException;
import org.nfunk.jep.Variable;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.keyframe.KeyframeMU;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.motionunit.MUPlayException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.InvalidParameterException;
import asap.realizer.planunit.KeyPosition;
import asap.realizer.planunit.KeyPositionManager;
import asap.realizer.planunit.KeyPositionManagerImpl;
import asap.realizer.planunit.ParameterException;
import asap.realizer.planunit.ParameterNotFoundException;

import com.google.common.collect.ImmutableSet;

/**
 * Generic procedural animation package Animation is described as end effector
 * path, joint rotation path and/or joint positions at key times
 * 
 * Typical program flow: <br>
 * 1. load proc animation through XML <br>
 * 2. setup an IKBody <br>
 * 3. link the IKBody to the proc animation through the setup function<br>
 * 4. animate using the play function<br>
 * 
 * @author welberge
 * @author Mark ter Maat
 */
public class ProcAnimationMU extends XMLStructureAdapter implements AnimationUnit
{
    public Id id = null;

    private Blending blending = Blending.NO;

    private ArrayList<EndEffector> endeffector = new ArrayList<EndEffector>();

    private List<KeyframeMU> keyFrameMUs = new ArrayList<KeyframeMU>();

    private HashMap<String, Rotation> rotations = new HashMap<String, Rotation>();

    private HashMap<String, Keyframes> keyframes = new HashMap<String, Keyframes>();

    private List<VJoint> bodyParts = new ArrayList<VJoint>();

    private Set<String> bodyPartFilter = new HashSet<String>();

    private EndEffector rootEff = null;

    private EndEffector rightFootEff = null;

    private EndEffector leftFootEff = null;

    private EndEffector rightHandEff = null;

    private EndEffector leftHandEff = null;

    private double minDuration = 0;

    private double maxDuration = 0;

    private double prefDuration = 0;

    private XJep parser;

    private Variable tVar;

    private ArrayList<IKParameter> parameters = new ArrayList<IKParameter>();

    private IKBody body;

    private KeyPositionManager keyPositionManager = new KeyPositionManagerImpl();

    private VJoint vNext, vAdditive;

    private AnimationPlayer aniPlayer;

    /**
     * Constructor
     */
    public ProcAnimationMU()
    {
        setupParser();
    }

    public void addSkeletonInterpolator(SkeletonInterpolator ski)
    {
        keyFrameMUs.add(new KeyframeMU(ski));
    }

    /**
     * Gets the joints steered with this ProcAnimation
     */
    public List<VJoint> getControlledJoints()
    {
        return bodyParts;
    }

    @Override
    public KeyPosition getKeyPosition(String name)
    {
        return keyPositionManager.getKeyPosition(name);
    }

    @Override
    public void addKeyPosition(KeyPosition kp)
    {
        keyPositionManager.addKeyPosition(kp);
    }

    @Override
    public void removeKeyPosition(String id)
    {
        keyPositionManager.removeKeyPosition(id);
    }

    @Override
    public List<KeyPosition> getKeyPositions()
    {
        return keyPositionManager.getKeyPositions();
    }

    @Override
    public void setKeyPositions(List<KeyPosition> p)
    {
        keyPositionManager.setKeyPositions(p);
    }

    public void mirror()
    {
        if (body != null)
        {
            bodyParts.clear();
            bodyParts.addAll(body.getHuman().getParts());
        }
        for (EndEffector e : endeffector)
        {
            e.mirror();
        }
        setupEndEffectors();

        ArrayList<Keyframes> keyFr = new ArrayList<Keyframes>();
        for (Keyframes kf : keyframes.values())
        {
            keyFr.add(kf);
            kf.mirror();
            if (body != null)
            {
                kf.setTarget(body.getHuman());
            }
        }
        keyframes.clear();
        for (Keyframes kf : keyFr)
        {
            keyframes.put(kf.getTarget(), kf);
        }

        ArrayList<Rotation> rots = new ArrayList<Rotation>();
        for (Rotation r : rotations.values())
        {
            rots.add(r);
            r.mirror();
        }
        rotations.clear();
        for (Rotation r : rots)
        {
            rotations.put(r.getTarget(), r);
        }

        for (KeyframeMU kmu : keyFrameMUs)
        {
            kmu.setParameterValue("mirror", "true");
        }

        if (body != null)
        {
            filterBodyParts();
        }
    }

    /**
     * Creates a copy of this ProcAnimation. The copy is not linked to an ikbody
     */
    public ProcAnimationMU deepCopy() throws MUSetupException
    {
        ProcAnimationMU copy = new ProcAnimationMU();
        copy.id = id;

        ArrayList<KeyPosition> keys = new ArrayList<KeyPosition>();
        for (KeyPosition key : getKeyPositions())
        {
            keys.add(key.deepCopy());
        }
        copy.setKeyPositions(keys);

        ArrayList<EndEffector> effectors = new ArrayList<EndEffector>();
        for (EndEffector e : endeffector)
        {
            effectors.add(e.deepCopy());
        }
        copy.setEndEffectors(effectors);

        HashMap<String, Keyframes> keyfr = new HashMap<String, Keyframes>();
        for (Keyframes kf : keyframes.values())
        {
            keyfr.put(kf.getTarget(), kf.deepCopy());
        }
        copy.setKeyframes(keyfr);

        HashMap<String, Rotation> rots = new HashMap<String, Rotation>();
        for (Rotation r : rotations.values())
        {
            try
            {
                rots.put(r.getTarget(), r.deepCopy());
            }
            catch (ParseException e1)
            {
                MUSetupException e = new MUSetupException(e1.getMessage(), this);
                e.initCause(e1);
                throw e;
            }
        }
        copy.setRotations(rots);

        ArrayList<Parameter> params = new ArrayList<Parameter>();
        for (IKParameter p : parameters)
        {
            params.add(p.param.deepCopy());
        }
        copy.setParameters(params);

        copy.keyFrameMUs = new ArrayList<KeyframeMU>();
        for (KeyframeMU kmu : keyFrameMUs)
        {
            copy.addSkeletonInterpolator(new SkeletonInterpolator(kmu.getSkeletonInterpolator(), null));
        }

        copy.setMinDuration(minDuration);
        copy.setMaxDuration(maxDuration);
        copy.setPrefDuration(prefDuration);

        copy.bodyPartFilter.addAll(bodyPartFilter);
        copy.blending = blending;
        return copy;
    }

    @Override
    public double getPreferedDuration()
    {
        return prefDuration;
    }

    /**
     * Sets to ani, copies over all of ani's properties
     */
    public void set(ProcAnimationMU ani) throws MUSetupException
    {
        id = ani.id;

        ArrayList<KeyPosition> keys = new ArrayList<KeyPosition>();
        for (KeyPosition key : ani.getKeyPositions())
        {
            keys.add(key.deepCopy());
        }
        setKeyPositions(keys);

        ArrayList<EndEffector> effectors = new ArrayList<EndEffector>();
        for (EndEffector e : ani.endeffector)
        {
            effectors.add(e.deepCopy());
        }
        setEndEffectors(effectors);

        HashMap<String, Rotation> rots = new HashMap<String, Rotation>();
        for (Rotation r : ani.rotations.values())
        {
            try
            {
                rots.put(r.getTarget(), r.deepCopy());
            }
            catch (ParseException e1)
            {
                MUSetupException e = new MUSetupException(e1.getMessage(), this);
                e.initCause(e1);
                throw e;
            }
        }
        setRotations(rots);

        HashMap<String, Keyframes> frames = new HashMap<String, Keyframes>();
        for (Keyframes fr : ani.keyframes.values())
        {
            frames.put(fr.getTarget(), fr.deepCopy());
        }
        setKeyframes(frames);

        ArrayList<Parameter> params = new ArrayList<Parameter>();
        for (IKParameter p : ani.parameters)
        {
            params.add(p.param.deepCopy());
        }
        setParameters(params);

        keyFrameMUs = new ArrayList<KeyframeMU>();
        for (KeyframeMU kmu : ani.keyFrameMUs)
        {
            addSkeletonInterpolator(kmu.getSkeletonInterpolator());
            // skelInterpolators.add(new SkeletonInterpolator(ip,
            // ip.getTargetParts()));
        }
        setMinDuration(ani.minDuration);
        setMaxDuration(ani.maxDuration);
        setPrefDuration(ani.prefDuration);
        blending = ani.blending;
    }

    /**
     * Removes a rotation
     */
    public void removeRotation(String name)
    {
        rotations.remove(name);
    }

    /**
     * Sets a rotation to a certain formula, adds it to the rotation list if it
     * does not exist yet
     * 
     * @param name
     *            name of the target
     * @param xForm
     *            x formula
     * @param yForm
     *            y formula
     * @param zForm
     *            z formula
     */
    public void setRotation(String name, String xForm, String yForm, String zForm) throws ParseException
    {
        Rotation rot = rotations.get(name);
        if (rot == null)
        {
            rot = new Rotation(parser, name);
            rotations.put(name, rot);
        }
        rot.setRotation(0, xForm);
        rot.setRotation(1, yForm);
        rot.setRotation(2, zForm);
    }

    /**
     * Sets a end effector to a certain formula, adds it to the endeffector list
     * if it does not exist yet
     * 
     * @param name
     *            name of the target
     * @param xForm
     *            x formula
     * @param yForm
     *            y formula
     * @param zForm
     *            z formula
     */
    public void setEndEffector(String name, String xForm, String yForm, String zForm, String sForm, boolean isLocal)
    {
        EndEffector eff = getEndEffector(name);
        if (eff == null)
        {
            eff = new EndEffector(parser, name);
            endeffector.add(eff);
            setupEndEffectors();
        }
        eff.setTranslation(0, xForm);
        eff.setTranslation(1, yForm);
        eff.setTranslation(2, zForm);
        eff.setSwivel(sForm);
        eff.setLocal(isLocal);
    }

    /**
     * Set the rotations
     * 
     * @param rotations
     *            The rotations to set.
     */
    public void setRotations(HashMap<String, Rotation> rots)
    {
        rotations.clear();
        for (Rotation r : rots.values())
        {
            Rotation rNew = new Rotation(parser);
            rNew.set(r);
            rotations.put(rNew.getTarget(), rNew);
        }
    }

    /**
     * Sets the parameter value for parameter sid. Ignores parameter sids that
     * are not used in this ProcAnimation.
     */
    @Override
    public void setFloatParameterValue(String sid, float value)
    {
        for (IKParameter p : parameters)
        {
            if (p.param.getSid().equals(sid))
            {
                p.param.setValue(value);
                p.tParam.setValue(value);
            }
        }
    }

    /**
     * Set the keyframes to frs
     */
    public void setKeyframes(HashMap<String, Keyframes> frs)
    {
        keyframes.clear();
        for (Keyframes fr : frs.values())
        {
            Keyframes fNew = new Keyframes(parser);
            fNew.set(fr);
            keyframes.put(fr.getTarget(), fNew);
        }
    }

    /**
     * Copies the rotations from another ProcAnimation Rotations that occur in
     * ik but not in this ProcAnimation are ignored
     * 
     * @param ik
     *            the IKAnimation to copy from
     */
    public void setRotations(ProcAnimationMU ik)
    {
        for (Rotation rotSrc : ik.rotations.values())
        {
            Rotation rotDst = rotations.get(rotSrc.getTarget());
            rotDst.set(rotSrc);
        }
    }

    private void setParameterValues()
    {
        for (IKParameter ikp : parameters)
        {
            ikp.tParam.setValue(ikp.param.getValue());
        }
    }

    /**
     * Sets the parameters the parameters in ps, retaining the 'old' parameters
     * that not are part of ps
     * 
     * @param parameters
     *            - the list of parameters to set
     */
    public void setParameters(Collection<Parameter> ps)
    {
        ArrayList<IKParameter> paramsToRemove = new ArrayList<IKParameter>();
        // parameters.clear();
        for (Parameter p : ps)
        {
            IKParameter ikp = getIKParameter(p.getSid());
            if (ikp != null)
            {
                paramsToRemove.add(ikp);
            }
        }

        parameters.removeAll(paramsToRemove);
        for (Parameter p : ps)
        {
            addParameter(p);
        }
    }

    private IKParameter getIKParameter(String paramId)
    {
        for (IKParameter p : parameters)
        {
            if (p.param.getSid().equals(paramId))
            {
                return p;
            }
        }
        return null;
    }

    /**
     * Adds a keyframe
     */
    public void addKeyframes(Keyframes kf)
    {
        keyframes.put(kf.getTarget(), kf);
    }

    /**
     * Adds a parameter
     */
    public void addParameter(Parameter p)
    {
        IKParameter ikp = new IKParameter();
        ikp.param = p;
        parser.addVariable(p.getSid(), p.getValue());
        ikp.tParam = parser.getVar(p.getSid());
        ikp.tParam.setValue(p.getValue());
        parameters.add(ikp);
    }

    /**
     * Set the endeffectors of another ProcAnimation into this animation.
     * EndEffectors that occur only in ik are ignored.
     */
    public void setEffectors(ProcAnimationMU ik)
    {
        for (EndEffector effSrc : ik.endeffector)
        {
            for (EndEffector effDst : endeffector)
            {
                if (effSrc.getTarget().equals(effDst.getTarget()))
                {
                    effDst.set(effSrc);
                    break;
                }
            }
        }
    }

    /**
     * Set a new set of EndEffectors
     * 
     * @param the
     *            new list of EndEffectors
     */
    public void setEndEffectors(ArrayList<EndEffector> newEndEffectors)
    {
        endeffector.clear();
        for (EndEffector eff : newEndEffectors)
        {
            EndEffector e = new EndEffector(parser, eff.getTarget());
            e.set(eff);
            endeffector.add(e);
        }
        setupEndEffectors();
    }

    /**
     * Get the EndEffector with sid target
     * 
     * @param target
     *            the sid of the endeffector to obtain
     * @return the endeffector, null if not found
     */
    public EndEffector getEndEffector(String target)
    {
        for (EndEffector eff : endeffector)
        {
            if (eff.getTarget().equals(target))
            {
                return eff;
            }
        }
        return null;
    }

    /**
     * Returns the complete arraylist with EndEffectors
     * 
     * @return the EndEffectors
     */
    public List<EndEffector> getAllEndEffectors()
    {
        return endeffector;
    }

    /**
     * Adds an endeffector
     * 
     * @param eff
     *            endeffector to add
     */
    public void addEndEffector(EndEffector eff)
    {
        endeffector.add(eff);
    }

    /**
     * Removes all endeffectors with target name
     * 
     * @param name
     *            the target
     */
    public void removeEndEffector(String name)
    {
        ArrayList<EndEffector> remove = new ArrayList<EndEffector>();
        for (EndEffector eff : endeffector)
        {
            if (eff.getTarget().equals(name))
            {
                remove.add(eff);
                if (eff == rootEff) rootEff = null;
                if (eff == rightFootEff) rightFootEff = null;
                if (eff == rightHandEff) rightHandEff = null;
                if (eff == leftFootEff) leftFootEff = null;
                if (eff == leftHandEff) leftHandEff = null;
            }
        }
        endeffector.removeAll(remove);
    }

    /**
     * @return Returns the rotations.
     */
    public HashMap<String, Rotation> getRotations()
    {
        return rotations;
    }

    /**
     * Returns the Rotation of the given target
     * 
     * @param the
     *            target
     * @return the rotation.
     */
    public Rotation getRotation(String target)
    {
        return rotations.get(target);
    }

    /**
     * Get the current endeffector IK info
     * 
     * @param lf
     *            output: left foot vector
     * @param rf
     *            output: right foot position
     * @param lh
     *            output: left hand position
     * @param rh
     *            output: right hand position
     * @param root
     *            output: root position
     * @param swivels
     *            output: swivel rotations of respectively left foot, rightfoot,
     *            lefthand, right hand
     */
    public void getPositions(float lf[], float rf[], float lh[], float rh[], float root[], double swivels[])
    {
        if (rootEff != null)
        {
            rootEff.evaluateTrans(root);
        }
        else
        {
            VJoint hRoot = body.getHuman().getPart(Hanim.HumanoidRoot);
            hRoot.getPathTranslation(null, root);
        }

        if (leftFootEff != null)
        {
            swivels[0] = leftFootEff.evaluateSwivel();
            leftFootEff.evaluateTrans(lf);
        }
        else
        {
            VJoint lAnkle = body.getHuman().getPart(Hanim.l_ankle);
            lAnkle.getPathTranslation(null, lf);
            swivels[0] = body.getSwivelLeftFoot();
        }
        if (rightFootEff != null)
        {
            swivels[1] = rightFootEff.evaluateSwivel();
            rightFootEff.evaluateTrans(rf);
        }
        else
        {
            VJoint rAnkle = body.getHuman().getPart(Hanim.r_ankle);
            rAnkle.getPathTranslation(null, rf);
            swivels[1] = body.getSwivelRightFoot();
        }
        if (leftHandEff != null)
        {
            swivels[2] = leftHandEff.evaluateSwivel();
            leftHandEff.evaluateTrans(lh);
            if (leftHandEff.isLocal())
            {
                VJoint lShoulder = body.getHuman().getPart(Hanim.l_shoulder);
                lShoulder.getParent().pathTransform(null, lh);
            }
        }
        else
        {
            VJoint lWrist = body.getHuman().getPart(Hanim.l_wrist);
            lWrist.getPathTranslation(null, lh);
            swivels[2] = body.getSwivelLeftArm();
        }

        if (rightHandEff != null)
        {
            swivels[3] = rightHandEff.evaluateSwivel();
            rightHandEff.evaluateTrans(rh);
            if (rightHandEff.isLocal())
            {
                VJoint rShoulder = body.getHuman().getPart(Hanim.r_shoulder);
                float temp[] = new float[3];
                Vec3f.set(temp, rh);
                rShoulder.getParent().pathTransform(null, temp);
                Vec3f.set(rh, temp);
            }
        }
        else
        {
            VJoint rWrist = body.getHuman().getPart(Hanim.r_wrist);
            rWrist.getPathTranslation(null, rh);
            swivels[3] = body.getSwivelRightArm();
        }
    }

    /**
     * Gets the parameters
     * 
     * @return the parameters
     */
    public Collection<Parameter> getParameters()
    {
        ArrayList<Parameter> params = new ArrayList<Parameter>();
        for (IKParameter p : parameters)
        {
            params.add(p.param);
        }
        return params;
    }

    private void setupEndEffectors()
    {
        rootEff = null;
        leftFootEff = null;
        rightFootEff = null;
        leftHandEff = null;
        rightHandEff = null;
        for (EndEffector eff : endeffector)
        {
            if (eff.getTarget().equals("root") || eff.getTarget().equals(Hanim.HumanoidRoot))
            {
                rootEff = eff;
            }
            else if (eff.getTarget().equals("l_ankle"))
            {
                leftFootEff = eff;
            }
            else if (eff.getTarget().equals("r_ankle"))
            {
                rightFootEff = eff;
            }
            else if (eff.getTarget().equals("l_wrist"))
            {
                leftHandEff = eff;
            }
            else if (eff.getTarget().equals("r_wrist"))
            {
                rightHandEff = eff;
            }
        }
    }

    /**
     * Links the ProcAnimation to a body and a set of parameters Filters out the
     * joints to animate based on the set Rotations, end effectors, keyframes
     * and skeletoninterpolators
     */
    public void setup(Collection<Parameter> params, IKBody b)
    {
        body = b;
        bodyParts = new ArrayList<VJoint>();
        bodyParts.addAll(body.getHuman().getParts());

        for (Rotation r : rotations.values())
        {
            r.setTarget(body.getHuman());
        }
        for (Keyframes fr : keyframes.values())
        {
            fr.setTarget(body.getHuman());
        }

        for (KeyframeMU kmu : keyFrameMUs)
        {
            kmu.setTarget(body.getHuman());
        }

        setupEndEffectors();
        setParameters(params);
        findMissingParameters();
        filterBodyParts();
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "ES_COMPARING_STRINGS_WITH_EQ", justification = "String is interned")
    private void filterBodyParts()
    {
        if (bodyPartFilter.size() > 0)
        {
            for (KeyframeMU kmu : keyFrameMUs)
            {
                kmu.filterJoints(bodyPartFilter);
            }
        }

        ArrayList<VJoint> deleteList = new ArrayList<VJoint>();
        for (VJoint v : bodyParts)
        {
            boolean found = false;
            for (KeyframeMU kmu : keyFrameMUs)
            {
                if (kmu.getJoints().contains(v.getSid())) found = true;
            }
            if (found) continue;

            if (rotations.get(v.getSid()) != null) continue;
            if (keyframes.get(v.getSid()) != null) continue;
            if ((v.getSid() == Hanim.r_shoulder || v.getSid() == Hanim.r_elbow) && rightHandEff != null) continue;
            if ((v.getSid() == Hanim.l_shoulder || v.getSid() == Hanim.l_elbow) && leftHandEff != null) continue;
            if ((v.getSid() == Hanim.r_hip || v.getSid() == Hanim.r_knee || v.getSid() == Hanim.r_ankle) && rightFootEff != null) continue;
            if ((v.getSid() == Hanim.l_hip || v.getSid() == Hanim.l_knee || v.getSid() == Hanim.l_ankle) && leftFootEff != null) continue;
            deleteList.add(v);
        }

        if (bodyPartFilter.size() > 0)
        {
            for (VJoint v : bodyParts)
            {
                if (!bodyPartFilter.contains(v.getSid()))
                {
                    deleteList.add(v);
                }

                // TODO: remove endeffectors completely if not all their joints
                // are in the filter

            }
        }
        bodyParts.removeAll(deleteList);
        if (aniPlayer != null)
        {
            aniPlayer.filterAdditiveBody(vAdditive, this.getAdditiveJoints());
        }
        sortBodyParts();
    }

    private void sortBodyParts()
    {
        Collections.sort(bodyParts, new Comparator<VJoint>()
        {

            @Override
            public int compare(VJoint vj1, VJoint vj2)
            {
                int pl1 = body.getHuman().getPath(vj1).size();
                int pl2 = body.getHuman().getPath(vj2).size();
                if (pl1 < pl2)
                {
                    return -1;
                }
                else if (pl2 < pl1)
                {
                    return 1;
                }
                else return 0;
            }
        });
    }

    private void findMissingParameters()
    {
        @SuppressWarnings("rawtypes")
        Vector v = new Vector();
        for (Rotation r : rotations.values())
        {
            v = r.findParameters(v);
        }
        for (Keyframes fr : keyframes.values())
        {
            v = fr.findParameters(v);
        }
        for (EndEffector eff : endeffector)
        {
            v = eff.findParameters(v);
        }

        for (int i = 0; i < v.size(); i++) // For all parameters in the
        // formula's
        {
            String par = v.get(i).toString();
            par = par.substring(0, par.indexOf(":"));
            if (containsOnlyLetters(par) && !parameterExists(par) && !par.equals("t") && !isConstantParameter(par)) // If the
            // parameter
            // is not
            // defined
            {
                Parameter p = new Parameter();
                p.setSid(par);
                p.setValue(0);
                addParameter(p);
            }
        }
    }

    /**
     * Checks if the given String only contains letters
     */
    private boolean containsOnlyLetters(String str)
    {
        char[] charList = str.toCharArray();
        for (char ch : charList)
        {
            if (!Character.isLetter(ch))
            {
                return false;
            }
        }
        return true;
    }

    private boolean isConstantParameter(String str)
    {
        Variable v = parser.getVar(str);
        if (v != null)
        {
            if (v.isConstant())
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a parameter exists with the given name
     */
    private boolean parameterExists(String str)
    {
        for (IKParameter p : parameters)
        {
            if (p.param.getSid().equals(str))
            {
                return true;
            }
        }
        return false;
    }

    /*
     * temp vars for speedup
     */
    private float lf[] = new float[3];

    private float rf[] = new float[3];

    private float lh[] = new float[3];

    private float rh[] = new float[3];

    private float goal[] = new float[3];

    private float q[] = new float[4];

    private float ro[] = new float[3];

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "ES_COMPARING_STRINGS_WITH_EQ", justification = "Strings are interned")
    private void play(double t, VJoint v)
    {
        if ((v.getSid() == Hanim.r_hip || v.getSid() == Hanim.r_knee || v.getSid() == Hanim.r_ankle) && rightFootEff != null)
        {
            if (v.getSid() == Hanim.r_hip)
            {
                body.setSwivelRightFoot(rightFootEff.evaluateSwivel());
                rightFootEff.evaluateTrans(rf);
                body.setRightFoot(rf, true);
            }
        }
        else if ((v.getSid() == Hanim.l_hip || v.getSid() == Hanim.l_knee || v.getSid() == Hanim.l_ankle) && leftFootEff != null)
        {
            if (v.getSid() == Hanim.l_hip)
            {
                leftFootEff.evaluateTrans(lf);
                body.setSwivelLeftFoot(leftFootEff.evaluateSwivel());
                body.setLeftFoot(lf, true);
            }
        }
        else if ((v.getSid() == Hanim.r_shoulder || v.getSid() == Hanim.r_elbow) && rightHandEff != null)
        {
            if (v.getSid() == Hanim.r_shoulder)
            {
                body.setSwivelRightHand(rightHandEff.evaluateSwivel());
                rightHandEff.evaluateTrans(rh);
                if (rightHandEff.isLocal())
                {
                    body.setLocalRightHand(rh);
                }
                else
                {
                    body.setRightHand(rh);
                }
            }
        }
        else if ((v.getSid() == Hanim.l_shoulder || v.getSid() == Hanim.l_elbow) && leftHandEff != null)
        {
            if (v.getSid() == Hanim.l_shoulder)
            {
                body.setSwivelLeftHand(leftHandEff.evaluateSwivel());
                leftHandEff.evaluateTrans(lh);
                if (leftHandEff.isLocal())
                {
                    body.setLocalLeftHand(lh);
                }
                else
                {
                    body.setLeftHand(lh);
                }
            }
        }
        else
        {
            Rotation r = rotations.get(v.getSid());
            Keyframes kfs = keyframes.get(v.getSid());

            if (r != null)
            {
                if (r.evaluate(ro))
                {
                    Quat4f.setFromRollPitchYaw(q, ro[2], ro[0], ro[1]);
                    if (!r.isLocal())
                    {
                        v.setPathRotation(q, body.getHuman());
                    }
                    else
                    {
                        v.setRotation(q);
                    }
                }
            }
            else if (kfs != null)
            {
                kfs.evaluate(t, q, body.getHuman());
                v.setRotation(q);
            }

        }
    }

    /**
     * Play the IKAnimation at time 0 &lt t &lt 1 First plays all
     * skeletoninterpolators, then sets the joints starting at the root as
     * specified by IK and keyframes
     * 
     * @param t
     *            0 &lt;= t &lt;= 1 the time position
     */
    @Override
    public void play(double t)
    {
        tVar.setValue(t);
        setParameterValues();

        for (KeyframeMU kmu : keyFrameMUs)
        {
            kmu.play(t);
        }

        if (rootEff != null)
        {
            rootEff.evaluateTrans(goal);
            body.setRoot(goal);
        }
        for (VJoint v : bodyParts)
        {
            play(t, v);
        }
    }

    /**
     * @return Returns the maxDuration in seconds
     */
    public double getMaxDuration()
    {
        return maxDuration;
    }

    /**
     * @param maxDuration
     *            The new maxDuration in seconds
     */
    public void setMaxDuration(double maxDuration)
    {
        this.maxDuration = maxDuration;
    }

    /**
     * @return Returns the minDuration in seconds
     */
    public double getMinDuration()
    {
        return minDuration;
    }

    /**
     * @param minDuration
     *            The minDuration to set, in seconds
     */
    public void setMinDuration(double minDuration)
    {
        this.minDuration = minDuration;
    }

    /**
     * @return Returns the prefered duration (in seconds) of this ProcAnimation
     */
    public double getPrefDuration()
    {
        return prefDuration;
    }

    /**
     * @param prefDuration
     *            the prefered duration (in seconds) of this ProcAnimation
     */
    public void setPrefDuration(double prefDuration)
    {
        this.prefDuration = prefDuration;
    }

    /**
     * Get the id string
     */
    @Override
    public String toString()
    {
        if (id != null)
        {
            return id.toString();
        }
        return "";
    }

    private void setupParser()
    {
        parser = new XJep();
        parser.addStandardFunctions();
        parser.addStandardConstants();
        parser.addFunction("noise", new PerlinNoiseJEP());
        parser.addFunction("hermite", new HermiteSplineJEP());
        parser.addFunction("tcbspline", new TCBSplineJEP());
        parser.addVariable("t", 0);
        parser.setAllowUndeclared(true);
        tVar = parser.getVar("t");
    }

    /**
     * @return the left foot endeffector, null if not used
     */
    public EndEffector getLeftFootEff()
    {
        return leftFootEff;
    }

    /**
     * @return the left hand endeffector, null if not used
     */
    public EndEffector getLeftHandEff()
    {
        return leftHandEff;
    }

    /**
     * @return the right foot endeffector, null if not used
     */
    public EndEffector getRightFootEff()
    {
        return rightFootEff;
    }

    /**
     * @return the right hand endeffector, null if not used
     */
    public EndEffector getRightHandEff()
    {
        return rightHandEff;
    }

    /**
     * @return the root end effector, null if not used
     */
    public EndEffector getRootEff()
    {
        return rootEff;
    }

    @Override
    public synchronized ProcAnimationMU copy(AnimationPlayer p) throws MUSetupException
    {
        ProcAnimationMU mu = copy(p.getVNext(), p.constructAdditiveBody());
        mu.aniPlayer = p;
        mu.setup2();        
        return mu;
    }

    public void setup(AnimationPlayer p)
    {
        this.aniPlayer = p;
        setup2(p.getVNext(), p.constructAdditiveBody());
    }

    public void setup(VJoint vNext)
    {
        body = new IKBody(vNext);
        setup(getParameters(), body);
    }

    private void setup2(VJoint vN, VJoint vAdd)
    {
        this.vAdditive = vAdd;
        this.vNext = vN;
        setup2();
    }

    private void setup2()
    {
        VJoint vj;
        switch (blending)
        {
        case ADDITIVE:
            vj = vAdditive;
            break;
        default:
            vj = vNext;
        }
        if (vj == null) return;
        setup(vj);
        for (Keyframes kf : keyframes.values())
        {
            kf.setJoint(vj.getPart(kf.getTarget()));
        }
        for (Rotation r : rotations.values())
        {
            r.setJoint(vj.getPart(r.getTarget()));
        }
        for (KeyframeMU kmu : keyFrameMUs)
        {
            kmu.setTarget(vj);
        }
    }

    /**
     * Creates a copy of this ProcAnimation and links is to VJoint v
     */
    public ProcAnimationMU copy(VJoint vNext, VJoint vAdditive) throws MUSetupException
    {
        ProcAnimationMU copy = deepCopy();
        copy.setup2(vNext, vAdditive);
        return copy;
    }

    public ProcAnimationMU copy(VJoint vNext) throws MUSetupException
    {
        return copy(vNext, vNext);
    }

    static class IKParameter
    {
        Parameter param;

        Variable tParam;
    }

    /*
     * =========================================== IMPLEMENTATION OF
     * XMLSTRUCTURE INTERFACES ===========================================
     */
    @Override
    public boolean decodeAttribute(String attrName, String attrValue)
    {
        if (attrName.equals("prefDuration"))
        {
            prefDuration = Double.parseDouble(attrValue);
        }
        else if (attrName.equals("minDuration"))
        {
            minDuration = Double.parseDouble(attrValue);
        }
        else if (attrName.equals("maxDuration"))
        {
            maxDuration = Double.parseDouble(attrValue);
        }
        else
        {
            return false;
        }
        return true;
    }

    @Override
    public boolean decodeAttribute(String attrName, String attrValue, XMLTokenizer tokenizer)
    {
        return decodeAttribute(attrName, attrValue);
    }

    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws java.io.IOException
    {
        while (!tokenizer.atETag())
        {
            if (tokenizer.atSTag("EndEffector"))
            {
                EndEffector eff = new EndEffector(parser);
                eff.readXML(tokenizer);
                endeffector.add(eff);
            }
            else if (tokenizer.atSTag("Rotation"))
            {
                Rotation rot = new Rotation(parser);
                rot.readXML(tokenizer);
                // no need to add empty rotations
                if (!(rot.getRotationFormula(0) == null && rot.getRotationFormula(1) == null && rot.getRotationFormula(2) == null))
                {
                    if (!(rot.getRotationFormula(0).equals("") && rot.getRotationFormula(1).equals("") && rot.getRotationFormula(2).equals(
                            "")))
                    {
                        rotations.put(rot.getTarget(), rot);
                    }
                }
            }
            else if (tokenizer.atSTag("Keyframes"))
            {
                Keyframes frames = new Keyframes(parser);
                frames.readXML(tokenizer);
                addKeyframes(frames);
            }
            else if (tokenizer.atSTag("SkeletonInterpolator"))
            {
                SkeletonInterpolator ip = new SkeletonInterpolator(tokenizer);
                // TODO: not always necesary...
                // lowPassFilter(ip);
                addSkeletonInterpolator(ip);
            }
            else if (tokenizer.atSTag("Parameter"))
            {
                Parameter param = new Parameter();
                param.readXML(tokenizer);
                addParameter(param);
            }
            else if (tokenizer.atSTag("KeyPosition"))
            {
                KeyPosition k = new KeyPosition();
                k.readXML(tokenizer);
                addKeyPosition(k);
            }
            else if (tokenizer.atSTag("CutPosition"))
            {
                throw new RuntimeException("CutPositions have been deprecated, please remove from XML file");
            }
        }
    }

    @Override
    public StringBuilder appendContent(StringBuilder buf, XMLFormatting fmt)
    {
        for (Keyframes kf : keyframes.values())
        {
            kf.appendXML(buf, fmt);
        }
        for (EndEffector ef : endeffector)
        {
            ef.appendXML(buf, fmt);
        }
        for (Rotation r : rotations.values())
        {
            r.appendXML(buf, fmt);
        }
        for (KeyframeMU kmu : keyFrameMUs)
        {
            kmu.getSkeletonInterpolator().appendXML(buf, fmt);
        }
        for (KeyPosition k : getKeyPositions())
        {
            k.appendXML(buf, fmt);
        }
        for (IKParameter par : parameters)
        {
            par.param.appendXML(buf, fmt);
        }
        return buf;
    }

    @Override
    public StringBuilder appendAttributeString(StringBuilder buf)
    {
        appendAttribute(buf, "prefDuration", prefDuration);
        appendAttribute(buf, "minDuration", minDuration);
        appendAttribute(buf, "maxDuration", maxDuration);
        return buf;
    }

    @Override
    public String getXMLTag()
    {
        return "ProcAnimation";
    }

    private enum Blending
    {
        NO, ADDITIVE;
    }

    @Override
    public void setParameterValue(String name, String value) throws ParameterException
    {
        if (name.equals("mirror"))
        {
            if (Boolean.parseBoolean(value))
            {
                mirror();
            }
        }
        else if (name.equals("blending"))
        {
            blending = Blending.valueOf(value);
            setup2();
        }
        else if (name.equals("joints"))
        {
            String joints[] = value.split("\\s");

            for (String joint : joints)
            {
                bodyPartFilter.add(joint);
            }
            filterBodyParts();
        }
        else if (StringUtil.isNumeric(value))
        {
            setFloatParameterValue(name, Float.parseFloat(value));
        }
        else
        {
            throw new InvalidParameterException(name, value);
        }
    }

    @Override
    public String getParameterValue(String name) throws ParameterNotFoundException
    {
        for (Parameter p : getParameters())
        {
            if (p.getSid().equals(name)) return "" + p.getValue();
        }
        throw new ParameterNotFoundException(name);
    }

    @Override
    public float getFloatParameterValue(String name) throws ParameterNotFoundException
    {
        for (Parameter p : getParameters())
        {
            if (p.getSid().equals(name)) return (float) p.getValue();
        }
        throw new ParameterNotFoundException(name);
    }

    @Override
    public TimedAnimationMotionUnit createTMU(FeedbackManager bfm, BMLBlockPeg bbPeg, String bmlId, String id, PegBoard pb)
    {
        return new TimedAnimationMotionUnit(bfm, bbPeg, bmlId, id, this, pb, aniPlayer);
    }

    private static final Set<String> PHJOINTS = ImmutableSet.of();

    @Override
    public Set<String> getPhysicalJoints()
    {
        return PHJOINTS;
    }

    @Override
    public Set<String> getAdditiveJoints()
    {
        if (blending.equals(Blending.ADDITIVE))
        {
            return VJointUtils.transformToSidSet(bodyParts);
        }

        return ImmutableSet.of();
    }

    @Override
    public Set<String> getKinematicJoints()
    {
        if (blending.equals(Blending.ADDITIVE)) return ImmutableSet.of();
        return VJointUtils.transformToSidSet(bodyParts);
    }

    @Override
    public void startUnit(double t) throws MUPlayException
    {
    }
    
    @Override
    public void cleanup()
    {
        if(aniPlayer!=null)
        {
            aniPlayer.removeAdditiveBody(vAdditive);
        }
    }
}
