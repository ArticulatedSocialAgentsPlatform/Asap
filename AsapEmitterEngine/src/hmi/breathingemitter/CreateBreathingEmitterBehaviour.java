package hmi.breathingemitter;

import hmi.emitterengine.bml.*;
import java.io.IOException;
import hmi.xml.XMLTokenizer;

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
