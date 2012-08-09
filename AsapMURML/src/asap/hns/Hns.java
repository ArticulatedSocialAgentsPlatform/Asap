package asap.hns;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
    private Map<String, Map<String, String>> symbols = new HashMap<>(); // className->(name->value)

    private static final String XMLTAG = "hns";
    private static final String HAND_REFERENCES = "handReferences";
    
    
    public boolean getHandLocation(String value, float[] location)
    {
        if(getSymbolValue(HAND_REFERENCES,value)==null)
        {
            return HnsUtils.parseVector(value, location);
        }
        return false;
    }

    public String getSymbolValue(String className, String name)
    {
        Map<String, String> map = symbols.get(className);
        if (map == null) return null;
        return map.get(name);
    }

    
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
                for(Entry<String, Map<String, String>> entry: sym.getSymbols().entrySet())
                {
                    Map<String, String> map = symbols.get(entry.getKey());
                    if(map==null)
                    {
                        map = new HashMap<>();
                        symbols.put(entry.getKey(), map);
                    }
                    for(Entry<String, String> entry2:entry.getValue().entrySet())
                    {
                        map.put(entry2.getKey(), entry2.getValue());
                    }                    
                }                
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
