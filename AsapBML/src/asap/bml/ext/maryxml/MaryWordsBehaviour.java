/*******************************************************************************
 *******************************************************************************/
package asap.bml.ext.maryxml;

import hmi.xml.XMLTokenizer;

import java.io.IOException;
/**
 * MaryTTS words behavior
 * @author reidsma
 */
public class MaryWordsBehaviour extends MaryXMLBaseBehaviour
{
    public MaryWordsBehaviour(String bmlId, XMLTokenizer tokenizer) throws IOException
    {
        super(bmlId, tokenizer);        
    }    
    
    public MaryWordsBehaviour(String bmlId, String id, XMLTokenizer tokenizer) throws IOException
    {
        super(bmlId, id, tokenizer);        
    } 
}
