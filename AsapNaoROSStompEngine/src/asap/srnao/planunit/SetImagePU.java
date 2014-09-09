/*******************************************************************************
 * Copyright (C) 2009 Human Media Interaction, University of Twente, the Netherlands
 * 
 * This file is part of the Elckerlyc BML realizer.
 * 
 * Elckerlyc is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Elckerlyc is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Elckerlyc.  If not, see http://www.gnu.org/licenses/.
 ******************************************************************************/
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

public class SetImagePU implements NaoUnit
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
    public void startUnit(double time) throws NUPlayException
    {
        display.setImage(puId, imageId, 1);
    }

    /**
     * 
     * @param t
     *            execution time, 0 &lt t &lt 1
     * @throws NUPlayException
     *             if the play fails for some reason
     */
    public void play(double t) throws NUPlayException
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
    public TimedNaoUnit createTNU(FeedbackManager bfm, BMLBlockPeg bbPeg, String bmlId, String id)
    {
        puId = id;
        return new TimedNaoUnit(bfm, bbPeg, bmlId, id, this);
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
    public NaoUnit copy(PictureDisplay display)
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
