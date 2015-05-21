/*******************************************************************************
 *******************************************************************************/
package asap.livemocapengine.bml;

import hmi.xml.XMLTokenizer;

import java.util.HashMap;
import java.util.List;

import lombok.Getter;
import saiba.bml.core.Behaviour;
import saiba.bml.parser.SyncPoint;

import com.google.common.collect.ImmutableList;

/**
 * Superclass for all LiveMocapBehaviours
 * @author welberge
 *
 */
public class LiveMocapBehaviour extends Behaviour
{
    @Getter private String output;
    @Getter private String input;
    
    public LiveMocapBehaviour(String bmlId)
    {
        super(bmlId);        
    }

    
    @Override
    public StringBuilder appendAttributeString(StringBuilder buf)
    {
        appendAttribute(buf, "output", output);
        appendAttribute(buf, "input", input);        
        return super.appendAttributeString(buf);
    }

    @Override
    public String getStringParameterValue(String name)
    {
        if (name.equals("output")) return output;
        if (name.equals("input")) return input;
        return super.getStringParameterValue(name);
    }
    
    @Override
    public boolean specifiesParameter(String name)
    {
        if (name.equals("output")) return true;
        if (name.equals("input")) return true;
        return super.specifiesParameter(name);
    }
    
    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        output = getRequiredAttribute("output", attrMap, tokenizer);
        input = getRequiredAttribute("input", attrMap, tokenizer);        
        super.decodeAttributes(attrMap, tokenizer);
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
}
