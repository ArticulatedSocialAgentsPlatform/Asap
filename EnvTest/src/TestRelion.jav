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
import hmi.environmentbase.CopyEnvironment;
import hmi.environmentbase.Environment;
import hmi.headandgazeembodiments.SystemSchedulingClock;
import hmi.mixedanimationenvironment.MixedAnimationEnvironment;
import hmi.physicsenvironment.OdePhysicsEnvironment;
import hmi.util.DefaultDeadlockListener;
import hmi.util.EventDispatchThreadHangMonitor;
import hmi.util.SystemClock;
import hmi.util.ThreadDeadlockDetector;

import java.io.IOException;
import java.util.ArrayList;

import asap.environment.AsapEnvironment;
import asap.environment.AsapVirtualHuman;

/**
 * Elckerlyc demo.
 * 
 * @author Dennis Reidsma
 */
public class TestRelion
{

    static
    {
        EventDispatchThreadHangMonitor.initMonitoring();
        new ThreadDeadlockDetector().addListener(new DefaultDeadlockListener());
    }

    /**
     * Start the ElckerlycEnvironment prog
     */
    @SuppressWarnings("unused")
    public static void main(String[] arg)
    {
        System.setProperty("sun.java2d.noddraw", "true"); // avoid potential
                                                          // interference with
                                                          // (non-Jogl) Java
                                                          // using direct draw
        AsapEnvironment ae = new AsapEnvironment();
        AudioEnvironment aue = new AudioEnvironment("WAV_CLIP");
        ClockDrivenCopyEnvironment ce = new ClockDrivenCopyEnvironment(1000 / 20); // with freq=20, copy joints (i.e., send them to SUIT
        OdePhysicsEnvironment ope = new OdePhysicsEnvironment();
        MixedAnimationEnvironment mae = new MixedAnimationEnvironment();

        ope.init();
        aue.init();
        mae.init(ope);
        ce.init();
        ArrayList<Environment> environments = new ArrayList<Environment>();
        environments.add(aue);
        environments.add(ae);
        environments.add(ce);
        environments.add(ope);
        environments.add(mae);

        ae.init(environments, ope.getPhysicsSchedulingClock()); // the scheduling clock passed here drives the engines in ae. if no physics is used, then register ae as a
                                                                // listener at the render clock or even at your own SystemClock!

        ope.addPrePhysicsCopyListener(ae);
        ope.startPhysicsClock();

        try
        {
            AsapVirtualHuman avh = ae.loadVirtualHuman("relion", "Humanoids/relion", "relion_asaploader_nodebug.xml",
                    "Relion avatar - testing system integration");
        }
        catch (IOException ex)
        {
            System.out.println("Cannot load VH");
        }

    }

}
