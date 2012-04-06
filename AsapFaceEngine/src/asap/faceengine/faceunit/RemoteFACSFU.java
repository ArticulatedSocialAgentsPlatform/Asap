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

import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.planunit.KeyPosition;
import hmi.elckerlyc.planunit.KeyPositionManager;
import hmi.elckerlyc.planunit.KeyPositionManagerImpl;
import hmi.elckerlyc.planunit.ParameterException;
import hmi.elckerlyc.planunit.ParameterNotFoundException;
import hmi.faceanimation.FaceController;
import hmi.faceanimation.converters.EmotionConverter;
import hmi.faceanimation.converters.FACSConverter;
import hmi.faceanimation.model.FACSConfiguration;
import hmi.faceanimation.model.MPEG4Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

import lombok.Delegate;

import asap.utils.AnimationSync;

/**
 * A faction animation unit that retrieves its FACS configuration remotely.
 * It will open a socket to the localhost on port 9123, and will send the
 * FACS it receives there directly to the face.
 * 
 * @author Mark ter Maat & Dennis Reidsma
 */
public class RemoteFACSFU extends Thread implements FaceUnit
{

    private final static String HOST = "130.89.228.90";
    private final static int PORT = 9123;

    @Delegate private final KeyPositionManager keyPositionManager = new KeyPositionManagerImpl();

    private float intensity = 1f;

    private FACSConfiguration facsConfig = new FACSConfiguration();

    private FaceController faceController;
    private FACSConverter facsConverter;

    private final MPEG4Configuration mpeg4Config = new MPEG4Configuration();

    private Float[] faceValues;
    private BufferedReader in;

    public RemoteFACSFU()
    {

    }

    public void connectToServer()
    {
        try
        {
            Socket socket = new Socket(HOST, PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
        catch (UnknownHostException e)
        {
            System.out.println("Unknown host " + HOST);
            in = null;
        }
        catch (IOException e)
        {
            System.out.println("Could not connect to " + HOST);
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
                    e.printStackTrace();
                    in = null;
                }
                if (line != null)
                {
                    String[] recValues = line.split(" ");
                    if (recValues.length == 84)
                    {
                        synchronized (this)
                        {
                            faceValues = new Float[84];
                            for (int i = 0; i < 84; i++)
                            {
                                faceValues[i] = Float.valueOf(recValues[i]);
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

    public void setFACSConverter(FACSConverter fc)
    {
        facsConverter = fc;
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
        return facsConfig != null;
    }

    public void setConfig(FACSConfiguration fc)
    {
        facsConfig = fc;
    }

    /**
     * Executes the face unit, by applying the face configuration. Linear
     * interpolate from intensity 0..max between start and ready; keep at max
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
        synchronized (this)
        {
            Float[] faceValuesCopy = faceValues.clone();
            try
            {
                facsConfig.setValues(faceValuesCopy);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        synchronized (AnimationSync.getSync())
        {

            faceController.removeMPEG4Configuration(mpeg4Config);
            facsConverter.convert(facsConfig, mpeg4Config);
            faceController.addMPEG4Configuration(mpeg4Config);
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
        RemoteFACSFU result = new RemoteFACSFU();
        result.setFaceController(fc);
        result.setFACSConverter(fconv);
        result.intensity = intensity;
        result.setConfig(facsConfig);
        for (KeyPosition keypos : getKeyPositions())
        {
            result.addKeyPosition(keypos.deepCopy());
        }
        return result;
    }
}
