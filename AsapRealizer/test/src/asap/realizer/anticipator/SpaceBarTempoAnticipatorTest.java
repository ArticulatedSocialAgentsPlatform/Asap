package asap.realizer.anticipator;

import static org.junit.Assert.*;

import java.awt.event.KeyEvent;

import hmi.util.SystemClock;

import org.junit.Before;
import org.junit.Test;

import asap.realizer.anticipator.SpaceBarTempoAnticipator;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;
import static org.mockito.Mockito.*;
/**
 * Unit test cases for the SpaceBarTempoAnticipator
 * @author hvanwelbergen
 *
 */
public class SpaceBarTempoAnticipatorTest
{
    private KeyEvent mockKeyEvent = mock(KeyEvent.class);
    private SpaceBarTempoAnticipator sbta = new SpaceBarTempoAnticipator();
    private StubSystemClock stubSystemClock = new StubSystemClock();
    private TimePeg managingPegs[];    
    
    private static class StubSystemClock extends SystemClock
    {
        private double time = 0;
        
        public void setTime(double t)
        {
            time = t;
        }
        
        @Override
        public double getMediaSeconds()
        {
            return time;
        }
    }
    
    @Before
    public void setup()
    {
        BMLBlockPeg bbPeg = new BMLBlockPeg("bml1",0.3);
        
        managingPegs = new TimePeg[100];
        for(int i=0;i<100;i++)
        {
            managingPegs[i] = new TimePeg(bbPeg);
            managingPegs[i].setGlobalValue(i);
            sbta.addSynchronisationPoint("s"+i, managingPegs[i]);
        }
        sbta.setPhysicsClock(stubSystemClock);
        when(mockKeyEvent.getKeyCode()).thenReturn(KeyEvent.VK_SPACE);
    }
    
    @Test
    public void test()
    {
        //0 1 2 3 4 5 6 7 8 9 10 11 12 13 before
        
        //0 2 4 6 8 10 
        
        //0 1 2 4 6 8 10        
        for(int i=0;i<5;i++)
        {
            stubSystemClock.setTime(i*2.0);            
            sbta.keyPressed(mockKeyEvent);
            sbta.keyReleased(mockKeyEvent);
        }
        
        assertEquals(0,managingPegs[0].getGlobalValue(),0.0001);
        assertEquals(1,managingPegs[1].getGlobalValue(),0.0001);
        assertEquals(2,managingPegs[2].getGlobalValue(),0.0001);
        for(int i=3;i<100;i++)
        {
            assertEquals( (i-1)*2,managingPegs[i].getGlobalValue(),0.0001);
        }
        verify(mockKeyEvent,times(10)).getKeyCode();
    }
    
    @Test
    public void test2()
    {
        //0 1 2 3 4 5 6 7 8 9 10 11 12 13 before
        
        //0.1 2.1 4.1 6.1 8.1 10.1 
        
        //0 1 2.1 3 4.1 6.1 8.1 10.1        
        for(int i=0;i<5;i++)
        {
            stubSystemClock.setTime(i*2.0+0.1);            
            sbta.keyPressed(mockKeyEvent);
            sbta.keyReleased(mockKeyEvent);
        }
        
        assertEquals(0,managingPegs[0].getGlobalValue(),0.0001);
        assertEquals(1,managingPegs[1].getGlobalValue(),0.0001);
        assertEquals(2.1,managingPegs[2].getGlobalValue(),0.0001);
        for(int i=3;i<100;i++)
        {
            assertEquals( (i-1)*2+0.1,managingPegs[i].getGlobalValue(),0.0001);
        }
        verify(mockKeyEvent,times(10)).getKeyCode();
    }
    
    @Test
    public void test3()
    {
        //0 1 2 3 4 5 6 7 8 9 10 11 12 13 before
        
        //0.1 0.6 1.1 1.6 2.1 2.6 
        
        //0 1 1.6 2.1 2.6
        for(int i=0;i<5;i++)
        {
            stubSystemClock.setTime(i*0.5+0.1);            
            sbta.keyPressed(mockKeyEvent);
            sbta.keyReleased(mockKeyEvent);
        }
        
        assertEquals(0,managingPegs[0].getGlobalValue(),0.0001);
        assertEquals(1,managingPegs[1].getGlobalValue(),0.0001);
        assertEquals(1.6,managingPegs[2].getGlobalValue(),0.0001);
        for(int i=3;i<100;i++)
        {
            assertEquals( (i-2)*0.5+1.6,managingPegs[i].getGlobalValue(),0.0001);
        }
        verify(mockKeyEvent,times(10)).getKeyCode();
    }
}
