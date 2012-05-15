package asap.bml.ext.bmlt;

import saiba.bml.core.Behaviour;
import saiba.bml.parser.SyncPoint;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * BMLT Interrupt behavior. Specifies the activation of a target preplanned BML block
 * @author welberge
 */
public class BMLTActivateBehaviour extends Behaviour
{
    private String target;
    
    public BMLTActivateBehaviour(String bmlId,XMLTokenizer tokenizer) throws IOException
    {
        super(bmlId);
        readXML(tokenizer);
    }
    
    @Override
    public String getNamespace()
    {
        return BMLTBehaviour.BMLTNAMESPACE;
    }

    public String getTarget()
    {
        return target;
    }
    
    @Override
    public void addDefaultSyncPoints()
    {
        for(String s:getDefaultSyncPoints())
        {
            addSyncPoint(new SyncPoint(bmlId, id, s));
        }        
    }
    
    
    /*
     * The XML Stag for XML encoding
     */
    private static final String XMLTAG = "activate";

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
    public float getFloatParameterValue(String name)
    {
        // TODO Throw exception?
        return 0;
    }

    @Override
    public String getStringParameterValue(String name)
    {
        if (name.equals("target")) return target;
        return null;
    }

    @Override
    public boolean specifiesParameter(String name)
    {
        if (name.equals("target")) return true;
        return false;
    }
    
    @Override
    public StringBuilder appendAttributeString(StringBuilder buf)
    {
        appendAttribute(buf, "target", target);
        return super.appendAttributeString(buf);
    }

    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        target = getRequiredAttribute("target", attrMap, tokenizer);
        super.decodeAttributes(attrMap, tokenizer);
    }
    
    private static final List<String> DEFAULT_SYNCS = ImmutableList.of("start","end");
    public static List<String> getDefaultSyncPoints()
    {
        return DEFAULT_SYNCS;
    }
}
