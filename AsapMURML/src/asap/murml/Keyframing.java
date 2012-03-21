package asap.murml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lombok.Getter;

import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

/**
 * Parses a MURML keyframing element
 * @author hvanwelbergen
 */
public class Keyframing extends XMLStructureAdapter
{
    enum Mode
    {
        SPLINE, LINEAR, QUATERNION, RAW;
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
    @Getter private List<Phase> phases = new ArrayList<Phase>();
    
    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        mode = Mode.valueOf(getOptionalAttribute("mode", attrMap,"spline").toUpperCase());
        priority = getOptionalIntAttribute("priority", attrMap, 0);
        easescale = getOptionalIntAttribute("easescale", attrMap, 1);
        applyMode = ApplyMode.valueOf(getOptionalAttribute("applymode", attrMap,"exclusive").toUpperCase());
        name = getOptionalAttribute("name", attrMap,"KF_Anim_");
        easeturningpoint = getOptionalDoubleAttribute("easeturningpoint", attrMap, 0.5);
        startTime = getOptionalDoubleAttribute("startTime", attrMap, 0);
        endTime = getOptionalDoubleAttribute("endTime", attrMap, 0);
        postponeStartframe = getOptionalDoubleAttribute("postpone_startframe", attrMap, 0);
        insertStartframe = !getOptionalBooleanAttribute("dont_insert_startframe", attrMap, false);
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
        }
    }
        
    /*
     * The XML Stag for XML encoding
     */
    private static final String XMLTAG = "keyframing";

    /**
     * The XML Stag for XML encoding -- use this static method when you want to
     * see if a given String equals the xml tag for this class
     */
    public static String xmlTag()
    {
        return XMLTAG;
    }

    /**
     * The XML Stag for XML encoding -- use this method to find out the run-time
     * xml tag of an object
     */
    @Override
    public String getXMLTag()
    {
        return XMLTAG;
    }
}
