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

import hmi.animation.VJoint;
import hmi.audioenvironment.AudioEnvironment;
import hmi.environmentbase.Environment;
import hmi.mixedanimationenvironment.MixedAnimationEnvironment;
import hmi.physicsenvironment.OdePhysicsEnvironment;
import hmi.renderenvironment.HmiRenderEnvironment;
import hmi.renderenvironment.HmiRenderEnvironment.RenderStyle;
import hmi.worldobjectenvironment.WorldObject;
import hmi.worldobjectenvironment.WorldObjectEnvironment;

import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;

import lombok.extern.slf4j.Slf4j;
import asap.bml.ext.bmlt.BMLTInfo;
import asap.environment.AsapEnvironment;
import asap.environment.AsapVirtualHuman;

/**
 * Elckerlyc demo.
 * 
 * @author Dennis Reidsma
 * @author Herwin van Welbergen
 */
@Slf4j
public class Test extends WindowAdapter
{
    protected AsapEnvironment ae = null;
    protected JFrame mainUI = null;

    /*/
    static
    {
        EventDispatchThreadHangMonitor.initMonitoring();
        new ThreadDeadlockDetector().addListener(new DefaultDeadlockListener());
    }
    /**/

    /**
     * Start the ElckerlycEnvironment prog
     */
    public static void main(String[] arg)
    {
        new Test().go();
    }

    @SuppressWarnings("unused")
    public void go()
    {
        System.setProperty("sun.java2d.noddraw", "true"); // avoid potential interference with (non-Jogl) Java using direct draw

        // create the main UI window
        mainUI = new JFrame("Test new HmiEnvironment");
        mainUI.setSize(1000, 600);
        mainUI.addWindowListener(this);


        BMLTInfo.init();
        
        HmiRenderEnvironment hre = new HmiRenderEnvironment();
        WorldObjectEnvironment we = new WorldObjectEnvironment();
        OdePhysicsEnvironment ope = new OdePhysicsEnvironment();
        MixedAnimationEnvironment mae = new MixedAnimationEnvironment();
        ae = new AsapEnvironment();
        AudioEnvironment aue = new AudioEnvironment(); // AudioEnvironment aue = new AudioEnvironment("WAV_CLIP");

        ArrayList<Environment> environments = new ArrayList<Environment>();
        environments.add(hre);
        environments.add(we);
        environments.add(ope);
        environments.add(mae);
        environments.add(aue);
        environments.add(ae);

        hre.init(); // canvas does not exist until init was called
        we.init();
        ope.init();
        aue.init();
        mae.init(ope);

        ae.init(environments, ope.getPhysicsClock()); // the scheduling clock passed here drives the engines in ae. if no physics is used, then register ae as a
                                                                // listener at the render clock or even at your own SystemClock!

        ope.addPrePhysicsCopyListener(ae);

        hre.getCameraTarget().setTranslation(0,0,0);
        
        // after initialisation: start the clocks -- animation will start now
        hre.startRenderClock();
        ope.startPhysicsClock();

        java.awt.Component canvas = hre.getAWTComponent(); // after init, get canvas and add to window
        mainUI.add(canvas);
        mainUI.setVisible(true);

        // add worldobject "camera" that we can use as target for looking at user
        VJoint camera = hre.getCameraTarget();
        we.getWorldObjectManager().addWorldObject("camera", new WorldObject(camera));

        // load a virtual human
        try
        {
        	//AsapVirtualHuman avh = ae.loadVirtualHuman("blueguy", "Humanoids/blueguy", "blueguy_asaploader_mary_hudson.xml", "blueguy - test avatar");
        	//AsapVirtualHuman avh = ae.loadVirtualHuman("billie", "Humanoids/billie", "asaploader_billie.xml", "billie");
        	//AsapVirtualHuman avh2 = ae.loadVirtualHuman("armandia1", "Humanoids/armandia", "asapvhloader_test.xml", "armandia - testing new environment setup");
        	//AsapVirtualHuman avh = ae.loadVirtualHuman("relion", "Humanoids/relion", "relion_asaploader.xml", "Relion avatar - testing system integration");
        	AsapVirtualHuman avh = ae.loadVirtualHuman("armandiahudson", "Humanoids/armandia", "armandia_asaploader_mary_hudson.xml", "armandia for hudson");
        }
        catch (IOException ex)
        {
            System.out.println("Cannot load VH");
        }

        // load the checkerboard ground plane of Herwin into the hmirenderenvironment
        hre.loadCheckerBoardGround("groundplane", 0f, 0.5f);

        // set background color of hmirenderenvironment
        hre.setBackground(0.3f, 0, 0);

        // === Some examples on loading various types of objects into your world

        /*
         * /
         * //load collada object; get its VJoint; unload it again.
         * hre.loadObject("cokecan", "Shared3DModels/cokecan", "Shared3DModels/cokecan", "cokecan_scale0.01.dae");
         * VJoint cokecanjoint = hre.getObjectRootJoint("cokecan");
         * hre.unloadObject("cokecan");
         * /*
         */
        // another example to look at: the room made by Guillermo.
        // hre.loadObject("psychoroom", "Shared3DModels/psychoroom", "Shared3DModels/psychoroom", "psychological_room.dae");

        /*
         * /
         * //Load a humanoid from file, without all the engines and such. This allows you to do completely your own animation;
         * //for this, you need no animation, physics, or Asap environment.
         * hre.loadHumanoid("armandia", "Humanoids/armandia/bin", "Humanoids/armandia/maps", null, "armandia_boring_neckfix_light_toplevel.bin", "ARMANDIA", new
         * HashMap<String, Float>());
         * VJoint armandiaRoot = hre.getHumanoidRootJoint("armandia");
         * armandiaRoot.setTranslation(1,1,1);
         * /*
         */

        /**/
        // a series of objects to show how to work with basic objects
        hre.loadSphere("greensphere", 0.05f, 25, 25, RenderStyle.FILL, new float[] { 0.2f, 1, 0.2f, 1 }, new float[] { 0.2f, 1, 0.2f, 1 },
                new float[] { 0.2f, 1, 0.2f, 0 }, new float[] { 0.2f, 1, 0.2f, 1 });
        VJoint sphereJoint = hre.getObjectRootJoint("greensphere");
        sphereJoint.setTranslation(-0.8f, 2.1f, 0.2f);
        we.getWorldObjectManager().addWorldObject("greensphere", new WorldObject(sphereJoint));

        hre.loadBox("bluebox", new float[] { 0.05f, 0.05f, 0.05f }, RenderStyle.LINE, new float[] { 0.2f, 0.2f, 1, 1 }, new float[] { 0.2f,
                0.2f, 1, 1 }, new float[] { 0.2f, 0.2f, 1, 0 }, new float[] { 0.2f, 0.2f, 1, 1 });
        VJoint boxJoint = hre.getObjectRootJoint("bluebox");
        boxJoint.setTranslation(-1.4f, 1.5f, 0.5f);
        we.getWorldObjectManager().addWorldObject("bluebox", new WorldObject(boxJoint));

        hre.loadDisc("yellowdisc", 0.01f, 0.03f, 0.03f, 10, 10, RenderStyle.FILL, new float[] { 1f, 1f, 0.2f, 1 }, new float[] { 1f, 1f,
                0.2f, 1 }, new float[] { 1, 1, 0.2f, 0 }, new float[] { 1, 1, 0.2f, 1 });
        VJoint discJoint = hre.getObjectRootJoint("yellowdisc");
        discJoint.setTranslation(-1.9f, 1f, 0.3f);
        we.getWorldObjectManager().addWorldObject("yellowdisc", new WorldObject(discJoint));

        hre.loadCapsule("redcapsule", 0.1f, 0.8f, 8, 8, RenderStyle.FILL, new float[] { 1f, 0.2f, 0.2f, 1 }, new float[] { 1f, 0.8f, 0.8f,
                1 }, new float[] { 1, 0.2f, 0.2f, 1 }, new float[] { 1, 0.2f, 0.2f, 1 });
        VJoint capsuleJoint = hre.getObjectRootJoint("redcapsule");
        capsuleJoint.setTranslation(1f, 0.8f, 0.5f);
        we.getWorldObjectManager().addWorldObject("redcapsule", new WorldObject(capsuleJoint));

        hre.loadLine("whiteline", new float[] { -0.5f, 0, 0.4f, 1.5f, 0.8f, 0.6f }, new float[] { 1f, 1f, 1f, 1 }, new float[] { 1f, 1f,
                1f, 1 }, new float[] { 1, 1f, 1f, 0 }, new float[] { 1, 1f, 1f, 1 });
        VJoint lineJoint = hre.getObjectRootJoint("whiteline");
        we.getWorldObjectManager().addWorldObject("whiteline", new WorldObject(lineJoint));

        hre.loadAxisCross("axiscross", 10f);
        VJoint axisJoint = hre.getObjectRootJoint("axiscross");
        axisJoint.setTranslation(0f, 2f, 0.5f);
        we.getWorldObjectManager().addWorldObject("axiscross", new WorldObject(axisJoint));
        /**/

        // OK, generally, this demo class should not know about physics, but the code below shows how to make a sphere, take the vjoint of the sphere, attach it to a physical
        // sphere that resides in the OPE as a rigidbody.
        /*
         * 
         * OdeRigidBody phsphere = ope.createRigidBody("phsphere");
         * OdeMass m = new OdeMass();
         * m.setFromSphere(0.05f, 1);
         * m.adjustMass(10f);
         * phsphere.setMass(m);
         * phsphere.addSphere(0.2f);
         * phsphere.setTranslation(0.0f, 3f, 0.0f);
         * phsphere.addRotationBuffer(sphereJoint.getRotationBuffer());
         * phsphere.addTranslationBuffer(sphereJoint.getTranslationBuffer());
         */

        // requestShutdown(true,true);

    }

    public void requestShutdown(boolean closeWindow, boolean exitOnClose)
    {
        ae.requestShutdown();

        while (!ae.isShutdown())
        {
            log.debug("wait for shutdown");
            try
            {
                Thread.sleep(1000);
            }
            catch (Exception ex)
            {

            }
        }

        System.out.println("AsapEnvironment completely shut down.");
        if (closeWindow)
        {
            System.out.println("Closing main GUI now.");
            // method to programatically close the frame, from
            // http://stackoverflow.com/questions
            // /1234912/how-to-programmatically-close-a-jframe
            //
            mainUI.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            WindowEvent wev = new WindowEvent(mainUI, WindowEvent.WINDOW_CLOSING);
            Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
        }
        if (exitOnClose) System.exit(0);

    }

    public void windowClosing(WindowEvent e)
    {
        new Thread()
        {
            public void run()
            {
                requestShutdown(false, true);
            }
        }.start();
    }

}
