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
package asap.faceengine.loader;

import hmi.faceanimation.FaceController;
import hmi.faceanimation.FaceEmbodiment;
import hmi.faceanimation.util.XFaceController;
import hmi.faceanimation.util.XfaceInterface;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;

import asap.environment.AsapVirtualHuman;
import asap.environment.EmbodimentLoader;
import asap.environment.Loader;
import asap.utils.CopyEmbodiment;
import asap.utils.Embodiment;
import asap.utils.Environment;

public class XFaceEmbodiment implements FaceEmbodiment, CopyEmbodiment, EmbodimentLoader
{
  
  private XMLStructureAdapter adapter = new XMLStructureAdapter();

  private FaceController faceController = null;
  private XfaceInterface xfi = null;
  
  String id = "";

  @Override
  public void unload()
  {
    xfi.disconnect();
  }
  public String getId()
  {
    return id;
  }
  @Override
  public void readXML(XMLTokenizer tokenizer, String newId, AsapVirtualHuman avh, Environment[] environments, Loader ... requiredLoaders) throws IOException
  {
    id=newId;

    while (!tokenizer.atETag("Loader")) 
    {
      readSection(tokenizer);
    }
  }
  protected void readSection(XMLTokenizer tokenizer) throws IOException
  {
    HashMap<String, String> attrMap = null;
    if (tokenizer.atSTag("XFaceHost"))
    {
      attrMap = tokenizer.getAttributes();
      tokenizer.takeSTag("XFaceHost");
      int port = adapter.getOptionalIntAttribute("port", attrMap, 50011);
      xfi = new XfaceInterface(port);
      faceController = new XFaceController(xfi);
      xfi.connect();
      tokenizer.takeETag("XFaceHost");
    }
    else
    {
      throw tokenizer.getXMLScanException("Unknown tag in Embodiment content");
    }
  }

  
  @Override
  public Embodiment getEmbodiment()
  {
    return this;
  }

  @Override
  public FaceController getFaceController()
  {
    return faceController;
  }
  public void copy()
  {
    faceController.copy();
  } 
}