/*******************************************************************************
 *******************************************************************************/
package asap.bml.ext.murml;

import hmi.xml.XMLFormatting;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import lombok.Getter;
import saiba.bml.core.Behaviour;
import asap.bml.ext.bmla.BMLAInfo;
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
    
    public MURMLBehaviour(String bmlId, String id)
    {
        super(bmlId, id);        
    }
    
    static final String MURMLNAMESPACE = "http://www.techfak.uni-bielefeld.de/ags/soa/murml";

    @Override
    public String getNamespace()
    {
        return MURMLNAMESPACE;
    }

    @Override
    public boolean hasContent()
    {
        return true;
    }
    
    @Override
    public StringBuilder appendContent(StringBuilder buf, XMLFormatting fmt)
    {
        return murmlDescription.appendXML(buf,fmt);
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
                if(specifiesParameter(BMLAInfo.BMLA_NAMESPACE+":priority"))
                {
                   murmlDescription.setPriority((int)getFloatParameterValue(BMLAInfo.BMLA_NAMESPACE+":priority")); 
                }
            }
        }
        ensureDecodeProgress(tokenizer);
    }
}
