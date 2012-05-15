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
package asap.realizer.world;

import static org.junit.Assert.*;
import hmi.animation.VJoint;
import hmi.math.Quat4f;
import hmi.math.Vec3f;

import org.junit.Test;

import asap.realizer.world.WorldObject;

/**
 * Unit testcases for WorldObject
 * @author welberge
 *
 */
public class WorldObjectTest
{
    @Test
    public void testGetWorldTranslation()
    {
        VJoint vj1 = new VJoint();
        VJoint vjWorld = new VJoint();
        vjWorld.setTranslation(0,10,0);
        vj1.setTranslation(10,0,0);
        vjWorld.addChild(vj1);
        WorldObject wj = new WorldObject(vj1);
        float[]trRef = {10,10,0};
        float[] tr = new float[3];
        wj.getWorldTranslation(tr);
        assertTrue(Vec3f.epsilonEquals(tr, trRef, 0.0001f));
    }
    
    @Test
    public void testGetTranslation()
    {
        VJoint vj1 = new VJoint();
        VJoint vj2 = new VJoint();
        VJoint vjWorld = new VJoint();
        vj1.setTranslation(10,0,0);
        vj2.setTranslation(-10,0,0);
        float q[] = new float[4];
        Quat4f.setFromAxisAngle4f(q, 0,0,1,(float)Math.PI * 0.5f);
        vj2.setRotation(q);
        vjWorld.addChild(vj1);
        vjWorld.addChild(vj2);
        WorldObject wj = new WorldObject(vj1);
        float[]trRef = {0,-20,0};
        float[] tr = new float[3];
        wj.getTranslation(tr, vj2);
        assertTrue(Vec3f.epsilonEquals(tr, trRef, 0.0001f));      
    }
}
