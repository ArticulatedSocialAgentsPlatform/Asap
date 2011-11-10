package asap.bmlb;

import java.util.HashMap;

import hmi.bml.core.BMLBehaviorAttributeExtension;
import hmi.bml.core.BehaviourBlock;
import hmi.xml.XMLTokenizer;

/**
 * Attributes added to the &ltbml&gt tag by bmlb
 * @author hvanwelbergen
 */
public class BMLBBMLBehaviorAttributes implements BMLBehaviorAttributeExtension
{

    @Override
    public void decodeAttributes(BehaviourBlock behavior, HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        
    }   
    
    
}
