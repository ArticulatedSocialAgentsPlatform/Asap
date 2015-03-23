/*******************************************************************************
 *******************************************************************************/
package asap.realizerembodiments.impl;

import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import saiba.bml.core.BMLBehaviorAttributeExtension;
import saiba.bml.parser.BMLParser;

import com.google.common.collect.ImmutableSet;

/**
 * Constructs a BML parser from an XML description 
 * @author hvanwelbergen
 */
public class BMLParserAssembler extends XMLStructureAdapter
{
    private BMLParser parser;
    public BMLParser getBMLParser()
    {
        return parser;
    }
    
    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        Set<Class<? extends BMLBehaviorAttributeExtension>>attributeExtensions 
        = new HashSet<Class<? extends BMLBehaviorAttributeExtension>>();
        while (!tokenizer.atETag())
        {
            if (tokenizer.atSTag("BMLAttributeExtension"))
            {
                BMLAttributeExtensionAssembler asm = new BMLAttributeExtensionAssembler();
                asm.readXML(tokenizer);
                attributeExtensions.add(asm.getAttributeExtension());
            }
        }
        parser = new BMLParser(ImmutableSet.copyOf(attributeExtensions));
    }

    
    private static final String XMLTAG = "BMLParser";    
    /**
     * The XML Stag for XML encoding -- use this static method when you want to see if a given String equals
     * the xml tag for this class
     */
    public static String xmlTag()
    {
        return XMLTAG;
    }

    /**
     * The XML Stag for XML encoding -- use this method to find out the run-time xml tag of an object
     */
    @Override
    public String getXMLTag()
    {
        return XMLTAG;
    }
}
