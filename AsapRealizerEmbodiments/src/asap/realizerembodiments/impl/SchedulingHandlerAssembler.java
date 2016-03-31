/*******************************************************************************
 *******************************************************************************/
package asap.realizerembodiments.impl;

import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import asap.realizer.pegboard.PegBoard;
import asap.realizer.scheduler.SchedulingHandler;
import asap.realizer.scheduler.SchedulingStrategy;

/**
 * Constructs a SchedulingHandler from an xml description
 * For now assumes that each SchedulingHandler has a constructor with as the sole attribute a scheduling strategy
 * @author hvanwelbergen
 *
 */
public class SchedulingHandlerAssembler extends XMLStructureAdapter
{
    SchedulingHandler bmlSchedulingHandler;
    private final PegBoard pegBoard;
    
    public SchedulingHandlerAssembler(PegBoard pb)
    {
        pegBoard = pb;
    }
    
    public SchedulingHandler getBMLSchedulingHandler()
    {
        
        return bmlSchedulingHandler;
    }

    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)  
    {
        String className = getRequiredAttribute("class", attrMap, tokenizer);
        String schedulingStrategy = getRequiredAttribute("schedulingStrategy", attrMap, tokenizer);
        SchedulingStrategy strategy = null;
        try
        {
            Class<?> schedulStratClass = Class.forName(schedulingStrategy);
            Constructor<?> constructor = schedulStratClass.getConstructor(PegBoard.class);
            strategy = (SchedulingStrategy) constructor.newInstance(pegBoard);            
        }
        catch (ClassNotFoundException e)
        {
            throw new XMLScanException("Cannot instantiate schedulingstrategy with class "+schedulingStrategy,e);
        }
        catch (InstantiationException e)
        {
            throw new XMLScanException("Cannot instantiate schedulingstrategy with class "+schedulingStrategy,e);
        }
        catch (IllegalAccessException e)
        {
            throw new XMLScanException("Cannot instantiate schedulingstrategy with class "+schedulingStrategy,e);
        }
        catch (IllegalArgumentException e)
        {
            throw new XMLScanException("Cannot instantiate schedulingstrategy with class "+schedulingStrategy,e);
        }
        catch (InvocationTargetException e)
        {
            throw new XMLScanException("Cannot instantiate schedulingstrategy with class "+schedulingStrategy,e);
        }
        catch (SecurityException e)
        {
            throw new XMLScanException("Cannot instantiate schedulingstrategy with class "+schedulingStrategy,e);
        }
        catch (NoSuchMethodException e)
        {
            throw new XMLScanException("Cannot instantiate schedulingstrategy with class "+schedulingStrategy,e);
        }
        
        try
        {
            Class<?> schedulingHandlerClass = Class.forName(className);
            Constructor<?> constructor = schedulingHandlerClass.getConstructor(SchedulingStrategy.class, PegBoard.class);
            bmlSchedulingHandler = (SchedulingHandler)constructor.newInstance(strategy, pegBoard);
        }
        catch (ClassNotFoundException e)
        {
            throw new XMLScanException("Cannot instantiate schedulinghandler with class "+className,e);
        }
        catch (SecurityException e)
        {
            throw new XMLScanException("Cannot instantiate schedulinghandler with class "+className,e);
        }
        catch (NoSuchMethodException e)
        {
            throw new XMLScanException("Cannot instantiate schedulinghandler with class "+className,e);
        }
        catch (IllegalArgumentException e)
        {
            throw new XMLScanException("Cannot instantiate schedulinghandler with class "+className,e);
        }
        catch (InstantiationException e)
        {
            throw new XMLScanException("Cannot instantiate schedulinghandler with class "+className,e);
        }
        catch (IllegalAccessException e)
        {
            throw new XMLScanException("Cannot instantiate schedulinghandler with class "+className,e);
        }
        catch (InvocationTargetException e)
        {
            throw new XMLScanException("Cannot instantiate schedulinghandler with class "+className,e);
        }
        
    }
    
    private static final String XMLTAG = "SchedulingHandler";    
    /**
     * The XML Stag for XML encoding -- use this static method when you want to see if a given String equals
     * the xml tag for this class
     */
    public static String xmlTag()
    {
        return XMLTAG;
    }

    /**
     * The XML Stag for XML encoding -- use this method to find out the run-time xml tag of an object
     */
    @Override
    public String getXMLTag()
    {
        return XMLTAG;
    }
}
