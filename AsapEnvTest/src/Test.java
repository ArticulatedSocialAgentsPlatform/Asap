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
import hmi.elckerlyc.world.WorldObject;
import hmi.mixedanimationenvironment.MixedAnimationEnvironment;
import hmi.physicsenvironment.OdePhysicsEnvironment;
import hmi.renderenvironment.HmiRenderEnvironment;
import hmi.util.DefaultDeadlockListener;
import hmi.util.EventDispatchThreadHangMonitor;
import hmi.util.ThreadDeadlockDetector;

import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;

import asap.environment.AsapEnvironment;
import asap.environment.AsapVirtualHuman;
import asap.utils.Environment;

/**
 * Elckerlyc demo.
 * 
 * @author Dennis Reidsma
 * @author Herwin van Welbergen
 */
public class Test
{

    static { 
      EventDispatchThreadHangMonitor.initMonitoring(); 
      new ThreadDeadlockDetector().addListener(new DefaultDeadlockListener());
    }


    /**
     * Start the ElckerlycEnvironment prog
     */
    public static void main(String[] arg)
    {
        System.setProperty("sun.java2d.noddraw", "true"); // avoid potential
                                                          // interference with
                                                          // (non-Jogl) Java
                                                          // using direct draw
        HmiRenderEnvironment hre = new HmiRenderEnvironment();
        OdePhysicsEnvironment ope = new OdePhysicsEnvironment();
        MixedAnimationEnvironment mae = new MixedAnimationEnvironment();
        AsapEnvironment ae = new AsapEnvironment();
        AudioEnvironment aue = new AudioEnvironment("WAV_CLIP");
        
        JFrame jf = new JFrame("Test new HmiEnvironment");
        jf.setSize(1000,600);
        
        hre.init(); //canvas does not exist until init was called
        ope.init();
        aue.init();
        mae.init(ope);
        ArrayList<Environment> environments = new ArrayList<Environment>();
        environments.add(hre);
        environments.add(ope);
        environments.add(mae);
        environments.add(aue);
        environments.add(ae);
        
        ae.init(environments, ope.getPhysicsSchedulingClock()); //if no physics, just use renderclock here!
        ope.addPrePhysicsCopyListener(ae); // this clock method drives the engines in ee. if no physics, then register ee as a listener at the render clock!
        
        java.awt.Component canvas = hre.getAWTComponent(); //after init, get canvas and add to window
        jf.add(canvas);
        jf.setVisible(true);
        
        
        hre.startRenderClock();
        ope.startPhysicsClock();
        
        //add worldobject "camera" that we can use to look at user :)
        VJoint camera = hre.getCameraTarget(); 
        ae.getWorldObjectManager().addWorldObject("camera", new WorldObject(camera));
        try
        {
            AsapVirtualHuman avh = ae.loadVirtualHuman("armandia1", "Humanoids/armandia", "asapvhloader_test.xml", "armandia - testing new environment setup");
            //AsapVirtualHuman avh2 = ae.loadVirtualHuman("armandia2", "Humanoids/armandia", "loader2.xml", "another armandia - testing new environment setup"); 
        }
        catch (IOException ex)
        {
            System.out.println("Cannot load VH");
        }
        
        //loading and using objects...
        
        /*
        hre.loadObject("cokecan", "Shared3DModels/cokecan", "Shared3DModels/cokecan", "cokecan_scale0.01.dae");
        VJoint cokecanjoint = hre.getObjectRootJoint("cokecan");
        hre.unloadObject("cokecan");
        */
        
        //USE THIS IF YOU JUST WANT THE HUMANOID, AND PREFER TO TAKE CARE OF THE ANIMATION YOURSELF (WITHOUT THE BML REALIZER)      
        /*
        hre.loadHumanoid("armandia", "Humanoids/armandia/bin", "Humanoids/armandia/maps", null, "armandia_boring_neckfix_light_toplevel.bin", "ARMANDIA", new HashMap<String, Float>());
        VJoint armandiaRoot = hre.getHumanoidRootJoint("armandia");
        armandiaRoot.setTranslation(1,1,1);
        */

        //OK, generally, this demo class should not know about physics, but the code below shows how to make a sphere, take the vjoint of the sphere, attach it to a physical sphere that resides in the OPE as a rigidbody.
        /*
        hre.loadSphere("sphere", 0.2f, 25, 25, new float[]{ 1, 0, 0, 1 },  new float[]{ 1, 1, 1, 1 }, new float[]{ 0, 0, 0, 0 }, new float[]{ 0, 0, 0, 1 });
        VJoint sphereJoint = hre.getObjectRootJoint("sphere");

        OdeRigidBody phsphere = ope.createRigidBody("phsphere");
        OdeMass m = new OdeMass();
        m.setFromSphere(0.05f, 1);
        m.adjustMass(10f);
        phsphere.setMass(m);
        phsphere.addSphere(0.2f);
        phsphere.setTranslation(0.0f, 3f, 0.0f);
        phsphere.addRotationBuffer(sphereJoint.getRotationBuffer());
        phsphere.addTranslationBuffer(sphereJoint.getTranslationBuffer());
        */

        
        
//close down:
//shut down physics env
//shut down render env
//do we need to unload anything further?
//shut down all engines
//exit all frames
        
    }




}
