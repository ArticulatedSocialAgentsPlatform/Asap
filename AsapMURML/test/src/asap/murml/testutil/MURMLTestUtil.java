/*******************************************************************************
 *******************************************************************************/
package asap.murml.testutil;

import static org.junit.Assert.fail;

import java.util.List;

import asap.murml.Dynamic;
import asap.murml.JointValue;
import asap.murml.Slot;
import asap.murml.Static;

/**
 * Test Utilities for AsapMURML
 * @author hvanwelbergen
 *
 */
public final class MURMLTestUtil 
{
    private MURMLTestUtil(){}
    public static JointValue createJointValue(String id, float ... dofs)
    {
        return new JointValue(id,dofs);
    }
    
    public static Dynamic getDynamic(Slot slot, List<Dynamic> dynamics)
    {
        for(Dynamic dyn:dynamics)
        {
            if(dyn.getSlot().equals(slot))return dyn;
        }
        fail("dynamic with slot "+slot +"not found in "+dynamics);
        return null;
    }
    
    
    public static Static getStatic(Slot slot, List<Static> statics)
    {
        for(Static stat:statics)
        {
            if(stat.getSlot().equals(slot))return stat;
        }
        fail("static with slot "+slot +"not found in "+statics);
        return null;
    }
}
