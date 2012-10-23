package asap.murml;

/**
 * Parser for the MURML sequence element
 * @author hvanwelbergen
 * 
 */
public class Sequence extends MURMLElement
{

    private static final String XMLTAG = "sequence";

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
