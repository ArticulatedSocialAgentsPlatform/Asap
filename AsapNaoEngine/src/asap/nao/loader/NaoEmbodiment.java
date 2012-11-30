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
package asap.nao.loader;

import hmi.environmentbase.Embodiment;
import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import asap.nao.Nao;

/** Take care of its own loading from XML. */
public class NaoEmbodiment implements EmbodimentLoader, Embodiment
{
    private Nao nao = null;

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

    /**
     * Creates a Nao class to get a connection with the Nao
     */
    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
            Loader... requiredLoaders) throws IOException
    {
        setId(loaderId);
        nao = new Nao();
        return;
    }

    @Override
    public void unload()
    {
        throw new RuntimeException("Missing functionality to unload Nao");
    }

    /** Return this embodiment */
    @Override
    public Embodiment getEmbodiment()
    {
        return this;
    }

    /** provide access to the abstract Nao interface of Nao, which independent of whether it is a robot nao or a simulation nao. */

    public Nao getNao()
    {
        return nao;
    }

}
