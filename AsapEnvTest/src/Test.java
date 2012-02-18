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
import hmi.renderenvironment.HmiRenderEnvironment.RenderStyle;
import hmi.util.DefaultDeadlockListener;
import hmi.util.EventDispatchThreadHangMonitor;
import hmi.util.ThreadDeadlockDetector;

import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;

import lombok.extern.slf4j.Slf4j;
import asap.environment.AsapEnvironment;
import asap.environment.AsapVirtualHuman;
import asap.utils.Environment;

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
	
    static { 
      EventDispatchThreadHangMonitor.initMonitoring(); 
      new ThreadDeadlockDetector().addListener(new DefaultDeadlockListener());
    }


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
        System.setProperty("sun.java2d.noddraw", "true"); // avoid potential
                                                          // interference with
                                                          // (non-Jogl) Java
                                                          // using direct draw
        HmiRenderEnvironment hre = new HmiRenderEnvironment();
        OdePhysicsEnvironment ope = new OdePhysicsEnvironment();
        MixedAnimationEnvironment mae = new MixedAnimationEnvironment();
        ae = new AsapEnvironment();
        AudioEnvironment aue = new AudioEnvironment("WAV_CLIP");
        
        mainUI = new JFrame("Test new HmiEnvironment");
        mainUI.setSize(1000,600);
        mainUI.addWindowListener(this);
        
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
        mainUI.add(canvas);
        mainUI.setVisible(true);
        
        
        hre.startRenderClock();
        ope.startPhysicsClock();
        
        //add worldobject "camera" that we can use to look at user :)
        VJoint camera = hre.getCameraTarget(); 
        ae.getWorldObjectManager().addWorldObject("camera", new WorldObject(camera));
        try
        {
        	AsapVirtualHuman avh = ae.loadVirtualHuman("blueguy", "Humanoids/blueguy", "blueguy_asaploader_mary_hudson.xml", "blueguy - test avatar");
        	//AsapVirtualHuman avh = ae.loadVirtualHuman("billie", "Humanoids/billie", "asaploader_billie.xml", "billie");
        	//AsapVirtualHuman avh = ae.loadVirtualHuman("armandia1", "Humanoids/armandia", "asapvhloader_test.xml", "armandia - testing new environment setup");
            //AsapVirtualHuman avh2 = ae.loadVirtualHuman("armandia2", "Humanoids/armandia", "loader2.xml", "another armandia - testing new environment setup"); 
        }
        catch (IOException ex)
        {
            System.out.println("Cannot load VH");
        }
        
        //loading and using objects...
        

        //hre.loadObject("psychoroom", "Shared3DModels/psychoroom", "Shared3DModels/psychoroom", "psychological_room.dae");

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

        hre.loadSphere("greensphere", 0.05f, 25, 25, RenderStyle.FILL, new float[]{ 0.2f, 1, 0.2f, 1 },  new float[]{ 0.2f, 1, 0.2f, 1 }, new float[]{ 0.2f, 1, 0.2f, 0 }, new float[]{ 0.2f, 1, 0.2f, 1 });
        VJoint sphereJoint = hre.getObjectRootJoint("greensphere");
        sphereJoint.setTranslation(-0.8f, 2.1f, 0.2f);
        ae.getWorldObjectManager().addWorldObject("greensphere", new WorldObject(sphereJoint));

        hre.loadBox("bluebox", new float[]{0.05f,0.05f,0.05f}, RenderStyle.FILL, new float[]{ 0.2f, 0.2f, 1, 1 },  new float[]{ 0.2f, 0.2f, 1, 1 }, new float[]{ 0.2f, 0.2f, 1, 0 }, new float[]{ 0.2f, 0.2f, 1, 1 });
        VJoint boxJoint = hre.getObjectRootJoint("bluebox");
        boxJoint.setTranslation(-0.4f, 1.5f, 0.5f);
        ae.getWorldObjectManager().addWorldObject("bluebox", new WorldObject(boxJoint));

        //OK, generally, this demo class should not know about physics, but the code below shows how to make a sphere, take the vjoint of the sphere, attach it to a physical sphere that resides in the OPE as a rigidbody.
        /*
        
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

        //requestShutdown(true,true);

    }
    public void requestShutdown(boolean closeWindow, boolean exitOnClose)
    {
        ae.requestShutdown();
        //no need to pull the plug on the JFrame as the application will end now?
        while (!ae.isShutdown()){
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
    	new Thread(){
    		public void run()
    		{
    			requestShutdown(false,true);
    		}
    	}.start();
    }


}
