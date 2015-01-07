/*******************************************************************************
 *******************************************************************************/
package asap.bml.ext.bmlt;

import hmi.xml.XMLFormatting;
import hmi.xml.XMLScanException;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import lombok.Getter;
import saiba.bml.parser.SyncPoint;

import com.google.common.collect.ImmutableList;

/**
 * BMLT Morphkeyframe/mocap animation behavior
 * @author herwinvw
 *
 */
public class BMLTFaceKeyframeBehaviour extends BMLTBehaviour
{
    public String name;
    public String content;

    public enum Type
    {
        MORPH, FACS, FAPS;
    }

    @Getter
    private Type type = Type.MORPH;

    private static final List<String> DEFAULT_SYNCS = ImmutableList.of("start", "end");

    public static List<String> getDefaultSyncPoints()
    {
        return DEFAULT_SYNCS;
    }

    @Override
    public boolean satisfiesConstraint(String n, String value)
    {
        if (n.equals("name") && value.equals(name)) return true;
        return super.satisfiesConstraint(n, value);
    }

    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        while (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            if (tag.equals(BMLTParameter.xmlTag()))
            {
                BMLTParameter param = new BMLTParameter();
                param.readXML(tokenizer);
                parameters.put(param.name, param);
            }
            else if (tag.equals("FaceInterpolator"))
            {
                content = tokenizer.getXMLSection();
            }
            else
            {
                throw new XMLScanException("Invalid content " + tag + " in BMLTFaceKeyframeBehaviour " + id);
            }
        }
    }

    @Override
    public StringBuilder appendContent(StringBuilder buf, XMLFormatting fmt)
    {
        if (content != null) buf.append(content);
        return super.appendContent(buf, fmt);
    }

    @Override
    public boolean hasContent()
    {
        if (content != null) return true;
        return super.hasContent();
    }

    public BMLTFaceKeyframeBehaviour(String bmlId, XMLTokenizer tokenizer) throws IOException
    {
        super(bmlId);
        readXML(tokenizer);
    }

    @Override
    public void addDefaultSyncPoints()
    {
        for (String s : getDefaultSyncPoints())
        {
            addSyncPoint(new SyncPoint(bmlId, id, s));
        }
    }

    @Override
    public StringBuilder appendAttributeString(StringBuilder buf, XMLFormatting fmt)
    {
        if (name != null && !name.isEmpty())
        {
            appendAttribute(buf, "name", name);
        }
        appendAttribute(buf, "type", type.toString());
        return super.appendAttributeString(buf, fmt);
    }

    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        name = getOptionalAttribute("name", attrMap, null);
        type = Type.valueOf(getOptionalAttribute("type", attrMap, Type.MORPH.toString()));
        super.decodeAttributes(attrMap, tokenizer);
    }

    /*
     * The XML Stag for XML encoding
     */
    private static final String XMLTAG = "facekeyframe";

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
