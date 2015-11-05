package asap.faceengine.lipsync;

import hmi.faceanimation.FaceController;
import hmi.faceanimation.converters.EmotionConverter;
import hmi.faceanimation.converters.FACSConverter;
import hmi.util.StringUtil;

import java.util.List;
import java.util.Set;

import com.google.common.base.Joiner;

import asap.faceengine.faceunit.FaceUnit;
import asap.faceengine.faceunit.TimedFaceUnit;
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

/**
 * This class implements the dominance function.
 * When the method play() is called the morphedWeights for the visemes are calculated using dominance functions defined by multiple parameters.
 * 
 * @author mklemens
 */
public class DominanceFU implements FaceUnit
{
    private final KeyPositionManager keyPositionManager = new KeyPositionManagerImpl();
    private FaceController faceController;
    private String targetName = "";
    private String[] morphTargets = new String[] { "" };
    private int repeats = 1;
    private float intensity = 1;
    
    // If you do not want to see the debug-output with all values for the parameters set this to false
    private static Boolean DEBUGOUTPUTPARAMETERS = false;	//															<<<<< DISABLE DEBUG OUTPUT
    
    
    // THE PARAMETERS REQUIRED FOR MODIFYING THE DOMINANCE FUNCTION
    
    // This parameter defines the overall amplitude of the dominance function
    private double magnitude;
    
    // Increase this factor to squash the function
    private double stretchFactor_LeftFromPeak;
    private double stretchFactor_RightFromPeak;
    
    // Increase these parameters to make the function steeper
    private double rateParameter_LeftFromPeek;
    private double rateParameter_RightFromPeek;
    
    // This parameter lets you shift the peak of the dominance function
    private double functionPeak;
        
    // Use this function to set the parameters for the dominance function
    public void setFunctionParameters(double mag, double stretchFacLeft, double stretchFacRight, double rateParameterLeft, double rateParameterRight, double peak, int visemeNumber, int visemeType)
    {
    	this.magnitude = mag;
    	this.stretchFactor_LeftFromPeak = stretchFacLeft;
    	this.stretchFactor_RightFromPeak = stretchFacRight;
    	this.rateParameter_LeftFromPeek = rateParameterLeft;
    	this.rateParameter_RightFromPeek = rateParameterRight;
    	this.functionPeak = peak;
    	
    	if (DEBUGOUTPUTPARAMETERS)
			System.out.println("Changed parameters for viseme   "+visemeNumber+"   to:   "
					+ "Magnitude "+mag+"     "
					+ "StretchL "+stretchFacLeft+"     "
		    		+ "StretchR "+stretchFacRight+"     "
					+ "RateLeft "+rateParameterLeft+"     "
					+ "RateRight "+rateParameterRight+"     "
					+ "Peak "+peak+"     "
					+ "-   Visemetype:   "+visemeType);
    }

	private void updateMorphTargets()
    {
        morphTargets = targetName.split(",");
    }

    public void setTargets(Set<String> targets)
    {
        targetName = Joiner.on(",").join(targets);
        updateMorphTargets();
    }
    
    @Override
    public void play(double t) throws MUPlayException
    {
    	float newMorphedWeight = 0;

    	if (t<=functionPeak)
    		newMorphedWeight = (float)(magnitude*Math.exp(rateParameter_LeftFromPeek *
    							(-50 * stretchFactor_LeftFromPeak * (t-functionPeak))));
    	else
    		newMorphedWeight = (float)(magnitude*Math.exp(rateParameter_RightFromPeek *
    							(-50 * stretchFactor_RightFromPeak * (t-functionPeak))));
    	
        float[] newWeights = new float[morphTargets.length];
        for (int i = 0; i < newWeights.length; i++)
            newWeights[i] = newMorphedWeight;
        faceController.addMorphTargets(morphTargets, newWeights);
    }

    @Override
    public void startUnit(double t) throws MUPlayException
    {

    }

    @Override
    public double getPreferedDuration()
    {
        return 1;
    }

    @Override
    public void setFloatParameterValue(String name, float value) throws ParameterException
    {
        if(name.equals("intensity"))
        {
            intensity = value;            
        }
        else if(name.equals("repeats"))
        {
            repeats = (int)value;
        }
        else
        {
            throw new ParameterNotFoundException(name);
        }
    }

    @Override
    public void setParameterValue(String name, String value) throws ParameterException
    {
        if (name.equals("targetname"))
        {
            targetName = value;
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
        return "" + getFloatParameterValue(name);
    }

    @Override
    public float getFloatParameterValue(String name) throws ParameterException
    {
        if(name.equals("intensity"))
        {
            return intensity;
        }
        else if(name.equals("repeats"))
        {
            return repeats;
        }
        throw new ParameterNotFoundException(name);        
    }

    @Override
    public void addKeyPosition(KeyPosition kp)
    {
        keyPositionManager.addKeyPosition(kp);
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

    @Override
    public KeyPosition getKeyPosition(String id)
    {
        return keyPositionManager.getKeyPosition(id);
    }

    @Override
    public void removeKeyPosition(String id)
    {
        keyPositionManager.removeKeyPosition(id);
    }

    @Override
    public boolean hasValidParameters()
    {
        return true;
    }

    @Override
    public TimedFaceUnit createTFU(FeedbackManager bfm, BMLBlockPeg bbPeg, String bmlId, String id, PegBoard pb)
    {
        return new TimedFaceUnit(bfm, bbPeg, bmlId, id, this, pb);
    }

    public void setFaceController(FaceController fc)
    {
        faceController = fc;
    }

    @Override
    public DominanceFU copy(FaceController fc, FACSConverter fconv, EmotionConverter econv)
    {
        DominanceFU fu = new DominanceFU();
        fu.setFaceController(fc);
        fu.targetName = targetName;
        fu.intensity = intensity;
        fu.repeats = repeats;
        for (KeyPosition keypos : getKeyPositions())
        {
            fu.addKeyPosition(keypos.deepCopy());
        }
        fu.updateMorphTargets();
        return fu;
    }

    @Override
    public void interruptFromHere()
    {
        // TODO Implement this
    }
}
