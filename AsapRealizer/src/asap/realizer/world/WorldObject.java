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

import hmi.animation.VJoint;
import hmi.math.Mat4f;
import hmi.math.Vec3f;
import asap.utils.AnimationSync;

/**
 * Contains an entity in the world that can be pointed at, talked about etc.
 * @author welberge
 */
public class WorldObject
{
    private VJoint joint;
    private float[] mTemp1 = new float[16];
    private float trTemp[] = new float[3];
    private float trTempToParent[] = new float[3];
        
    public WorldObject(VJoint vj)
    {
        joint = vj;
    }
    
    /**
     * Get the world position of the object
     * @param tr output: the world position of the object
     */
    public void getWorldTranslation(float[]tr)
    {
        synchronized(AnimationSync.getSync())
        {
            joint.calculateMatrices();
            joint.getPathTranslation(null, tr);
        }
    }
    
    
    /**
     * Get the position of the world object in the coordinate system of vj.
     * This joint and vj are assumed to be in the same joint tree, but can be in different branches of the tree. 
     * @param tr output: the position of the world object
     */
    public void getTranslation(float tr[], VJoint vj)
    {
        synchronized(AnimationSync.getSync())
        {
            joint.getPathTranslation(null, trTemp);     //world pos of the joint
            if(vj!=null)
            {
                vj.getPathTransformMatrix(null, mTemp1);      
                
            }
            else
            {
                Mat4f.setIdentity(mTemp1);
            }
        }
        Mat4f.invertRigid(mTemp1);
        Mat4f.transformPoint(mTemp1, tr, trTemp);
        
    }

    /**
     * Get the position of the world object in the coordinate system of vj, minus the rotation of vj.
     * This joint and vj are assumed to be in the same joint tree, but can be in different branches of the tree. 
     * @param tr output: the position of the world object
     */
    public void getTranslation2(float tr[], VJoint vj)
    {
        synchronized(AnimationSync.getSync())
        {
            joint.getPathTranslation(null, trTemp);     //world pos of the joint
            
            if(vj!=null)
            {
                vj.getParent().getPathTransformMatrix(null, mTemp1);            
                vj.getTranslation(trTempToParent);
            }
            else
            {
                Mat4f.setIdentity(mTemp1);
                Vec3f.set(trTempToParent,0,0,0);
            }
        }
        Mat4f.invertRigid(mTemp1);
        Mat4f.transformPoint(mTemp1, tr, trTemp);
        Vec3f.sub(tr, trTempToParent);
    }
    
    /**
     * @return the joint
     */
    public VJoint getJoint()
    {
        return joint;
    }
}
