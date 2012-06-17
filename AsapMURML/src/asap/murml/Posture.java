package asap.murml;

import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;

/**
 * Parses a MURML posture
 * @author hvanwelbergen
 *
 */
public class Posture extends MURMLElement
{
    @Getter
    private List<JointValue> jointValues = new ArrayList<JointValue>();
    
    @Override
    public boolean hasContent()
    {
        return true;
    }
    
    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        StringBuffer contentBuffer = new StringBuffer();
        while (!tokenizer.atETag())
        {
            if (tokenizer.atCDSect())
            {
                contentBuffer = contentBuffer.append(tokenizer.takeCDSect());
            }
            else if (tokenizer.atCharData())
            {
                contentBuffer = contentBuffer.append(tokenizer.takeCharData());
            }
        }
        Pattern regex = Pattern.compile("(?<=\\()(.+?)(?=\\))"); //match everything inside braces
        Matcher regexMatcher = regex.matcher(contentBuffer.toString());
        List<String> matchList = new ArrayList<String>();
        while (regexMatcher.find()) {
            matchList.add(regexMatcher.group());
        }

        for (String spec:matchList)
        {
            String specElements[]=spec.split("\\s");
            String jointId = specElements[0];
            int nrOfDof = Integer.parseInt(specElements[1]);
            float dofs[] = new float[nrOfDof];
            for(int i=0;i<nrOfDof;i++)
            {
                dofs[i] = Float.parseFloat(specElements[i+2]);
            }
            jointValues.add(new JointValue(jointId,dofs));
        }
    }
    
    /*
     * The XML Stag for XML encoding
     */
    private static final String XMLTAG = "posture";

    /**
     * The XML Stag for XML encoding -- use this static method when you want to
     * see if a given String equals the xml tag for this class
     */
    public static String xmlTag()
    {
        return XMLTAG;
    }

    /**
     * The XML Stag for XML encoding -- use this method to find out the run-time
     * xml tag of an object
     */
    @Override
    public String getXMLTag()
    {
        return XMLTAG;
    }
}
