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
package asap.environment;

import hmi.xml.XMLTokenizer;

import java.io.IOException;

import asap.utils.Environment;

/**
Superinterface of all loaders (embodiment loader, engine loader)
*/
public interface Loader
{
  
  String getId();

  /** Construct an engine or embodiment from the XML specification. Takes care of reading the content 
  within the <Engine> or <Embodiment> tag (but not that tag). */
  void readXML(XMLTokenizer tokenizer, String newId, AsapVirtualHuman avh, Environment[] environments, Loader ... requiredLoaders) throws IOException;

  void unload();  
}