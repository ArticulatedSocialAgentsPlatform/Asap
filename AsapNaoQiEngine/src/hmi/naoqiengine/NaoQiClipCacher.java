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
package hmi.naoqiengine;

import java.util.ArrayList;
import java.util.List;

import saiba.bml.BMLInfo;
import saiba.bml.core.Behaviour;


import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.jnaoqiembodiment.NaoQiEmbodiment;
import hmi.jnaoqiembodiment.loader.NaoQiEmbodimentLoader;
import hmi.util.Console;
import hmi.util.SystemClock;

import java.awt.event.WindowEvent;
import java.io.IOException;

import asap.bml.ext.bmlt.BMLTInfo;
import asap.environment.AsapEnvironment;
import asap.environment.AsapVirtualHuman;


import com.aldebaran.proxy.*;

public class NaoQiClipCacher
{
	NaoQiEmbodiment nqe = null;
	ALBehaviorManagerProxy bmp = null;
	
	public NaoQiClipCacher(NaoQiEmbodiment nqe)
	{
		this.nqe = nqe;
		bmp = nqe.getBehaviorManagerProxy();
		initClipCache();
	}
	
	protected void initClipCache()
	{
		String[] ibs = bp.getInstalledBehaviors();
		for (int i = 0; i < ibs.length;i++)
		{
			take next string; 
			remove numerical suffix;
			get the
		}

	}
	
	    
		System.out.println("gdsfsdfds");
        bp.runBehavior("ledfade");
        bp.runBehavior("earon");
        bp.preloadBehavior("ledfade");
        bp.preloadBehavior("earon"); //kan dit ook multithreaded tijdens het uitvoeren?
        System.out.println("fvsd");
        bp.runBehavior("ledfade");
        bp.runBehavior("earon");
        bp.runBehavior("ledfade");
        bp.preloadBehavior("earon");
        bp.runBehavior("earon");
		
}
