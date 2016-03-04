/*******************************************************************************
 *******************************************************************************/
package asap.faceengine.faceunit;

import hmi.faceanimation.FaceController;
import hmi.faceanimation.converters.EmotionConverter;
import hmi.faceanimation.converters.FACSConverter;
import hmi.faceanimation.model.MPEG4Configuration;
import hmi.util.StringUtil;
import lombok.Delegate;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.InvalidParameterException;
import asap.realizer.planunit.KeyPosition;
import asap.realizer.planunit.KeyPositionManager;
import asap.realizer.planunit.KeyPositionManagerImpl;
import asap.realizer.planunit.ParameterException;
import asap.realizer.planunit.ParameterNotFoundException;

import com.google.common.util.concurrent.AtomicDouble;

/**
 * A basic facial animation unit consisting of one position on Plutchiks emotion
 * disc (see hmi.faceanimation.converters.EmotionConverter The key positions
 * are: start, attackPeak, relax, end. This describes an apex-like intensity
 * development: Between start and attackPeak, the face configuration is blended in;
 * between relax and end the face configuration is blended out.
 * 
 * Parameter constraint: 0 &lt;= angle &lt;= 360
 * 
 * @author Dennis Reidsma
 */
public class PlutchikFU implements FaceUnit
{
    @Delegate
    private final KeyPositionManager keyPositionManager = new KeyPositionManagerImpl();

    private AtomicDouble intensity = new AtomicDouble(1f);
    private AtomicDouble prevIntensity = new AtomicDouble(1f);
    
    private float angle = -1f;

    private float activation = -1f;

    private FaceController faceController;
    private EmotionConverter emotionConverter;

    @Override
    public void startUnit(double t)
    {

    }

    public PlutchikFU()
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

    public void setEmotionConverter(EmotionConverter ec)
    {
        emotionConverter = ec;
    }

    @Override
    public void setFloatParameterValue(String name, float value) throws ParameterException
    {
        if (name.equals("intensity"))
        {
            intensity.set(value);
            prevIntensity.set(value);
        }
        else if (name.equals("angle")) angle = value;
        else if (name.equals("activation")) activation = value;
        else throw new ParameterNotFoundException(name);
    }

    @Override
    public void setParameterValue(String name, String value) throws ParameterException
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

    @Override
    public String getParameterValue(String name)
    {
        if (name.equals("intensity")) return "" + intensity;
        else if (name.equals("angle")) return "" + angle;
        else if (name.equals("activation")) return "" + activation;
        return null;
    }

    @Override
    public float getFloatParameterValue(String name) throws ParameterException
    {
        if (name.equals("intensity")) return intensity.floatValue();
        if (name.equals("angle")) return angle;
        if (name.equals("activation")) return activation;
        throw new ParameterNotFoundException(name);
    }

    @Override
    public boolean hasValidParameters()
    {
        return (angle >= 0) && (angle <= 360);
    }

    /**
     * Executes the face unit, by applying the face configuration. Linear
     * interpolate from intensity 0..max between start and attackPeak; keep at max
     * till relax; then back to zero from relax till end.
     * 
     * @param t
     *            execution time, 0 &lt; t &lt; 1
     * @throws FUPlayException
     *             if the play fails for some reason
     */
    public void play(double t) throws FUPlayException
    {
        // between where and where? Linear interpolate from intensity 0..max
        // between start&attackPeak; then down from relax till end
        double attackPeak = getKeyPosition("attackPeak").time;
        double relax = getKeyPosition("relax").time;
        float newAppliedWeight = 0;

        if (t < attackPeak && t > 0)
        {
            newAppliedWeight = intensity.floatValue() * (float) (t / attackPeak);
        }
        else if (t >= attackPeak && t <= relax)
        {
            newAppliedWeight = intensity.floatValue();
        }
        else if (t > relax && t < 1)
        {
            newAppliedWeight = intensity.floatValue() * (float) (1 - ((t - relax) / (1 - relax)));
        }
        MPEG4Configuration config = emotionConverter.convert(angle, activation * newAppliedWeight);
        faceController.addMPEG4Configuration(config);
        prevIntensity.set(newAppliedWeight);
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
        PlutchikFU result = new PlutchikFU();
        result.setFaceController(fc);
        result.setEmotionConverter(econv);
        result.intensity = intensity;
        result.angle = angle;
        result.activation = activation;
        for (KeyPosition keypos : getKeyPositions())
        {
            result.addKeyPosition(keypos.deepCopy());
        }
        return result;
    }

    @Override
    public void interruptFromHere()
    {
        intensity.set(prevIntensity.get());        
    }
}
