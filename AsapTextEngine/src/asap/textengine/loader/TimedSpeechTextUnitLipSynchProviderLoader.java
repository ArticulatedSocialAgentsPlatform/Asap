/**
 * *****************************************************************************
 * Copyright (C) 2009 Human Media Interaction, University of Twente, the
 * Netherlands
 *
 * This file is part of the Elckerlyc BML realizer.
 *
 * Elckerlyc is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Elckerlyc is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Elckerlyc. If not, see http://www.gnu.org/licenses/.
 * ****************************************************************************
 */
package asap.textengine.loader;

import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import asap.realizer.lipsync.LipSynchProvider;
import asap.realizerembodiments.LipSynchProviderLoader;
import asap.textengine.TextEngineLoader;
import asap.textengine.lipsync.TimedSpeechTextUnitLipSynchProvider;

/**
 * Loader for a TimedTextSpeechUnitLipSynchProvider.
 * 
 * @author Jordi Hendrix
 */
public class TimedSpeechTextUnitLipSynchProviderLoader implements LipSynchProviderLoader
{

    private String id;
    private LipSynchProvider lipSyncProvider;

    public void setId(String newId)
    {
        id = newId;
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
            Loader... requiredLoaders) throws IOException
    {
        TextEngineLoader tel = null;

        for (Loader e : requiredLoaders)
        {
            if (e instanceof TextEngineLoader)
            {
                tel = (TextEngineLoader) e;
            }
        }

        if (tel == null)
        {
            throw new RuntimeException("TimedTextSpeechUnitLipSynchProviderLoader requires an Embodiment of type TextSpeechEngineLoader");
        }

        lipSyncProvider = new TimedSpeechTextUnitLipSynchProvider(tel.getPlanManager(), tel.getTextOutput());
    }

    @Override
    public void unload()
    {
    }

    @Override
    public LipSynchProvider getLipSyncProvider()
    {
        return lipSyncProvider;
    }
}
