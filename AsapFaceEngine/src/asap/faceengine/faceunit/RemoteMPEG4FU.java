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
package asap.faceengine.faceunit;

import hmi.faceanimation.FaceController;
import hmi.faceanimation.converters.EmotionConverter;
import hmi.faceanimation.converters.FACSConverter;
import hmi.faceanimation.model.MPEG4Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import lombok.Delegate;

import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.KeyPosition;
import asap.realizer.planunit.KeyPositionManager;
import asap.realizer.planunit.KeyPositionManagerImpl;
import asap.realizer.planunit.ParameterException;
import asap.realizer.planunit.ParameterNotFoundException;
import asap.utils.AnimationSync;

/**
 * A faction animation unit that retrieves its FACS configuration remotely.
 * It will open a socket to the localhost on port 9123, and will send the
 * FACS it receives there directly to the face.
 * 
 * @author Mark ter Maat & Dennis Reidsma
 */
public class RemoteMPEG4FU extends Thread implements FaceUnit
{

    @Delegate private final KeyPositionManager keyPositionManager = new KeyPositionManagerImpl();

    private FaceController faceController;

    private final MPEG4Configuration mpeg4Config = new MPEG4Configuration();

    private Integer[] faceValues;
    private BufferedReader in;

    public RemoteMPEG4FU()
    {

    }
    @Override
    public void startUnit(double t)
    {
        
    }
    public void connectToServer()
    {
        try
        {
            Socket socket = new Socket("localhost", 9123);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
        catch (UnknownHostException e)
        {
            System.out.println("Unknown host");
            in = null;
        }
        catch (IOException e)
        {
            System.out.println("Could not connect to localhost");
            in = null;
        }
        if (in != null)
        {
            start();
        }
    }

    public void run()
    {
        while (true)
        {
            if (in != null)
            {
                String line = null;
                try
                {
                    line = in.readLine();
                }
                catch (IOException e)
                {
                    in = null;
                    e.printStackTrace();
                }
                if (line != null)
                {
                    String[] recValues = line.split(" ");
                    if (recValues.length == 68)
                    {
                        synchronized (this)
                        {
                            faceValues = new Integer[68];
                            for (int i = 0; i < faceValues.length; i++)
                            {
                                faceValues[i] = Integer.valueOf(recValues[i]);
                            }
                        }
                    }
                }
            }
        }
    }

    public void setFaceController(FaceController fc)
    {
        faceController = fc;
    }

    @Override
    public void setFloatParameterValue(String name, float value) throws ParameterException
    {

    }

    @Override
    public void setParameterValue(String name, String value) throws ParameterException
    {
    }

    @Override
    public String getParameterValue(String name) throws ParameterException
    {
        return "" + getFloatParameterValue(name);
    }

    @Override
    public float getFloatParameterValue(String name) throws ParameterException
    {
        throw new ParameterNotFoundException(name);
    }

    @Override
    public boolean hasValidParameters()
    {
        return true;
    }

    /**
     * Executes the face unit, by applying the face configuration. Linear
     * interpolate from intensity 0..max between start and attackPeak; keep at max
     * till relax; then back to zero from relax till end.
     * 
     * @param t
     *            execution time, 0 &lt t &lt 1
     * @throws FUPlayException
     *             if the play fails for some reason
     */
    public void play(double t) throws FUPlayException
    {
        if (in == null)
        {
            connectToServer();
        }
        if (faceValues == null)
        {
            return;
        }

        Integer[] faceValuesCopy = null;
        synchronized (this)
        {
            faceValuesCopy = faceValues.clone();
        }

        synchronized (AnimationSync.getSync())
        {
            if (faceValuesCopy != null)
            {
                faceController.removeMPEG4Configuration(mpeg4Config);
                try
                {
                    mpeg4Config.setValues(faceValuesCopy);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                faceController.addMPEG4Configuration(mpeg4Config);
            }
        }
    }

    public void cleanup()
    {
        if (mpeg4Config != null) faceController.removeMPEG4Configuration(mpeg4Config);
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
    public TimedFaceUnit createTFU(FeedbackManager bfm, BMLBlockPeg bbPeg, String bmlId, String id)
    {
        return new TimedFaceUnit(bfm, bbPeg, bmlId, id, this);
    }

    @Override
    public String getReplacementGroup()
    {
        return null;
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
     * Create a copy of this face unit and link it to the facecopntroller
     */
    public FaceUnit copy(FaceController fc, FACSConverter fconv, EmotionConverter econv)
    {
        RemoteMPEG4FU result = new RemoteMPEG4FU();
        result.setFaceController(fc);
        for (KeyPosition keypos : getKeyPositions())
        {
            result.addKeyPosition(keypos.deepCopy());
        }
        return result;
    }
}
