package asap.bml.ext.maryxml;

import hmi.xml.XMLTokenizer;

import java.io.IOException;

/**
 * Mary XML behavior
 * @author welberge
 */
public class MaryXMLBehaviour extends MaryXMLBaseBehaviour
{
    public MaryXMLBehaviour(String bmlId, XMLTokenizer tokenizer) throws IOException
    {
        super(bmlId, tokenizer);        
    }    
}
