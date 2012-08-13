package asap.bml.ext.murml;

import hmi.xml.XMLTokenizer;

import java.io.IOException;

import lombok.Getter;
import saiba.bml.core.Behaviour;
import asap.murml.MURMLDescription;

/**
 * Generic class for MURML behaviours
 * @author hvanwelbergen
 *
 */
public abstract class MURMLBehaviour extends Behaviour
{
    @Getter
    private MURMLDescription murmlDescription;
    
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
            if (tag.equals(MURMLDescription.xmlTag()))
            {
                murmlDescription = new MURMLDescription();
                murmlDescription.readXML(tokenizer);                
            }
        }
        ensureDecodeProgress(tokenizer);
    }
}
