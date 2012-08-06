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

import hmi.audioenvironment.AudioEnvironment;
import hmi.environmentbase.ClockDrivenCopyEnvironment;
import hmi.environmentbase.Environment;
import hmi.util.SystemClock;

import java.io.IOException;
import java.util.ArrayList;

import asap.bml.ext.bmlt.BMLTInfo;
import asap.environment.AsapEnvironment;
import asap.environment.AsapVirtualHuman;

/**
 * Elckerlyc demo.
 * 
 * @author Dennis Reidsma
 */
public class TestNoOpenGL
{
	/*/
    static
    {
        EventDispatchThreadHangMonitor.initMonitoring();
        new ThreadDeadlockDetector().addListener(new DefaultDeadlockListener());
    }
    /**/

    @SuppressWarnings("unused")
    public static void main(String[] arg)
    {
        System.setProperty("sun.java2d.noddraw", "true"); // avoid potential
                                                          // interference with
                                                          // (non-Jogl) Java
                                                          // using direct draw
        AsapEnvironment ae = new AsapEnvironment();
        AudioEnvironment aue = new AudioEnvironment("WAV_CLIP");
        ClockDrivenCopyEnvironment ce = new ClockDrivenCopyEnvironment(1000 / 20);

        aue.init();
        ce.init();
        ArrayList<Environment> environments = new ArrayList<Environment>();
        environments.add(aue);
        environments.add(ae);
        environments.add(ce);

        // if no physics, use the following:
        /**/
        SystemClock clock = new SystemClock(1000 / 50, "clock");
        clock.start();
        ae.init(environments, clock);
        clock.addClockListener(ae);
        /**/


        BMLTInfo.init();
        
        try
        {
            AsapVirtualHuman avh = ae.loadVirtualHuman("jordi", "pictureengine/smarcos/", "smarcos_pc_asaploader.xml", "Smarcos agent");
            //AsapVirtualHuman avh = ae.loadVirtualHuman("daniel", "pictureengine/example/", "example_asaploader.xml","Pictureembodiment");
            //DOESN'T WORK YET: AsapVirtualHuman avh2 = ae.loadVirtualHuman("nabaztag", "Humanoids/nabaztag", "newnabaztagloader.xml", "Nabaztag");
        }
        catch (IOException ex)
        {
            System.out.println("Cannot load VH");
        }

    }

}
