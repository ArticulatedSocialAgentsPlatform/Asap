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
package asap.picture.swing;

import hmi.environmentbase.Embodiment;
import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;

import asap.picture.display.PictureDisplay;
import asap.picture.loader.PictureEmbodiment;


/** Take care of its own loading from XML. */
public class JFramePictureEmbodiment implements EmbodimentLoader, PictureEmbodiment
{
    private PictureDisplay display;

    private String id = "";

    private int width = 400;
    private int height = 400;

    public void setId(String id)
    {
        this.id = id;
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments, Loader ... requiredLoaders) 
    	throws IOException
    {
        setId(loaderId);
        HashMap<String, String> attrMap = null;
        XMLStructureAdapter adapter = new XMLStructureAdapter();
        if (tokenizer.atSTag("Size"))
        {
            attrMap = tokenizer.getAttributes();
        	width = adapter.getRequiredIntAttribute("width", attrMap, tokenizer);
        	height = adapter.getRequiredIntAttribute("height", attrMap, tokenizer);
            tokenizer.takeEmptyElement("Size");            
        } 

        // initialize the picturedisplay
        display = new PictureJFrame(width,height);
        return;
    }

    @Override
    public void unload()
    {
    }

    @Override
    public Embodiment getEmbodiment()
    {
        return this;
    }

    public PictureDisplay getPictureDisplay()
    {
        return display;
    }
}
