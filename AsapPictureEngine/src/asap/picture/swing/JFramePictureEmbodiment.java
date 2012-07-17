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

import asap.picture.display.PictureDisplay;
import asap.picture.loader.PictureEmbodiment;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import asap.environment.AsapVirtualHuman;
import asap.environment.EmbodimentLoader;
import asap.environment.Loader;
import asap.utils.Embodiment;
import asap.utils.Environment;

/** Take care of its own loading from XML. */
public class JFramePictureEmbodiment implements EmbodimentLoader, Embodiment, PictureEmbodiment
{
    private PictureDisplay display;

    private String id = "";

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
    public void readXML(XMLTokenizer tokenizer, String newId, AsapVirtualHuman avh, Environment[] environments, Loader... requiredLoaders)
            throws IOException
    {
        setId(newId);

        // initialize the picturedisplay
        display = new PictureJFrame();
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
