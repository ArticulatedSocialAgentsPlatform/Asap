/*******************************************************************************
 *******************************************************************************/
package asap.picture.planunit;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asap.picture.PicturePlanner;
import asap.picture.display.PictureDisplay;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.InvalidParameterException;
import asap.realizer.planunit.KeyPosition;
import asap.realizer.planunit.KeyPositionManager;
import asap.realizer.planunit.KeyPositionManagerImpl;
import asap.realizer.planunit.ParameterException;

public class SetImagePU implements PictureUnit
{
    private final KeyPositionManager keyPositionManager = new KeyPositionManagerImpl();

    @SuppressWarnings("unused")
    private static Logger logger = LoggerFactory.getLogger(PicturePlanner.class.getName());

    private String filePath = "";
    private String fileName = "";

    private String imageId = "";

    private PictureDisplay display;

    private String puId;

    public SetImagePU()
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
        display.preloadImage(imageId, filePath, fileName);
    }

    @Override
    public void setFloatParameterValue(String name, float value) throws ParameterException
    {
    }

    @Override
    public void setParameterValue(String name, String value) throws ParameterException
    {
        if (name.equals("filePath"))
        {
            filePath = value;
            if (!filePath.endsWith("/")) filePath += "/";
            imageId = filePath + fileName;
        }
        else if (name.equals("fileName"))
        {
            fileName = value;
            imageId = filePath + fileName;
        }
        else if (name.equals("imageId"))
        {
            imageId = value;
        }
        else
        {
            throw new InvalidParameterException(name, value);
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
        if (name.equals("imageId"))
        {
            return imageId.toString();
        }
        return "" + getFloatParameterValue(name);
    }

    @Override
    public float getFloatParameterValue(String name) throws ParameterException
    {
        return 0;
    }

    @Override
    public boolean hasValidParameters()
    {
        // TODO: perform some checks on valid path + filename
        return true;
    }

    /** start the unit. */
    public void startUnit(double time) throws PUPlayException
    {
        display.setImage(puId, imageId, 1);
    }

    /**
     * 
     * @param t
     *            execution time, 0 &lt t &lt 1
     * @throws PUPlayException
     *             if the play fails for some reason
     */
    public void play(double t) throws PUPlayException
    {
    }

    public void cleanup()
    {
        display.removeImage(puId, 1);
    }

    /**
     * Creates the TimedPictureUnit corresponding to this face unit
     * 
     * @param bmlId
     *            BML block id
     * @param id
     *            behaviour id
     * 
     * @return the TPU
     */
    @Override
    public TimedPictureUnit createTPU(FeedbackManager bfm, BMLBlockPeg bbPeg, String bmlId, String id)
    {
        puId = id;
        return new TimedPictureUnit(bfm, bbPeg, bmlId, id, this);
    }

    /**
     * @return Prefered duration (in seconds) of this face unit, 0 means not determined/infinite
     */
    public double getPreferedDuration()
    {
        return 1d;
    }

    /**
     * Create a copy of this picture unit and link it to the display
     */
    public PictureUnit copy(PictureDisplay display)
    {
        SetImagePU result = new SetImagePU();
        result.filePath = filePath;
        result.fileName = fileName;
        result.imageId = imageId;
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
