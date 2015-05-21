/*******************************************************************************
 *******************************************************************************/
package asap.blinkemitter;

import hmi.xml.XMLTokenizer;

import java.io.IOException;

import asap.emitterengine.bml.CreateEmitterBehaviour;

/**
 * Create Emitter behavior
 * @author Dennis Reidsma
 */
public class CreateBlinkEmitterBehaviour extends CreateEmitterBehaviour
{

    public CreateBlinkEmitterBehaviour() 
    {
      this("");
    }
    public CreateBlinkEmitterBehaviour(String bmlId)
    {
      super(bmlId);
      setEmitterInfo(new BlinkEmitterInfo());
    }

    public CreateBlinkEmitterBehaviour(String bmlId,XMLTokenizer tokenizer) throws IOException
    {
        this(bmlId);  
        readXML(tokenizer);
    }

    
}
