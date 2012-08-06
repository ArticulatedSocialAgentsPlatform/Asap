/*******************************************************************************
 * 
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
package hmi.relion;

import hmi.animation.AnimationDistributor2;
import hmi.animation.Hanim;
import hmi.animation.VJoint;
import hmi.animationembodiments.SkeletonEmbodiment;
import hmi.environmentbase.CopyEmbodiment;
import hmi.environmentbase.CopyEnvironment;
import hmi.environmentbase.Embodiment;
import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.util.SystemClock;
import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import asap.environment.AsapVirtualHuman;

@Slf4j
public class RelionEmbodiment implements Embodiment, CopyEmbodiment, SkeletonEmbodiment, EmbodimentLoader
{
    @Getter
    private String id = "";

    private XMLStructureAdapter adapter = new XMLStructureAdapter();

    @Getter
    private VJoint animationVJoint = null;

    private AsapVirtualHuman theVirtualHuman = null;

    private AnimationDistributor2 animDistr = null;
    CopyEnvironment ce = null;

    @Override
    public void readXML(XMLTokenizer tokenizer, String newId, AsapVirtualHuman avh, Environment[] environments, Loader... requiredLoaders)
            throws IOException
    {
        id = newId;
        theVirtualHuman = avh;

        for (Environment e : environments)
        {
            if (e instanceof CopyEnvironment) ce = (CopyEnvironment) e;
        }
        if (ce == null)
        {
            throw new RuntimeException("RelionEmbodiment requires an Environment of type CopyEnvironment");
        }

        if (!tokenizer.atSTag("relionconnection"))
        {
            throw new XMLScanException("Relion embodiment loader requires child element of type relionconnection");
        }
        HashMap<String, String> attrMap = tokenizer.getAttributes();
        String server = adapter.getRequiredAttribute("server", attrMap, tokenizer);
        int serverport = adapter.getRequiredIntAttribute("serverport", attrMap, tokenizer);
        int senderport = adapter.getRequiredIntAttribute("senderport", attrMap, tokenizer);

        log.error("Cannot deal with other hosts than localhost!");

        try
        {
            animDistr = new AnimationDistributor2(serverport, senderport);
        }
        catch (Exception e)
        {
            log.error("Note: Cannot open connection to Relion", e);
        }
        // finally
        {
            log.debug("started connection...");

            animationVJoint = animDistr.getSkeleton().getRoot();
            animationVJoint.setSid(Hanim.HumanoidRoot);

            animationVJoint.getPart("LeftUpperLeg").setSid(Hanim.l_hip);
            animationVJoint.getPart("LeftLowerLeg").setSid(Hanim.l_knee);
            animationVJoint.getPart("LeftFoot").setSid(Hanim.l_ankle);
            animationVJoint.getPart("LeftToe").setSid(Hanim.l_midtarsal);

            animationVJoint.getPart("RightUpperLeg").setSid(Hanim.r_hip);
            animationVJoint.getPart("RightLowerLeg").setSid(Hanim.r_knee);
            animationVJoint.getPart("RightFoot").setSid(Hanim.r_ankle);
            animationVJoint.getPart("RightToe").setSid(Hanim.r_midtarsal);

            animationVJoint.getPart("LeftShoulder").setSid(Hanim.l_sternoclavicular);
            animationVJoint.getPart("LeftUpperArm").setSid(Hanim.l_shoulder);
            animationVJoint.getPart("LeftForeArm").setSid(Hanim.l_elbow);
            animationVJoint.getPart("LeftHand").setSid(Hanim.l_wrist);

            animationVJoint.getPart("RightShoulder").setSid(Hanim.r_sternoclavicular);
            animationVJoint.getPart("RightUpperArm").setSid(Hanim.r_shoulder);
            animationVJoint.getPart("RightForeArm").setSid(Hanim.r_elbow);
            animationVJoint.getPart("RightHand").setSid(Hanim.r_wrist);

            animationVJoint.getPart("L5").setSid(Hanim.vl5);
            animationVJoint.getPart("L3").setSid(Hanim.vl3);
            animationVJoint.getPart("T12").setSid(Hanim.vt12);
            animationVJoint.getPart("T8").setSid(Hanim.vt8);
            animationVJoint.getPart("Neck").setSid(Hanim.vc4);
            animationVJoint.getPart("Head").setSid(Hanim.skullbase);

            ce.addCopyEmbodiment(this);
        }
        tokenizer.takeSTag("relionconnection");
        tokenizer.takeETag("relionconnection");

    }

    @Override
    public void unload()
    {
        ce.removeCopyEmbodiment(this);
        log.error("Removing relion embodiment. tell suit to unload avatar? disconnect port?");

    }

    @Override
    public Embodiment getEmbodiment()
    {
        return this;
    }

    @Override
    public void copy()
    {
        animationVJoint.calculateMatrices();
        animDistr.snapshot();
    }

}
