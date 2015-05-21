/*******************************************************************************
 *******************************************************************************/
package asap.realizer.planunit;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import saiba.bml.BMLGestureSync;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;

/**
 * KeyPositionManager test cases
 * @author Herwin
 */
public class KeyPositionManagerTest
{
    private KeyPositionManager kpm;
    private static final double TIME_PRECISION = 0.0001;
    
    @Before
    public void setup()
    {
        kpm = new KeyPositionManagerImpl();        
    }
    
    @Test
    public void testGetPrevPegTime()
    {
        kpm.addKeyPosition(new KeyPosition("start",0,1));
        kpm.addKeyPosition(new KeyPosition("end",1,1));
        KeyPosition kpStroke = new KeyPosition("stroke",0.5,1);
        kpm.addKeyPosition(kpStroke);
        KeyPosition kpCustom2 = new KeyPosition("custom2",0.55,1);            
        kpm.addKeyPosition(kpCustom2);
        KeyPosition kpCustom3 = new KeyPosition("custom3",0.45,1);            
        kpm.addKeyPosition(kpCustom3);
        KeyPosition kpCustom = new KeyPosition("custom",0.6,1);            
        kpm.addKeyPosition(kpCustom);
        
        BMLBlockPeg gPeg = new BMLBlockPeg("bml1",0.3);
        TimePeg tpStroke = new TimePeg(gPeg);
        tpStroke.setGlobalValue(1);
        TimePeg tpCustom = new TimePeg(gPeg);
        tpCustom.setGlobalValue(1.2);        
                
        PlanUnitTimeManager put = new PlanUnitTimeManager(kpm);
        put.setTimePeg(kpStroke, tpStroke);
        put.setTimePeg(kpCustom, tpCustom);
        double t = put.getPrevPegTime("custom");
        assertEquals(1.0,t,TIME_PRECISION);
        
        t = put.getPrevPegTime(BMLGestureSync.STROKE_END.getId());
        assertEquals(1.0,t,TIME_PRECISION);
        
        t = put.getPrevPegTime("end");
        assertEquals(1.2,t,TIME_PRECISION);
                
        TimePeg tpCustom2 = new TimePeg(gPeg);
        tpCustom2.setGlobalValue(1.1);
        put.setTimePeg(kpCustom2, tpCustom2);
        t = put.getPrevPegTime("custom");
        assertEquals(1.1,t,TIME_PRECISION);
        
        tpStroke.setGlobalValue(TimePeg.VALUE_UNKNOWN);
        TimePeg tpCustom3 = new TimePeg(gPeg);
        tpCustom3.setGlobalValue(0.9);
        put.setTimePeg(kpCustom3, tpCustom3);
        t = put.getPrevPegTime("strokeEnd");
        assertEquals(0.9,t,TIME_PRECISION);
        
        t = put.getPrevPegTime("unknown");
        assertEquals(TimePeg.VALUE_UNKNOWN,t,TIME_PRECISION);
    }
    
    @Test
    public void testGetNextPegTime()
    {
        kpm.addKeyPosition(new KeyPosition("start",0,1));
        kpm.addKeyPosition(new KeyPosition("end",1,1));
        KeyPosition kpStroke = new KeyPosition("stroke",0.5,1);
        kpm.addKeyPosition(kpStroke);
        KeyPosition kpCustom2 = new KeyPosition("custom2",0.55,1);            
        kpm.addKeyPosition(kpCustom2);
        KeyPosition kpCustom = new KeyPosition("custom",0.6,1);            
        kpm.addKeyPosition(kpCustom);
        
        BMLBlockPeg gPeg = new BMLBlockPeg("bml1",0.3);
        TimePeg tpStroke = new TimePeg(gPeg);
        tpStroke.setGlobalValue(1);
        TimePeg tpCustom = new TimePeg(gPeg);
        tpCustom.setGlobalValue(1.2);        
        
        PlanUnitTimeManager put = new PlanUnitTimeManager(kpm);
        put.setTimePeg(kpStroke, tpStroke);
        put.setTimePeg(kpCustom, tpCustom);
        double t = put.getNextPegTime("stroke");
        assertEquals(1.2,t,TIME_PRECISION);
        
        t = put.getNextPegTime("strokeStart");
        assertEquals(1.0,t,TIME_PRECISION);
        
        tpStroke.setGlobalValue(TimePeg.VALUE_UNKNOWN);
        t = put.getNextPegTime("strokeStart");
        assertEquals(1.2,t,TIME_PRECISION);
        
        TimePeg tpCustom2 = new TimePeg(gPeg);
        tpCustom2.setGlobalValue(1.1);
        put.setTimePeg(kpCustom2, tpCustom2);
        t = put.getNextPegTime("custom2");
        assertEquals(1.2,t,TIME_PRECISION);
        
        t = put.getNextPegTime("unknown");
        assertEquals(TimePeg.VALUE_UNKNOWN,t,TIME_PRECISION);
    }
    
    
}
