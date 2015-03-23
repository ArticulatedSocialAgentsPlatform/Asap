/*******************************************************************************
 *******************************************************************************/
package asap.breathingemitter;

import hmi.xml.XMLTokenizer;

import java.io.IOException;

import asap.emitterengine.bml.CreateEmitterBehaviour;

/**
 * Create Emitter behavior
 * @author Dennis Reidsma
 */
public class CreateBreathingEmitterBehaviour extends CreateEmitterBehaviour
{

    public CreateBreathingEmitterBehaviour() 
    {
      this("");
    }
    public CreateBreathingEmitterBehaviour(String bmlId)
    {
      super(bmlId);
      setEmitterInfo(new BreathingEmitterInfo());
    }

    public CreateBreathingEmitterBehaviour(String bmlId,XMLTokenizer tokenizer) throws IOException
    {
        this(bmlId);  
        readXML(tokenizer);
    }

    
}
