/*******************************************************************************
 *******************************************************************************/
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
    
    public MaryXMLBehaviour(String bmlId, String id, XMLTokenizer tokenizer) throws IOException
    {
        super(bmlId, id, tokenizer);        
    }  
}
