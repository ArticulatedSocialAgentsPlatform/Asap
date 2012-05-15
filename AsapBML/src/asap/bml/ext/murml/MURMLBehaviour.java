package asap.bml.ext.murml;

import java.io.IOException;

import lombok.Getter;

import asap.murml.Definition;
import saiba.bml.core.Behaviour;
import hmi.xml.XMLTokenizer;

/**
 * Generic class for MURML behaviours
 * @author hvanwelbergen
 *
 */
public abstract class MURMLBehaviour extends Behaviour
{
    @Getter
    private Definition murmlDefinition;
    
    public MURMLBehaviour(String bmlId)
    {
        super(bmlId);        
    }
    
    static final String MURMLNAMESPACE = "http://www.techfak.uni-bielefeld.de/ags/soa/murml";

    @Override
    public String getNamespace()
    {
        return MURMLNAMESPACE;
    }

    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        if(tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            if (tag.equals(Definition.xmlTag()))
            {
                murmlDefinition = new Definition();
                murmlDefinition.readXML(tokenizer);                
            }
        }
        ensureDecodeProgress(tokenizer);
    }
}
