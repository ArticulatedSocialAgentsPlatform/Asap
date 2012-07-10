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
package asap.nao.bml;

import hmi.xml.XMLTokenizer;
import java.io.IOException;
import java.util.HashMap;

/**
 * Plays Choregraphe clip
 * @author Robin ten Buuren
 */
public class NaoPlayChoregrapheClipBehaviour extends NaoBehaviour
{
	
	private String filename;
	
	/**
	 * The actions needs a filename, so the Nao knows which file it should play
	 */
	
	@Override
    public boolean satisfiesConstraint(String n, String value)
    {
        if (n.equals("filename") && filename.toString().equals(value)) return true;
        return false;
    }	
	
	public  NaoPlayChoregrapheClipBehaviour(String bmlId, XMLTokenizer tokenizer) throws IOException
	{
		super(bmlId);
		readXML(tokenizer);
	}
	
    @Override
    public StringBuilder appendAttributeString(StringBuilder buf)
    {
        appendAttribute(buf, "filename", filename.toString());
        return super.appendAttributeString(buf);
    }
	
	 @SuppressWarnings("static-access")
	@Override
	    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
	    {
	        filename = filename.valueOf(getRequiredAttribute("filename", attrMap, tokenizer));
	        super.decodeAttributes(attrMap, tokenizer);
	    }
	
    /*
     * The XML Stag for XML encoding
     */
    private static final String XMLTAG = "playchoregrapheclip";

    /**
     * The XML Stag for XML encoding -- use this static method when you want to see if a given
     * String equals the xml tag for this class
     */
    public static String xmlTag()
    {
        return XMLTAG;
    }

    /**
     * The XML Stag for XML encoding -- use this method to find out the run-time xml tag of an
     * object
     */
    @Override
    public String getXMLTag()
    {
        return XMLTAG;
    }
    
	@Override
	public float getFloatParameterValue(String arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	/**
	 * The actions needs a filename, so the Nao knows which file it should play
	 */

	@Override
	public String getStringParameterValue(String name) {
        if (name.equals("filename"))
        {
        	return filename;
        }
        return "";
	}
	
	/**
	 * The actions needs a filename, so the Nao knows which file it should play
	 */

	@Override
	public boolean specifiesParameter(String name) {
		 return name.equals("filename");
	}
}
