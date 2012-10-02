package asap.ipaacaembodiments.xmlhumanoid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.ImmutableList;

import lombok.Getter;

import hmi.math.Vec3f;
import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

public class Joint extends XMLStructureAdapter
{
    private List<Segment> segments = new ArrayList<Segment>();
    private Axis1 axis1;
    private Axis2 axis2;
    private Axis3 axis3;

    @Getter
    private String name;
    @Getter
    private String type;
    @Getter
    private String alias;
    @Getter
    private int numChilds;

    @Getter
    private float angles[] = Vec3f.getVec3f();
    @Getter
    private float ulimits[] = Vec3f.getVec3f();
    @Getter
    private float llimits[] = Vec3f.getVec3f();

    public ImmutableList<Segment> getSegments()
    {
        return ImmutableList.copyOf(segments);
    }

    private void setAxis(int nr, Axis a)
    {
        angles[nr] = a.getAngle();
        ulimits[nr] = a.getUlimit();
        llimits[nr] = a.getLlimit();
    }

    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        while (tokenizer.atSTag())
        {
            switch (tokenizer.getTagName())
            {
            case Axis1.XMLTAG:
                axis1 = new Axis1();
                axis1.readXML(tokenizer);
                setAxis(0, axis1);
                break;
            case Axis2.XMLTAG:
                axis2 = new Axis2();
                axis2.readXML(tokenizer);
                setAxis(1, axis2);
                break;
            case Axis3.XMLTAG:
                axis3 = new Axis3();
                axis3.readXML(tokenizer);
                setAxis(2, axis3);
                break;
            case Segment.XMLTAG:
                Segment segment = new Segment();
                segment.readXML(tokenizer);
                segments.add(segment);
                break;
            default:
                throw new XMLScanException("Invalid tag " + tokenizer.getTagName() + " in <Humanoid>");
            }
        }
    }

    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        name = getRequiredAttribute("name", attrMap, tokenizer);
        type = getRequiredAttribute("type", attrMap, tokenizer);
        alias = getRequiredAttribute("alias", attrMap, tokenizer);
        numChilds = getRequiredIntAttribute("num_childs", attrMap, tokenizer);
    }

    private static class Axis extends XMLStructureAdapter
    {
        @Getter
        protected float angle;
        @Getter
        protected float llimit;
        @Getter
        protected float ulimit;

        @Override
        public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
        {
            angle = (float)Math.toRadians(getRequiredFloatAttribute("angle", attrMap, tokenizer));
            llimit = (float)Math.toRadians(getRequiredFloatAttribute("llimit", attrMap, tokenizer));
            ulimit = (float)Math.toRadians(getRequiredFloatAttribute("ulimit", attrMap, tokenizer));
        }
    }

    private static class Axis1 extends Axis
    {
        public static final String XMLTAG = "Axis1";

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

    private static class Axis2 extends Axis
    {
        public static final String XMLTAG = "Axis2";

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

    private static class Axis3 extends Axis
    {
        public static final String XMLTAG = "Axis3";

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

    public static final String XMLTAG = "Joint";

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
