/*******************************************************************************
 *******************************************************************************/
package asap.srnao.planunit;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.InvalidParameterException;
import asap.realizer.planunit.KeyPosition;
import asap.realizer.planunit.KeyPositionManager;
import asap.realizer.planunit.KeyPositionManagerImpl;
import asap.realizer.planunit.ParameterException;
import asap.srnao.PicturePlanner;
import asap.srnao.display.PictureDisplay;

import com.google.common.primitives.Floats;

public class AddAnimationDirPU implements NaoUnit
{

    private final KeyPositionManager keyPositionManager = new KeyPositionManagerImpl();
    @SuppressWarnings("unused")
    private static Logger logger = LoggerFactory.getLogger(PicturePlanner.class.getName());
    private String resourcePath;
    private String directoryName;
    private float layer;
    private AnimationDirLoader animationLoader = null;
    private int nrImages = 0;
    private int currentImage = 0;
    private PictureDisplay display;
    // the unique id of this PU as specified in the BML
    private String puId;

    public AddAnimationDirPU()
    {
        KeyPosition start = new KeyPosition("start", 0d, 1d);
        KeyPosition end = new KeyPosition("end", 1d, 1d);
        addKeyPosition(start);
        addKeyPosition(end);
    }

    public void setDisplay(PictureDisplay display)
    {
        this.display = display;
    }

    public void prepareImages()
    {
        animationLoader = new AnimationDirLoader(resourcePath, directoryName, display);
        nrImages = animationLoader.getNumberOfImages();
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

        if (name.equals("resourcePath"))
        {
            resourcePath = value;
        }
        else if (name.equals("directoryName"))
        {
            directoryName = value;
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
        if (name.equals("resourcePath"))
        {
            return resourcePath.toString();
        }
        if (name.equals("directoryName"))
        {
            return directoryName.toString();
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
        // TODO: perform some checks on valid path + filename
        return true;
    }

    /**
     * start the unit.
     */
    public void startUnit(double time) throws NUPlayException
    {
        display.addImage(puId, animationLoader.getImageId(0), layer);
        currentImage = 0;
    }

    /**
     * 
     * @param t execution time, 0 &lt t &lt 1
     * @throws NUPlayException if the play fails for some reason
     */
    public void play(double t) throws NUPlayException
    {
        if (t > (1d / nrImages) * (currentImage + 1))
        {
            currentImage++;
            display.replaceImage(puId, animationLoader.getImageId(currentImage), layer);
        }
    }

    public void cleanup()
    {
        // remove the current image from the layer
        display.removeImage(puId, layer);

    }

    /**
     * Creates the TimedPictureUnit corresponding to this face unit
     * 
     * @param bmlId BML block id
     * @param id behaviour id
     * 
     * @return the TPU
     */
    @Override
    public TimedNaoUnit createTNU(FeedbackManager bfm, BMLBlockPeg bbPeg, String bmlId, String id)
    {
        this.puId = id;
        return new TimedNaoUnit(bfm, bbPeg, bmlId, id, this);
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
     * Create a copy of this picture unit and link it to the display
     */
    public NaoUnit copy(PictureDisplay display)
    {
        AddAnimationDirPU result = new AddAnimationDirPU();
        result.resourcePath = resourcePath;
        result.directoryName = directoryName;
        result.layer = layer;
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
