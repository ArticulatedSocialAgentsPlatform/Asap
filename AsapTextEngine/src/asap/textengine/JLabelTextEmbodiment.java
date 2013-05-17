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
package asap.textengine;

import hmi.environmentbase.Embodiment;
import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.jcomponentenvironment.JComponentEmbodiment;
import hmi.textembodiments.TextEmbodiment;
import hmi.util.ArrayUtils;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/** Take care of its own loading from XML. */
public class JLabelTextEmbodiment implements TextEmbodiment, EmbodimentLoader
{

    private JComponentEmbodiment jce = null;
    private JLabel textLabel = new JLabel();
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

    /** No loading necessary, actually! Empty content expected. No required embodiments */
    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
            Loader... requiredLoaders) throws IOException
    {
        setId(loaderId);
        for (EmbodimentLoader e : ArrayUtils.getClassesOfType(requiredLoaders, EmbodimentLoader.class))
        {
            if (e.getEmbodiment() instanceof JComponentEmbodiment)
            {
                jce = (JComponentEmbodiment) e.getEmbodiment();
            }
        }
        if (jce == null)
        {
            throw new RuntimeException("JLabelTextEmbodiment requires an Embodiment of type JComponentEmbodiment");
        }
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                jce.addJComponent(textLabel);
            }
        });        
    }

    @Override
    public void unload()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                jce.removeJComponent(textLabel);
            }
        });
    }

    /** Return this embodiment */
    @Override
    public Embodiment getEmbodiment()
    {
        return this;
    }

    /** print to stdout */
    @Override
    public void setText(String text)
    {
        textLabel.setText(text);
    }
}
