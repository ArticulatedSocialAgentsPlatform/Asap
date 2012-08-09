package asap.hns;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import lombok.extern.slf4j.Slf4j;

import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

/**
 * HNS file parser
 * @author hvanwelbergen
 */
@Slf4j
public class Hns extends XMLStructureAdapter
{
    private Map<String, String> settings = new HashMap<>(); // name->value map
    private Map<String, Map<String, Double>> symbols = new HashMap<>(); // className->(name->value)

    private static final String XMLTAG = "hns";
    private static final String HAND_REFERENCES = "handReferences";
    private static final String HAND_LOCATORS = "handLocators";
    private static final String HAND_DISTANCES = "handDistances";

    /**
     * Translate hand location symbol in figure root coords
     * @return false, if a syntax error occurred, true otherwise.
     */
    public boolean getHandLocation(String value, float[] location)
    {
        // vector description
        if (getSymbolValue(HAND_REFERENCES, value) == null)
        {
            return HnsUtils.parseVector(value, location);
        }

        // string description
        boolean distanceRead = false;
        String distance;
        String locator = value;
        if (getSymbolValue(HAND_LOCATORS, locator) != null)
        {
            if (getSymbolValue(HAND_DISTANCES, locator) != null)
            {
                distanceRead = true;
                distance = locator;
            }
            else
            {
                locator = "LocCCenter";
            }
        }

        if (!distanceRead)
        {
            distance = value;
            if (getSymbolValue(HAND_DISTANCES, locator) == null)
            {
                log.warn("invalid distance symbol {}, assuming LocNorm", distance);
                distance = "LocNorm";
            }
            else
            {
                // "HNS: invalid argument string(s) for hand loc!"
                return false;
            }
        }

        double phi = 0;
        double r = 0;

        // location referent defines z-coordinate of xy-plane and
        // optionally origin of cylindrical coords within this plane
        if (getSymbolValue(HAND_REFERENCES, value) != null)
        {
            location[2] = getSymbolValue(HAND_REFERENCES, value).floatValue();
        }
        if (getSymbolValue(HAND_LOCATORS, locator) != null)
        {
            phi = getSymbolValue(HAND_LOCATORS, locator);
        }
        if (getSymbolValue(HAND_DISTANCES, locator) != null)
        {
            r = getEllipticDistance(phi, getSymbolValue(HAND_DISTANCES, locator));
        }
        location[0] = (float) (r * Math.cos(Math.toRadians(phi)));
        location[1] = (float) (r * Math.sin(Math.toRadians(phi)));
        return true;
    }

    public Double getSymbolValue(String className, String name)
    {
        Map<String, Double> map = symbols.get(className);
        if (map == null) return null;
        return map.get(name);
    }

    private double getEllipticDistance(double phi, double r)
    {
        // Variablen fuer den Koerper von Max. Gleichbedeutend mit Achsenhaelfte a bei Ellipse
        // Offset von 140, da a = 220 (Haelfte von der Breite cia 500), b = 80 => 170 ist Breite vom Nullpunkt
        double a = r + getSymbolValue("offset", "ellipticDistance");
        double dist;
        double epsilon = (Math.sqrt(a * a - r * r)) / a;
        if (epsilon < 1)
        // Berechnung des Vektors r, der neuen Distanz
        dist = r / Math.sqrt(1 - epsilon * Math.cos(Math.toDegrees(phi - 90)) * Math.cos(Math.toDegrees(phi - 90)));
        else
        // Falls >= 1 nehme kleinen Wert
        dist = r / Math.sqrt(1 - 0.999999 * Math.cos(Math.toDegrees(phi - 90)) * Math.cos(Math.toDegrees(phi - 90)));
        return dist;
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
                for (Entry<String, Map<String, Double>> entry : sym.getSymbols().entrySet())
                {
                    Map<String, Double> map = symbols.get(entry.getKey());
                    if (map == null)
                    {
                        map = new HashMap<>();
                        symbols.put(entry.getKey(), map);
                    }
                    for (Entry<String, Double> entry2 : entry.getValue().entrySet())
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
