/*******************************************************************************
 *******************************************************************************/
package asap.nao.naobinding;

import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import asap.nao.Nao;
import asap.nao.planunit.NaoUnit;
import asap.nao.planunit.TimedNaoUnit;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.ParameterException;
//import nao.NAO;
//import nabaztag.Rabbit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import saiba.bml.core.Behaviour;

/**
 * The NaoBinding maps from BML to a set of naounits
 *
 * @author Robin ten Buuren
 */
public class NaoBinding extends XMLStructureAdapter
{
    private ArrayList<NaoUnitSpec> specs = new ArrayList<NaoUnitSpec>();
    private Logger logger = LoggerFactory.getLogger(NaoBinding.class.getName());

    /**
     * Gets a list of timed nao units that satisfy the constraints of behaviour b     
     */
    public List<TimedNaoUnit> getNaoUnit(FeedbackManager fbManager, BMLBlockPeg bbPeg,Behaviour b, Nao nao)
    {
        ArrayList<TimedNaoUnit> nus = new ArrayList<TimedNaoUnit>();
        for (NaoUnitSpec s:specs)
        {
            if(s.getType().equals(b.getXMLTag()) && 
                    ((s.getSpecnamespace()==null && b.getNamespace()==null) || 
                    (s.getSpecnamespace()!=null && s.getSpecnamespace().equals(b.getNamespace())))
                )
            {
                if(!s.satisfiesConstraints(b))
                {
                    //System.out.println("Constraint mismatch");                    
                }
                else
                {
                    //System.out.println("Found type and constraint match");
                    NaoUnit nuCopy = s.naoUnit.copy(nao);
                    TimedNaoUnit tnu = nuCopy.createTNU(fbManager, bbPeg, b.getBmlId(),b.id);
                    nus.add(tnu);
                    
                    //System.out.println("set def params");                    
                    //set default parameter values
                    for(NaoUnitParameterDefault nupc:s.getParameterDefaults())
                    {
                        try
                        {
                            nuCopy.setParameterValue(nupc.name, nupc.value);
                            System.out.println("Toevoegen aan param map: " + nupc.toString());
                        }
                        catch (ParameterException e)
                        {
                            logger.warn("Error in setting default value in getNaoUnit, parameter "+nupc.name, e);                            
                        }
                        logger.debug("Setting parameter {} to default {}",nupc.name, nupc.value);                        
                    }
                    

                    //System.out.println("Map params");                    
                    //map parameters
                    for(String param :s.getParameters())
                    {
                        if(b.specifiesParameter(param))
                        {    
                            String value = b.getStringParameterValue(param);
                            try
                            {
                                nuCopy.setParameterValue(s.getParameter(param), value);
                                System.out.println("Toevoegen aan param map: " + s.getParameter(param).toString());
                            }
                            catch (ParameterException e)
                            {
                                logger.warn("Error in parameter mapping in getNaoUnit, parameter "+param, e);   
                            }
                            logger.debug("Setting parameter {} mapped to  {}",param ,s.getParameter(param));
                        }
                    }
                }
            }
        }
        return nus;

    }
    

    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        while (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            if (tag.equals(NaoUnitSpec.xmlTag()))
            {
                NaoUnitSpec nuSpec = new NaoUnitSpec();
                nuSpec.readXML(tokenizer);
                if (nuSpec.naoUnit != null) specs.add(nuSpec); //don't add failed nao units to the binding
                else logger.warn("Dropped nao unit spec because we could not construct the nao unit");
                System.out.println(nuSpec.getSpecnamespace());
            }
        }
    }

   /*
    * The XML Stag for XML encoding
    */
   private static final String XMLTAG = "naobinding";
 
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
