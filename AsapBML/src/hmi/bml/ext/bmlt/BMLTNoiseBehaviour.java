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
package hmi.bml.ext.bmlt;

import saiba.bml.parser.SyncPoint;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * This class represents noise on a joint. 
 * This is represented in BML by the <code>&lt;noise&gt;</code>-tag, 
 * in the http://hmi.ewi.utwente.nl/bmlt namespace.
 * 
 * @author dennisr
 */
public class BMLTNoiseBehaviour extends BMLTBehaviour
{
    protected String joint;
    protected String type;

    @Override
    public String getStringParameterValue(String name)
    {
        if (name.equals("joint")) return joint;
        if (name.equals("type")) return type;
        return super.getStringParameterValue(name);
    }
    
    private static final List<String> DEFAULT_SYNCS = ImmutableList.of("start","end");    
    public static List<String> getDefaultSyncPoints()
    {
        return DEFAULT_SYNCS;
    }
    
    @Override
    public void addDefaultSyncPoints()
    {
        for(String s:getDefaultSyncPoints())
        {
            addSyncPoint(new SyncPoint(bmlId, id, s));
        }        
    }

    @Override
    public boolean specifiesParameter(String name)
    {
        if (name.equals("joint") || name.equals("type") || name.equals("offsetx") || name.equals("offsety") || name.equals("offsetz")
                || name.equals("basefreqx") || name.equals("basefreqy") || name.equals("basefreqz") || name.equals("baseamplitudex")
                || name.equals("baseamplitudey") || name.equals("baseamplitudez") || name.equals("persistencex")
                || name.equals("persistencey") || name.equals("persistencez")) return true;
        return false;
    }

    @Override
    public boolean satisfiesConstraint(String n, String value)
    {
        if (n.equals("joint") && value.equals(joint)) return true;
        if (n.equals("type") && value.equals(type)) return true;
        return super.satisfiesConstraint(n, value);
    }

    public void setFloatParameterValue(String name, float value)
    {
        setParameterValue(name, "" + value);
    }

    public void setParameterValue(String name, String value)
    {
        BMLTParameter param = new BMLTParameter();
        param.name = name;
        param.value = value;
        parameters.put(name, param);
    }

    public BMLTNoiseBehaviour(String bmlId, XMLTokenizer tokenizer) throws IOException
    {
        super(bmlId);
        setParameterValue("joint", "skullbase");
        setFloatParameterValue("offsetx", -0.1f);
        setFloatParameterValue("offsety", 0f);
        setFloatParameterValue("offsetz", 0f);
        setFloatParameterValue("basefreqx", 1f);
        setFloatParameterValue("basefreqy", 1f);
        setFloatParameterValue("basefreqz", 1f);
        setFloatParameterValue("baseamplitudex", 0.5f);
        setFloatParameterValue("baseamplitudey", 0f);
        setFloatParameterValue("baseamplitudez", 0f);
        setFloatParameterValue("persistencex", 0.5f);
        setFloatParameterValue("persistencey", 0.5f);
        setFloatParameterValue("persistencez", 0.5f);
        readXML(tokenizer);
    }

    @Override
    public StringBuilder appendAttributeString(StringBuilder buf)
    {
        return super.appendAttributeString(buf);
    }

    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        super.decodeAttributes(attrMap, tokenizer);
        joint = getRequiredAttribute("joint", attrMap, tokenizer);
        type = getRequiredAttribute("type", attrMap, tokenizer);
    }

    /*
     * The XML Stag for XML encoding
     */
    private static final String XMLTAG = "noise";

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

    /**
     * @return the content
     */
    public String getContent()
    {
        return null;
    }

}
