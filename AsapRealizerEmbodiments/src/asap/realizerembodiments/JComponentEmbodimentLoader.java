package asap.realizerembodiments;

import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.util.ArrayUtils;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;

import lombok.Getter;
import lombok.Setter;

/**
 * Loader for JComponentEmbodiments from a JComponentEnvironment
 * @author Herwin
 * 
 */
public class JComponentEmbodimentLoader implements EmbodimentLoader
{
    @Getter
    @Setter
    private String id = "";
    private JComponentEmbodiment embodiment;

    public JComponentEmbodiment getEmbodiment()
    {
        return embodiment;
    }

    private class JComponentInfo extends XMLStructureAdapter
    {
        @Getter
        private String id;

        public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
        {
            id = getRequiredAttribute("id", attrMap, tokenizer);
        }

        public String getXMLTag()
        {
            return XMLTAG;
        }

        public static final String XMLTAG = "JComponent";
    }

    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
            Loader... requiredLoaders) throws IOException
    {
        id = loaderId;
        JComponentEnvironment jce = ArrayUtils.getFirstClassOfType(environments, JComponentEnvironment.class);
        if (jce == null)
        {
            throw tokenizer.getXMLScanException("JComponentEmbodimentLoader requires an JComponentEnvironment");
        }

        JComponentInfo jci = null;
        if (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            if (tag.equals(JComponentInfo.XMLTAG))
            {
                jci = new JComponentInfo();
                jci.readXML(tokenizer);
            }
        }
        if (jci==null)
        {
            throw tokenizer.getXMLScanException("JComponentEmbodimentLoader requires an JComponent section");
        }
        embodiment = jce.getJComponentEmbodiment(jci.getId());
        if(embodiment == null)
        {
            throw tokenizer.getXMLScanException("No JComponent with id "+jci.getId()+"registered in the environment");
        }
    }

    @Override
    public void unload()
    {
    }
}
