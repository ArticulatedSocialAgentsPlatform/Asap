/*******************************************************************************
 *******************************************************************************/
package asap.bml.ext.maryxml;

import hmi.xml.XMLTokenizer;

import java.io.IOException;
/**
 * MaryAllophones Behavior 
 * @author reidsma
 */
public class MaryAllophonesBehaviour extends MaryXMLBaseBehaviour
{
    public MaryAllophonesBehaviour(String bmlId, XMLTokenizer tokenizer) throws IOException
    {
        super(bmlId, tokenizer);        
    }
    
    public MaryAllophonesBehaviour(String bmlId, String id, XMLTokenizer tokenizer) throws IOException
    {
        super(bmlId, id, tokenizer);        
    }
}
