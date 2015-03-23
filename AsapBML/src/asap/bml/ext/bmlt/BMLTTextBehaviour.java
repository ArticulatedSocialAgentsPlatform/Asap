/*******************************************************************************
 *******************************************************************************/
package asap.bml.ext.bmlt;

import hmi.xml.XMLFormatting;
import hmi.xml.XMLScanException;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;

import saiba.bml.core.SpeechBehaviour;
import saiba.bml.core.Sync;
import saiba.bml.parser.InvalidSyncRefException;
import saiba.bml.parser.SyncPoint;

/**
 * Text behavior with the same syntax as the core speech behavior, to be used when speech and text are required
 * simultaneously. 
 * @author hvanwelbergen
 */
public class BMLTTextBehaviour extends SpeechBehaviour
{
    public BMLTTextBehaviour(String bmlId,XMLTokenizer tokenizer) throws IOException
    {
        super(bmlId, tokenizer);        
    }
    
    @Override
    public StringBuilder appendContent(StringBuilder buf, XMLFormatting fmt)
    {
        if (content != null) buf.append(content);
        String tmp = content;
        content = null; //hack hack: skip content handling in SpeechBehaviour
        StringBuilder sb = super.appendContent(buf, fmt);
        content = tmp;
        return sb;
    }
    
    
    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        content = "";
        ArrayList<Sync> syncs = new ArrayList<Sync>();
        while (!tokenizer.atETag())
        {
            if (tokenizer.atSTag("sync"))
            {
                Sync s = new Sync(bmlId);
                s.readXML(tokenizer);
                content = content + s.toString();
                syncs.add(s);
            }
            else if (tokenizer.atCDSect())
            {
                content = content + tokenizer.takeCDSect();
            }
            else if (tokenizer.atCharData())
            {
                content = content + tokenizer.takeCharData();
            }

            ensureDecodeProgress(tokenizer);
        }
        
        for(Sync sync:syncs)
        {
            SyncPoint s = new SyncPoint(bmlId, id, sync.id);        
            if(sync.ref!=null)
            {
                try
                {
                    s.setRefString(sync.ref.toString(bmlId));
                }
                catch (InvalidSyncRefException e)
                {
                    throw new XMLScanException("",e);
                }
            }
            addSyncPoint(s);
        }
    }
    
    @Override
    public boolean hasContent()
    {
        return true;
    }
    
    public static final String BMLTNAMESPACE = "http://hmi.ewi.utwente.nl/bmlt";

    @Override
    public String getNamespace()
    {
        return BMLTNAMESPACE;
    }
    
    private static final String XMLTAG = "text";

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
}
