/*******************************************************************************
 *******************************************************************************/
package asap.faceengine.faceunit;

import hmi.faceanimation.FaceController;
import hmi.faceanimation.converters.EmotionConverter;
import hmi.faceanimation.converters.FACSConverter;
import hmi.util.StringUtil;

import java.util.Set;

import lombok.Delegate;
import lombok.extern.slf4j.Slf4j;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.InvalidParameterException;
import asap.realizer.planunit.KeyPosition;
import asap.realizer.planunit.KeyPositionManager;
import asap.realizer.planunit.KeyPositionManagerImpl;
import asap.realizer.planunit.ParameterException;
import asap.realizer.planunit.ParameterNotFoundException;

import com.google.common.base.Joiner;
import com.google.common.util.concurrent.AtomicDouble;

/**
 * A basic facial animation unit consisting of one morph target. The key
 * positions are: start, attackPeak, relax, end. This descripes an apex-like
 * intensity development: The between start and attackPeak, the morph target is
 * blended in; between relax and end the morph target is blended out. The max
 * intensity for the morph target can also be specified.
 * 
 * More than one MorphFU can be active at the same time. Parameter constraints:
 * none
 * 
 * @author Dennis Reidsma
 */
@Slf4j
public class MorphFU implements FaceUnit
{
    private AtomicDouble intensity = new AtomicDouble(1f);
    private AtomicDouble prevIntensity = new AtomicDouble(1f);

    private String targetName = "";
    private String targetWeights = "";

    public void setTargetName(String targetName)
    {
        this.targetName = targetName;
    }
    
    public void setTargetWeights(String targetWeights)
    {
        this.targetWeights = targetWeights;
    }

    @Override
    public void startUnit(double t)
    {

    }

    @Delegate
    private final KeyPositionManager keyPositionManager = new KeyPositionManagerImpl();

    private String[] morphTargets = new String[] { "" };
    private float[] morphWeights = new float[] { };

    private FaceController faceController;

    public void setMorphTargets(Set<String> targets)
    {
        morphTargets = targets.toArray(new String[targets.size()]);
        setTargetName(Joiner.on(",").join(morphTargets));
    }

    public MorphFU()
    {
        KeyPosition attackPeak = new KeyPosition("attackPeak", 0.1d, 1d);
        KeyPosition relax = new KeyPosition("relax", 0.9d, 1d);
        KeyPosition start = new KeyPosition("start", 0d, 1d);
        KeyPosition end = new KeyPosition("end", 1d, 1d);
        addKeyPosition(start);
        addKeyPosition(attackPeak);
        addKeyPosition(relax);
        addKeyPosition(end);
    }

    public void setFaceController(FaceController fc)
    {
        faceController = fc;
    }

    public void setIntensity(float intensity)
    {
        this.intensity.set(intensity);
    }

    @Override
    public void setFloatParameterValue(String name, float value) throws ParameterNotFoundException
    {
        if (name.equals("intensity"))
        {
            intensity.set(value);
            prevIntensity.set(value);
        }
        else throw new ParameterNotFoundException(name);
    }

    @Override
    public void setParameterValue(String name, String value) throws ParameterException
    {
        if (name.equals("targetname"))
        {
            targetName = value;
            updateMorphTargets();
        }
        else if (name.equals("targetweights"))
        {
            targetWeights = value;
            updateMorphTargets();
        }
        else
        {
            if (StringUtil.isNumeric(value))
            {
                setFloatParameterValue(name, Float.parseFloat(value));
            }
            else
            {
                throw new InvalidParameterException(name, value);
            }
        }
    }

    @Override
    public String getParameterValue(String name) throws ParameterException
    {
        if (name.equals("targetname")) return "" + targetName;
        if (name.equals("targetweights")) return "" + targetWeights;
        return "" + getFloatParameterValue(name);
    }

    @Override
    public float getFloatParameterValue(String name) throws ParameterException
    {
        if (name.equals("intensity")) return intensity.floatValue();
        throw new ParameterNotFoundException(name);
    }

    @Override
    public boolean hasValidParameters()
    {
        return true;
    }

    private void updateMorphTargets()
    {
        morphTargets = targetName.split(",");
        if (targetWeights.length() == 0) {
            // do not waste time trying to parse
            // for default-confed morphs, e.g. visemes
            morphWeights = new float[]{ };
        } else {
            String[] stringWeights = targetWeights.split(",");
            morphWeights = new float[morphTargets.length];
            try {
                for (int i = 0; i<morphTargets.length; ++i) {
                    morphWeights[i] = Float.parseFloat(stringWeights[i]);
                }
            } catch (Exception e) {
                //log.warn("Error parsing morph weight - defaulting all to 1.0");
                morphWeights = new float[]{ };
            }
        }
    }

    /**
     * Executes the face unit, by morphing the face. Linear interpolate from
     * intensity 0..max between start and ready; keep at max till relax; then
     * back to zero from relax till end.
     * 
     * @param t
     *            execution time, 0 &lt; t &lt; 1
     * @throws FUPlayException
     *             if the play fails for some reason
     */
    public void play(double t) throws FUPlayException
    {
        log.debug("Playing FU at time={}", t);
        // between where and where? Linear interpolate from intensity 0..max
        // between start&Ready; then down from relax till end

        double attackPeak = getKeyPosition("attackPeak").time;
        double relax = getKeyPosition("relax").time;
        float newMorphedWeight = 0;

        boolean applyIndividualWeights = true;
        if (morphWeights.length == 0) {
            // targetweights not specified - weight 1.0 for all morphs
            applyIndividualWeights = false;
        } else {
            if (morphWeights.length != morphTargets.length) {
                log.warn("targetweights does not specify one weight per target - weighing all with 1.0");
                applyIndividualWeights = false;
            }
        }

        if (t < attackPeak && t > 0)
        {
            newMorphedWeight = intensity.floatValue() * (float) (t / attackPeak);
        }
        else if (t >= attackPeak && t <= relax)
        {
            newMorphedWeight = intensity.floatValue();
        }
        else if (t > relax && t < 1)
        {
            newMorphedWeight = intensity.floatValue() * (float) (1 - ((t - relax) / (1 - relax)));
        }

        float[] newWeights = new float[morphTargets.length];
        for (int i = 0; i < newWeights.length; i++)
            //newWeights[i] = newMorphedWeight;
            newWeights[i] = newMorphedWeight * (applyIndividualWeights ? morphWeights[i] : 1.0f);
        faceController.addMorphTargets(morphTargets, newWeights);
        prevIntensity.set(newMorphedWeight);
    }

    /**
     * Creates the TimedFaceUnit corresponding to this face unit
     * @param bmlId
     *            BML block id
     * @param id
     *            behaviour id
     * 
     * @return the TFU
     */
    @Override
    public TimedFaceUnit createTFU(FeedbackManager bfm, BMLBlockPeg bbPeg, String bmlId, String id, PegBoard pb)
    {
        return new TimedFaceUnit(bfm, bbPeg, bmlId, id, this, pb);
    }

    /**
     * @return Prefered duration (in seconds) of this face unit, 0 means not
     *         determined/infinite
     */
    public double getPreferedDuration()
    {
        return 1d;
    }

    /**
     * Create a copy of this face unit and link it to the facecontroller
     */
    public FaceUnit copy(FaceController fc, FACSConverter fconv, EmotionConverter econv)
    {
        MorphFU result = new MorphFU();
        result.setFaceController(fc);
        result.intensity = intensity;
        result.targetName = targetName;

        for (KeyPosition keypos : getKeyPositions())
        {
            result.addKeyPosition(keypos.deepCopy());
        }
        result.updateMorphTargets();
        return result;
    }

    @Override
    public void interruptFromHere()
    {
        intensity.set(prevIntensity.get());        
    }
}
