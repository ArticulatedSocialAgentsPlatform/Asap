/*******************************************************************************
 *******************************************************************************/
package asap.bml.ext.murml;

import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.List;

import saiba.bml.parser.SyncPoint;

import com.google.common.collect.ImmutableList;

/**
 * Extension for MURML face behaviors
 * @author hvanwelbergen
 * 
 */
public class MURMLFaceBehaviour extends MURMLBehaviour
{
    public MURMLFaceBehaviour(String bmlId, XMLTokenizer tokenizer) throws IOException
    {
        super(bmlId);
        readXML(tokenizer);
    }

    public MURMLFaceBehaviour(String bmlId, String id, XMLTokenizer tokenizer) throws IOException
    {
        super(bmlId, id);
        readXML(tokenizer);
    }

    /*
     * The XML Stag for XML encoding
     */
    private static final String XMLTAG = "murmlface";

    /**
     * The XML Stag for XML encoding -- use this static method when you want to see if a given
     * String equals the xml tag for this class
     */
    public static String xmlTag()
    {
        return XMLTAG;
    }

    /**
     * The XML Stag for XML encoding -- use this method to find out the run-time xml tag of an
     * object
     */
    @Override
    public String getXMLTag()
    {
        return XMLTAG;
    }

  
    
    @Override
    public void addDefaultSyncPoints()
    {
        for (String s : getDefaultSyncPoints())
        {
            addSyncPoint(new SyncPoint(bmlId, id, s));
        }
    }

    private static final List<String> DEFAULT_SYNCS = ImmutableList.of("start", "end");

    public static List<String> getDefaultSyncPoints()
    {
        return DEFAULT_SYNCS;
    }
}
