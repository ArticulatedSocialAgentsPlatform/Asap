package asap.animationengine.gesturebinding;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asap.animationengine.motionunit.MotionUnit;

import hmi.util.Resources;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

public class VisimeSpec extends XMLStructureAdapter implements ParameterDefaultsHandler
{
    private MotionUnit motionUnit;
    private final Resources resources;
    private int visime;
    private HashMap<String,MotionUnitParameterDefault>parameterdefault = new HashMap<String,MotionUnitParameterDefault>();
    private final static Logger logger = LoggerFactory.getLogger(VisimeSpec.class.getName());
    
    public int getVisime()
    {
        return visime;
    }
    
    public VisimeSpec(Resources r)
    {
        resources = r;
    }
    
    public MotionUnit getMotionUnit()
    {
        return motionUnit;
    }    
    
    /**
     * Get motion unit parameter for BML parameter src     
     */
    public Collection<MotionUnitParameterDefault> getParameterDefaults()
    {
        return parameterdefault.values();
    }
    
    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        visime = getRequiredIntAttribute("visime", attrMap, tokenizer);        
        //specnamespace = getOptionalAttribute("namespace", attrMap, null);
    }
    
    public void addParameterDefault(MotionUnitParameterDefault p)
    {
        parameterdefault.put(p.name, p);
    }
    
    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
      String tag = "";
      try
      {
        while (tokenizer.atSTag())
        {
            tag = tokenizer.getTagName();
            if (tag.equals(ParameterDefaults.xmlTag())) 
            {
                ParameterDefaults def = new ParameterDefaults(this);
                def.readXML(tokenizer);
            }
            else if (tag.equals(MotionUnitAssembler.xmlTag()))
            {
                MotionUnitAssembler mua = new MotionUnitAssembler(resources);
                mua.readXML(tokenizer);
                motionUnit = mua.getMotionUnit();
            }
        }
      }
      catch (RuntimeException ex)
      {
        logger.warn("Cannot read motion unit spec, dropping element from gesture binding. Tag: {} ", tag);
        motionUnit = null;
      }
    }
    
    /*
     * The XML Stag for XML encoding
     */
    private static final String XMLTAG = "VisimeSpec";
  
    /**
     * The XML Stag for XML encoding -- use this static method when you want to see if a given String equals
     * the xml tag for this class
     */
    public static String xmlTag() { return XMLTAG; }
  
    /**
     * The XML Stag for XML encoding -- use this method to find out the run-time xml tag of an object
     */
    @Override
    public String getXMLTag() {
       return XMLTAG;
    }
}
