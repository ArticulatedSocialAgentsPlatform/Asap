package asap.hns;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

/**
 * HNS file parser
 * @author hvanwelbergen
 */
public class Hns extends XMLStructureAdapter
{
    private Map<String, String> settings = new HashMap<>(); // name->value map

    private static final String XMLTAG = "hns";

    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        while (tokenizer.atSTag())
        {
            switch (tokenizer.getTagName())
            {
            case Settings.XMLTAG:
                Settings set = new Settings();
                set.readXML(tokenizer);
                settings.putAll(set.getSettings());
                break;
            case Symbols.XMLTAG:
                Symbols sym = new Symbols();
                sym.readXML(tokenizer);
                // TODO: actually do something with the symbols
                break;
            case SymbolMatrices.XMLTAG:
                SymbolMatrices symMat = new SymbolMatrices();
                symMat.readXML(tokenizer);
                // TODO: actually do something with the symbolMatrices
                break;
            default:
                throw new XMLScanException("Invalid tag " + tokenizer.getTagName() + " in <hns>");
            }
        }
    }

    public static String xmlTag()
    {
        return XMLTAG;
    }

    @Override
    public String getXMLTag()
    {
        return XMLTAG;
    }
}
