/*******************************************************************************
 *******************************************************************************/
package asap.bml.ext.bmla;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import hmi.xml.XMLFormatting;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.hamcrest.Matchers;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Test;

import saiba.bml.core.AbstractBehaviourTest;
import saiba.bml.core.Behaviour;
import saiba.utils.TestUtil;

/**
 * Unit test cases for interruptbehaviour parsing
 * @author welberge
 */
public class BMLAInterruptBehaviourTest extends AbstractBehaviourTest
{
    @Override
    protected Behaviour createBehaviour(String bmlId, String extraAttributeString) throws IOException
    {
        String str = "<bmla:interrupt xmlns:bmla=\""+BMLAInfo.BMLA_NAMESPACE+"\" " + TestUtil.getDefNS()
                + "id=\"interrupt0\" target=\"bmltarget\"" + extraAttributeString + "/>";
        return new BMLAInterruptBehaviour(bmlId, new XMLTokenizer(str));
    }

    @Override
    protected Behaviour parseBehaviour(String bmlId, String bmlString) throws IOException
    {
        return new BMLAInterruptBehaviour(bmlId, new XMLTokenizer(bmlString));
    }

    @Test
    public void testInterrupt() throws IOException
    {
        String interruptBML = "<bmla:interrupt xmlns:bmla=\""+BMLAInfo.BMLA_NAMESPACE+"\" id=\"interrupt0\" target=\"bml0\"/>";
        BMLAInterruptBehaviour bmi = new BMLAInterruptBehaviour("bml1", new XMLTokenizer(interruptBML));
        assertEquals("bml0", bmi.getTarget());
        assertEquals("bml1", bmi.getBmlId());
        assertEquals("interrupt0", bmi.id);
        assertThat(bmi.getExclude(), Matchers.<String> empty());
        assertThat(bmi.getInclude(), Matchers.<String> empty());
    }

    @Test
    public void testInterruptInclude() throws IOException
    {
        String interruptBML = "<bmla:interrupt xmlns:bmla=\""+BMLAInfo.BMLA_NAMESPACE+"\" id=\"interrupt0\" target=\"bml0\" "
                + "include=\"beh1,beh2\"/>";
        BMLAInterruptBehaviour bmi = new BMLAInterruptBehaviour("bml1", new XMLTokenizer(interruptBML));
        assertThat(bmi.getInclude(), IsIterableContainingInAnyOrder.containsInAnyOrder("beh1", "beh2"));
        assertThat(bmi.getExclude(), Matchers.<String> empty());
    }

    @Test
    public void testInterruptExclude() throws IOException
    {
        String interruptBML = "<bmla:interrupt xmlns:bmla=\""+BMLAInfo.BMLA_NAMESPACE+"\" id=\"interrupt0\" target=\"bml0\" "
                + "exclude=\"beh1,beh2\"/>";
        BMLAInterruptBehaviour bmi = new BMLAInterruptBehaviour("bml1", new XMLTokenizer(interruptBML));
        assertThat(bmi.getExclude(), IsIterableContainingInAnyOrder.containsInAnyOrder("beh1", "beh2"));
        assertThat(bmi.getInclude(), Matchers.<String> empty());
    }

    @Test
    public void testInterruptExcludeAndInclude() throws IOException
    {
        String interruptBML = "<bmla:interrupt xmlns:bmla=\""+BMLAInfo.BMLA_NAMESPACE+"\" id=\"interrupt0\" target=\"bml0\" "
                + "exclude=\"beh1,beh2\" include=\"beh3\"/>";
        BMLAInterruptBehaviour bmi = new BMLAInterruptBehaviour("bml1", new XMLTokenizer(interruptBML));
        assertThat(bmi.getExclude(), IsIterableContainingInAnyOrder.containsInAnyOrder("beh1", "beh2"));
        assertThat(bmi.getInclude(), IsIterableContainingInAnyOrder.containsInAnyOrder("beh3"));
    }

    @Test
    public void testWriteXML() throws IOException
    {
        String interruptBML = "<bmla:interrupt xmlns:bmla=\""+BMLAInfo.BMLA_NAMESPACE+"\" id=\"interrupt0\" target=\"bml0\" "
                + "exclude=\"beh1,beh2\" include=\"beh3\"/>";
        BMLAInterruptBehaviour bmIn = new BMLAInterruptBehaviour("bml1", new XMLTokenizer(interruptBML));
        StringBuilder buf = new StringBuilder();
        bmIn.appendXML(buf, new XMLFormatting(), "bmla", BMLAInfo.BMLA_NAMESPACE);
        BMLAInterruptBehaviour behOut = new BMLAInterruptBehaviour("bml1", new XMLTokenizer(buf.toString()));

        assertEquals("bml0", behOut.getTarget());
        assertEquals("bml1", behOut.getBmlId());
        assertEquals("interrupt0", behOut.id);
        assertThat(behOut.getExclude(), IsIterableContainingInAnyOrder.containsInAnyOrder("beh1", "beh2"));
        assertThat(behOut.getInclude(), IsIterableContainingInAnyOrder.containsInAnyOrder("beh3"));
    }

}
