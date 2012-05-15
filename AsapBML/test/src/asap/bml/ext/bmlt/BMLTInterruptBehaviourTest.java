package asap.bml.ext.bmlt;

import static org.junit.Assert.*;

import java.io.IOException;

import hmi.xml.XMLFormatting;
import hmi.xml.XMLTokenizer;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

import org.hamcrest.Matchers;
import org.hamcrest.collection.*;

import asap.bml.ext.bmlt.BMLTInterruptBehaviour;

/**
 * Unit test cases for interruptbehaviour parsing
 * @author welberge
 */
public class BMLTInterruptBehaviourTest
{
    @Test
    public void testInterrupt() throws IOException
    {
        String interruptBML = "<bmlt:interrupt xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" id=\"interrupt0\" target=\"bml0\"/>";
        BMLTInterruptBehaviour bmi = new BMLTInterruptBehaviour("bml1", new XMLTokenizer(interruptBML));
        assertEquals("bml0", bmi.getTarget());
        assertEquals("bml1",bmi.getBmlId());
        assertEquals("interrupt0",bmi.id);
        assertThat(bmi.getExclude(), Matchers.<String> empty());
        assertThat(bmi.getInclude(), Matchers.<String> empty());
    }

    @Test
    public void testInterruptInclude() throws IOException
    {
        String interruptBML = "<bmlt:interrupt xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" id=\"interrupt0\" target=\"bml0\" "
                + "include=\"beh1,beh2\"/>";
        BMLTInterruptBehaviour bmi = new BMLTInterruptBehaviour("bml1", new XMLTokenizer(interruptBML));
        assertThat(bmi.getInclude(), IsIterableContainingInAnyOrder.containsInAnyOrder("beh1", "beh2"));
        assertThat(bmi.getExclude(), Matchers.<String> empty());
    }

    @Test
    public void testInterruptExclude() throws IOException
    {
        String interruptBML = "<bmlt:interrupt xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" id=\"interrupt0\" target=\"bml0\" "
                + "exclude=\"beh1,beh2\"/>";
        BMLTInterruptBehaviour bmi = new BMLTInterruptBehaviour("bml1", new XMLTokenizer(interruptBML));
        assertThat(bmi.getExclude(), IsIterableContainingInAnyOrder.containsInAnyOrder("beh1", "beh2"));
        assertThat(bmi.getInclude(), Matchers.<String> empty());
    }

    @Test
    public void testInterruptExcludeAndInclude() throws IOException
    {
        String interruptBML = "<bmlt:interrupt xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" id=\"interrupt0\" target=\"bml0\" "
                + "exclude=\"beh1,beh2\" include=\"beh3\"/>";
        BMLTInterruptBehaviour bmi = new BMLTInterruptBehaviour("bml1", new XMLTokenizer(interruptBML));
        assertThat(bmi.getExclude(), IsIterableContainingInAnyOrder.containsInAnyOrder("beh1", "beh2"));
        assertThat(bmi.getInclude(), IsIterableContainingInAnyOrder.containsInAnyOrder("beh3"));
    }
    
    @Test
    public void testWriteXML() throws IOException
    {
        String interruptBML = "<bmlt:interrupt xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" id=\"interrupt0\" target=\"bml0\" "
                + "exclude=\"beh1,beh2\" include=\"beh3\"/>";
        BMLTInterruptBehaviour bmIn = new BMLTInterruptBehaviour("bml1", new XMLTokenizer(interruptBML));
        StringBuilder buf = new StringBuilder();        
        bmIn.appendXML(buf, new XMLFormatting(), "bmlt", "http://hmi.ewi.utwente.nl/bmlt");
        BMLTInterruptBehaviour behOut = new BMLTInterruptBehaviour("bml1", new XMLTokenizer(buf.toString()));
        
        assertEquals("bml0", behOut.getTarget());
        assertEquals("bml1",behOut.getBmlId());
        assertEquals("interrupt0",behOut.id);
        assertThat(behOut.getExclude(), IsIterableContainingInAnyOrder.containsInAnyOrder("beh1", "beh2"));
        assertThat(behOut.getInclude(), IsIterableContainingInAnyOrder.containsInAnyOrder("beh3"));
    }
}
