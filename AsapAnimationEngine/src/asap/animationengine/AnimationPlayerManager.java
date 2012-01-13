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
package asap.animationengine;

import java.util.ArrayList;

import net.jcip.annotations.GuardedBy;

import hmi.animation.AnimationSync;
import hmi.animation.VJoint;
import hmi.animation.VObjectTransformCopier;
import hmi.physics.PhysicsSync;

/**
 * Manages the playback and physical simulation of multiple AnimationPlayers
 * @author Herwin van Welbergen
 *
 */
public class AnimationPlayerManager
{
    @GuardedBy("this")
    private VObjectTransformCopier votc;
    
    @GuardedBy("this")
    private ArrayList<AnimationPlayer> players = new ArrayList<AnimationPlayer>();
    
    @GuardedBy("this")
    private double prevTime = 0;
    
    @GuardedBy("PhysicsSync.getSync(),AnimationSync.getSync()") //TODO: both needed??
    private PhysicsCallback physicsCallback;
    
    private static final float PHYSICS_STEPTIME = 0.003f;
    private VJoint animationRootJoint;
    private VJoint currentRootJoint;

    /**
     * ASSUMPTION: the humanoidroots are added directly under animationRoot
     * 
     * In each frame: 
     * 1. Run all animation players, in each animation player: <br>
     * a  Copy next to cur, cur to prev <br>
     * b Run procedural animations on next <br>
     * c Calculate the forces and torques generated by the proc motion <br>
     * 2. Do a physics step <br>
     * 3. Copy physical body rotations to cur and next <br>
     * 4. Copy curr to the animation tree <br>
     * 
     */
    public AnimationPlayerManager(PhysicsCallback pb, VJoint animationRoot,
            VJoint currRoot)
    {
        votc = VObjectTransformCopier.newInstanceFromMatchingVObjectLists(currRoot.getParts(), animationRoot.getParts(), "TR"); 
        /*
         * VOTC operates on sid equals OR id equals OR name equals. This gives
         * problems if there are three humanoids in the tree -- everything will
         * be copied to the first humanoid. therefore we use the ugly hacked
         * version of VOTC constructor. ASSUMPTION: the humanoidroots are added
         * directly under animationRoot
         */

        physicsCallback = pb;
        animationRootJoint = animationRoot;
        currentRootJoint = currRoot;
    }
    
    /**
     * Removes the animationplayer. Make sure the VJoint controlled by p is removed from the animationRootJoint.
     */
    public synchronized void removeAnimationPlayer(AnimationPlayer p)
    {
        players.remove(p);
        // recreate the VOTC because the VJoint controlled by p might have been removed        
        // from the animationRootJoint...
        votc = VObjectTransformCopier.newInstanceFromMatchingVObjectLists(currentRootJoint.getParts(),
                animationRootJoint.getParts(), "TR");
    }
    
    public synchronized void addAnimationPlayer(AnimationPlayer p)
    {
        players.add(p);
        // recreate the VOTC because the new animationplayer may have been
        // created for a human that was only recently added to the
        // animationworldroot...
        votc = VObjectTransformCopier.newInstanceFromMatchingVObjectLists(currentRootJoint.getParts(),
                animationRootJoint.getParts(), "TR");
    }

    /* nodig?
    public synchronized void reset()
    {
        prevTime = 0;

        for (AnimationPlayer p : players)
        {
            p.reset();
        }
    }
     */
    public synchronized void time(double currentTime)
    {
        float time = (float) (currentTime - prevTime);

        // handle resetSimulation
        if (time < 0)
        {
            prevTime = currentTime;
            return;
        }

        if (time > PHYSICS_STEPTIME)
        {
            while (time > PHYSICS_STEPTIME)
            {
                for (AnimationPlayer p : players)
                {
                    p.play(prevTime);                    
                }
                synchronized (PhysicsSync.getSync())
                {
                    physicsCallback.time(PHYSICS_STEPTIME);
                }
                time -= PHYSICS_STEPTIME;
                prevTime += PHYSICS_STEPTIME;
            }
        } else
        {
            return;
        }

        for (AnimationPlayer p : players)
        {
            p.copyPhysics();
        }

        synchronized (AnimationSync.getSync())
        {
            votc.copyConfig();
        }
    }

    /**
     * @return the PHYSICS_STEPTIME
     */
    public static float getH()
    {
        return PHYSICS_STEPTIME;
    }

    /**
     * @return the prevTime
     */
    public synchronized double getPrevTime()
    {
        return prevTime;
    }
}
