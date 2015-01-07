/*******************************************************************************
 *******************************************************************************/
package asap.bml.ext.murml;

import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.List;

import saiba.bml.core.GestureBehaviour;
import saiba.bml.parser.SyncPoint;

/**
 * Extension for MURML gesture behaviours
 * @author hvanwelbergen
 */
public class MURMLGestureBehaviour extends MURMLBehaviour
{
    public MURMLGestureBehaviour(String bmlId)
    {
        super(bmlId);
    }

    public MURMLGestureBehaviour(String bmlId, XMLTokenizer tokenizer) throws IOException
    {
        super(bmlId);
        readXML(tokenizer);
    }

    public MURMLGestureBehaviour(String bmlId, String id, XMLTokenizer tokenizer) throws IOException
    {
        super(bmlId, id);
        readXML(tokenizer);
    }

    private static final String XMLTAG = "murmlgesture";

    public static String xmlTag()
    {
        return XMLTAG;
    }

    @Override
    public String getXMLTag()
    {
        return XMLTAG;
    }

    private static final List<String> DEFAULT_SYNCS = GestureBehaviour.getDefaultSyncPoints();

    public static List<String> getDefaultSyncPoints()
    {
        return DEFAULT_SYNCS;
    }

    @Override
    public void addDefaultSyncPoints()
    {
        for (String s : getDefaultSyncPoints())
        {
            addSyncPoint(new SyncPoint(bmlId, id, s));
        }
    }
}
