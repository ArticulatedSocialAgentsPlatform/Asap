/*******************************************************************************
 *******************************************************************************/
package asap.environment;

import static org.junit.Assert.assertNotNull;
import hmi.xml.XMLFormatting;
import hmi.xml.XMLTokenizer;

import java.util.HashMap;
import java.util.Set;

import org.junit.Test;

import saiba.bml.core.BMLBehaviorAttributeExtension;
import saiba.bml.core.BMLBlockComposition;
import saiba.bml.core.BehaviourBlock;
import asap.realizerembodiments.impl.BMLParserAssembler;

import com.google.common.collect.ImmutableSet;

/**
 * Unit test cases for BMLParserAssembler
 * @author hvanwelbergen
 * 
 */
public class BMLParserAssemblerTest
{
    /**
     * Stub for BMLBehaviorAttributeExtension
     */
    public static class StubAttributeExtension1 implements BMLBehaviorAttributeExtension
    {
        @Override
        public void decodeAttributes(BehaviourBlock behavior, HashMap<String, String> attrMap, XMLTokenizer tokenizer)
        {

        }

        @Override
        public BMLBlockComposition handleComposition(String sm)
        {
            return null;
        }

        @Override
        public Set<String> getOtherBlockDependencies()
        {
            return ImmutableSet.of();
        }

        @Override
        public StringBuilder appendAttributeString(StringBuilder buf, XMLFormatting fmt)
        {
            return buf;
        }
    }

    /**
     * Stub for BMLBehaviorAttributeExtension
     */
    public static class StubAttributeExtension2 implements BMLBehaviorAttributeExtension
    {
        @Override
        public void decodeAttributes(BehaviourBlock behavior, HashMap<String, String> attrMap, XMLTokenizer tokenizer)
        {

        }

        @Override
        public BMLBlockComposition handleComposition(String sm)
        {
            return null;
        }

        @Override
        public Set<String> getOtherBlockDependencies()
        {
            return ImmutableSet.of();
        }

        @Override
        public StringBuilder appendAttributeString(StringBuilder buf, XMLFormatting fmt)
        {
            return buf;
        }
    }

    @Test
    public void testEmpty()
    {
        BMLParserAssembler assembler = new BMLParserAssembler();
        String str = "<BMLParser></BMLParser>";
        assembler.readXML(str);
        assertNotNull(assembler.getBMLParser());
    }

    @Test
    public void testBMLBehaviorAttributeExtension() throws InstantiationException, IllegalAccessException
    {
        BMLParserAssembler assembler = new BMLParserAssembler();
        String str = "<BMLParser>" + "<BMLAttributeExtension class=\"asap.environment.BMLParserAssemblerTest$StubAttributeExtension1\"/>"
                + "</BMLParser>";
        assembler.readXML(str);
        assertNotNull(assembler.getBMLParser());
        BehaviourBlock bb = assembler.getBMLParser().createBehaviourBlock();
        assertNotNull(bb.getBMLBehaviorAttributeExtension(StubAttributeExtension1.class));
    }

    @Test
    public void testMultipleBMLBehaviorAttributeExtensions() throws InstantiationException, IllegalAccessException
    {
        BMLParserAssembler assembler = new BMLParserAssembler();
        String str = "<BMLParser>" + "<BMLAttributeExtension class=\"asap.environment.BMLParserAssemblerTest$StubAttributeExtension1\"/>"
                + "<BMLAttributeExtension class=\"asap.environment.BMLParserAssemblerTest$StubAttributeExtension2\"/>" + "</BMLParser>";
        assembler.readXML(str);
        assertNotNull(assembler.getBMLParser());
        BehaviourBlock bb = assembler.getBMLParser().createBehaviourBlock();
        assertNotNull(bb.getBMLBehaviorAttributeExtension(StubAttributeExtension1.class));
        assertNotNull(bb.getBMLBehaviorAttributeExtension(StubAttributeExtension2.class));
    }
}
