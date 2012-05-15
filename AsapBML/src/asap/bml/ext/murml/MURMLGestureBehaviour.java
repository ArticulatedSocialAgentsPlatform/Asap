package asap.bml.ext.murml;

import saiba.bml.core.GestureBehaviour;
import saiba.bml.parser.SyncPoint;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.List;

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

    
    
    
    /*
     * The XML Stag for XML encoding
     */
    private static final String XMLTAG = "murmlgesture";

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
    public float getFloatParameterValue(String arg0)
    {
        // TODO Throw exception?
        return 0;
    }

    @Override
    public String getStringParameterValue(String arg0)
    {
        return null;
    }

    @Override
    public boolean specifiesParameter(String arg0)
    {
        return false;
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
