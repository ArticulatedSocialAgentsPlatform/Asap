/*******************************************************************************
 *******************************************************************************/
package asap.picture.planunit;

import hmi.util.Resources;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asap.picture.display.PictureDisplay;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.InvalidParameterException;
import asap.realizer.planunit.KeyPosition;
import asap.realizer.planunit.KeyPositionManager;
import asap.realizer.planunit.KeyPositionManagerImpl;
import asap.realizer.planunit.ParameterException;

import com.google.common.primitives.Floats;

/**
 * PictureUnit that plays an animation defined by an XML animation file.
 * 
 * @author Jordi Hendrix
 */
public class AddAnimationXMLPU implements PictureUnit
{

    private final KeyPositionManager keyPositionManager = new KeyPositionManagerImpl();
    private static Logger logger = LoggerFactory.getLogger(AddAnimationXMLPU.class.getName());
    private String filePath;
    private String fileName;
    private float layer;
    private AnimationXMLLoader animationLoader = null;
    private int currentImage = 0;
    private double currentEndtime = 0;
    private double totalDuration = 0;
    private PictureDisplay display;
    // the unique id of this PU as specified in the BML
    private String puId;

    public AddAnimationXMLPU()
    {
    }

    public void setDisplay(PictureDisplay display)
    {
        this.display = display;
    }

    @Override
    public void prepareImages() throws PUPrepareException
    {
        // Do not prepare images if animationLoader is already set (i.e. images are already loaded)
        if (animationLoader == null)
        {
            animationLoader = new AnimationXMLLoader(filePath, fileName, display);
            totalDuration = animationLoader.getTotalDuration();
            setKeyPositions(animationLoader.getKeyPositions());
        }
    }

    @Override
    public void setFloatParameterValue(String name, float value) throws ParameterException
    {
        if (name.equals("layer"))
        {
            layer = value;
        }
    }

    @Override
    public void setParameterValue(String name, String value) throws ParameterException
    {
        if (name.equals("filePath"))
        {
            filePath = value;
        }
        else if (name.equals("fileName"))
        {
            fileName = value;
        }
        else
        {
            Float f = Floats.tryParse(value);
            if (f!=null)
            {
                setFloatParameterValue(name, f);
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
        if (name.equals("filePath"))
        {
            return filePath.toString();
        }
        if (name.equals("fileName"))
        {
            return fileName.toString();
        }
        return "" + getFloatParameterValue(name);
    }

    @Override
    public float getFloatParameterValue(String name) throws ParameterException
    {
        if (name.equals("layer"))
        {
            return layer;
        }
        else
        {
            return 0;
        }
    }

    @Override
    public boolean hasValidParameters()
    {
        // Check existence of xml file
        Resources r = new Resources(filePath);
        return r.getInputStream(fileName) != null;
    }

    /**
     * Start the unit.
     */
    @Override
    public void startUnit(double time) throws PUPlayException
    {
        String imageId = animationLoader.getImageId(0);
        if (imageId == null)
        {
            throw new PUPlayException("Requested image id has not been preloaded.", this);
        }
        display.addImage(puId, animationLoader.getImageId(0), layer);
        currentImage = 0;
        currentEndtime = animationLoader.getImageEndtime(currentImage);
    }

    /**
     * Refresh the currently displayed image.
     * 
     * @param t execution time, 0 &lt t &lt 1
     * @throws PUPlayException if the play fails for some reason
     */
    @Override
    public void play(double t) throws PUPlayException
    {
        boolean refreshNeeded = false;
        logger.debug("PLAYING AT: {}", t);
        // Skip images if needed (in case of slow screen framerate)
        while (t > (currentEndtime / totalDuration))
        {
            refreshNeeded = true;
            currentImage++;
            currentEndtime = animationLoader.getImageEndtime(currentImage);
        }

        // Refresh image if required
        if (refreshNeeded)
        {
            String imageId = animationLoader.getImageId(currentImage);
            if (imageId == null)
            {
                throw new PUPlayException("Requested image id has not been preloaded.", this);
            }
            display.replaceImage(puId, imageId, layer);
        }
    }

    @Override
    public void cleanup()
    {
        // Remove the current image from the layer
        display.removeImage(puId, layer);
    }

    /**
     * Creates the TimedPictureUnit corresponding to this picture unit.
     * 
     * @param bmlId BML block id
     * @param id Behaviour id
     * 
     * @return The created TPU
     */
    @Override
    public TimedPictureUnit createTPU(FeedbackManager bfm, BMLBlockPeg bbPeg, String bmlId, String id)
    {
        this.puId = id;
        return new TimedPictureUnit(bfm, bbPeg, bmlId, id, this);
    }

    /**
     * @return Prefered duration (in seconds) of this face unit, 0 means not
     *         determined/infinite
     */
    @Override
    public double getPreferedDuration()
    {
        return totalDuration;
    }

    /**
     * Create a copy of this picture unit and link it to the display.
     */
    @Override
    public PictureUnit copy(PictureDisplay display)
    {
        AddAnimationXMLPU result = new AddAnimationXMLPU();
        result.filePath = filePath;
        result.fileName = fileName;
        result.layer = layer;
        result.animationLoader = animationLoader;
        result.totalDuration = totalDuration;
        result.setDisplay(display);
        for (KeyPosition keypos : getKeyPositions())
        {
            result.addKeyPosition(keypos.deepCopy());
        }
        return result;
    }

    @Override
    public void addKeyPosition(KeyPosition kp)
    {
        keyPositionManager.addKeyPosition(kp);
    }

    @Override
    public KeyPosition getKeyPosition(String name)
    {
        return keyPositionManager.getKeyPosition(name);
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
    public void removeKeyPosition(String id)
    {
        keyPositionManager.removeKeyPosition(id);
    }
}
