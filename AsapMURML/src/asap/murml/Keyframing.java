/*******************************************************************************
 *******************************************************************************/
package asap.murml;

import hmi.xml.XMLFormatting;
import hmi.xml.XMLScanException;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lombok.Getter;

/**
 * Parses a MURML keyframing element
 * @author hvanwelbergen
 */
public class Keyframing extends MURMLElement
{
    /**
     * Interpolation modes 
     */
    public enum Mode
    {
        SPLINE, LINEAR, SQUAD, QUATERNION, RAW;
    }
    enum ApplyMode
    {
        SUPERPOSE, EXCLUSIVE;
    }
    
    @Getter private Mode mode;
    @Getter private ApplyMode applyMode;
    @Getter private int priority;    
    @Getter private double easescale;
    @Getter private String name;
    @Getter private double easeturningpoint;
    @Getter private double startTime;
    @Getter private double endTime;
    @Getter private double postponeStartframe;
    @Getter private boolean insertStartframe;
    @Getter private boolean notify;
    @Getter private String scope;
    @Getter private List<Phase> phases = new ArrayList<Phase>();
    
    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        scope = getOptionalAttribute("scope", attrMap,"");
        mode = Mode.valueOf(getOptionalAttribute("mode", attrMap,"spline").toUpperCase());
        priority = getOptionalIntAttribute("priority", attrMap, 0);
        easescale = getOptionalDoubleAttribute("easescale", attrMap, 1);
        applyMode = ApplyMode.valueOf(getOptionalAttribute("applymode", attrMap,"exclusive").toUpperCase());
        name = getOptionalAttribute("name", attrMap,"KF_Anim_");
        easeturningpoint = getOptionalDoubleAttribute("easeturningpoint", attrMap, 0.5);
        startTime = getOptionalDoubleAttribute("startTime", attrMap, 0);
        endTime = getOptionalDoubleAttribute("endTime", attrMap, 0);
        postponeStartframe = getOptionalDoubleAttribute("postpone_startframe", attrMap, 0);
        insertStartframe = !getOptionalBooleanAttribute("dont_insert_startframe", attrMap, false);
    }
    
    @Override
    public StringBuilder appendAttributes(StringBuilder buf)
    {
        if(!scope.isEmpty())
        {
            appendAttribute(buf, "scope",scope);            
        }
        appendAttribute(buf,"mode", mode.toString().toLowerCase());
        if(priority!=0)
        {
            appendAttribute(buf,"priority",priority);
        }
        if(easescale!=1)
        {
            appendAttribute(buf,"easescale",easescale);
        }
        if(applyMode!=ApplyMode.EXCLUSIVE)
        {
            appendAttribute(buf,"applymode", applyMode.toString().toLowerCase());
        }
        if(!name.equals("KF_Anim_"))
        {
            appendAttribute(buf,"name",name);
        }
        if(easeturningpoint!=0.5)
        {
            appendAttribute(buf,"easeturningpoint",easeturningpoint);
        }
        if(startTime!=0)
        {
            appendAttribute(buf,"startTime",startTime);
        }
        if(endTime!=0)
        {
            appendAttribute(buf,"endTime",endTime);
        }
        if(postponeStartframe!=0)
        {
            appendAttribute(buf,"postpone_startframe",postponeStartframe);
        }
        if(!insertStartframe)
        {
            appendAttribute(buf,"dont_insert_startframe",true);
        }
        return buf;
    }
    
    @Override
    public boolean hasContent()
    {
        return true;
    }
    
    @Override
    public StringBuilder appendContent(StringBuilder buf, XMLFormatting fmt)
    {
        appendXMLStructureList(buf, fmt, phases);
        return buf;
    }
    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        while (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            if (tag.equals(Phase.xmlTag()))
            {
                Phase ph = new Phase();
                ph.readXML(tokenizer);
                phases.add(ph);
            }
            else
            {
                throw new XMLScanException("Invalid tag "+tag+" in <keyframing>");
            }
        }
    }
        
    static final String XMLTAG = "keyframing";

    public static String xmlTag()
    {
        return XMLTAG;
    }

    @Override
    public String getXMLTag()
    {
        return XMLTAG;
    }
}
