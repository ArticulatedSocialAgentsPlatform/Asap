/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.controller;

import hmi.physics.PhysicalHumanoid;
import hmi.physics.controller.ControllerParameterException;
import hmi.physics.controller.ControllerParameterNotFoundException;
import hmi.physics.controller.PhysicalController;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * A physical controller that is a compound of several other controllers.
 * XML writing functionality is not implemented.
 * @author Dennis Reidsma
 */
public class CompoundController extends XMLStructureAdapter implements PhysicalController
{
    private Map<String, PhysicalController> requiredControllers = new HashMap<String, PhysicalController>();
    private Map<String, PhysicalController> desiredControllers = new HashMap<String, PhysicalController>();

    private String[] jointIDs = new String[0];
    private String desJointIDs[] = new String[0];
    private PhysicalHumanoid pHuman;

    @Override
    public void reset()
    {
        for (PhysicalController controller : requiredControllers.values())
        {
            controller.reset();
        }
        for (PhysicalController controller : desiredControllers.values())
        {
            controller.reset();
        }
    }

    @Override
    public void setPhysicalHumanoid(PhysicalHumanoid p)
    {
        pHuman = p;
        for (PhysicalController controller : requiredControllers.values())
        {
            controller.setPhysicalHumanoid(p);
        }

        for (PhysicalController controller : desiredControllers.values())
        {
            controller.setPhysicalHumanoid(p);
        }
    }

    @Override
    public Set<String> getRequiredJointIDs()
    {
        return ImmutableSet.copyOf(jointIDs);        
    }

    @Override
    public Set<String> getDesiredJointIDs()
    {
        return ImmutableSet.copyOf(desJointIDs);
    }

    public CompoundController()
    {
        super();
    }

    public CompoundController(PhysicalHumanoid p)
    {
        // super();
        setPhysicalHumanoid(p);
        reset();
    }

    @Override
    public void update(double timeDiff)
    {
        for (PhysicalController controller : requiredControllers.values())
        {
            controller.update(timeDiff);
        }

        // only run a desiredController if all it's required joints are part of the VH
        for (PhysicalController controller : desiredControllers.values())
        {
            boolean containsAllReq = true;
            for (String j : controller.getRequiredJointIDs())
            {
                if (pHuman.getSegment(j) == null)
                {
                    containsAllReq = false;
                    break;
                }
            }
            if (containsAllReq)
            {
                controller.update(timeDiff);
            }
        }
    }

    @Override
    public PhysicalController copy(PhysicalHumanoid ph)
    {
        CompoundController c = new CompoundController(ph);
        for (String id : requiredControllers.keySet())
        {
            c.addRequiredController(requiredControllers.get(id).copy(ph), id);
        }

        for (String id : desiredControllers.keySet())
        {
            c.addDesiredController(desiredControllers.get(id).copy(ph), id);
        }
        return c;
    }

    public void addRequiredController(PhysicalController newController, String id)
    {
        requiredControllers.put(id, newController);

        List<String> idList = Arrays.asList(jointIDs);
        HashSet<String> idSet = new HashSet<String>();
        idSet.addAll(idList);
        for (String newId : newController.getRequiredJointIDs())
        {
            if (!idSet.contains(newId))
            {
                idSet.add(newId);
            }
        }
        jointIDs = idSet.toArray(new String[idSet.size()]);

        List<String> desIdList = Arrays.asList(desJointIDs);
        HashSet<String> desIdSet = new HashSet<String>();
        idSet.addAll(desIdList);
        for (String newId : newController.getDesiredJointIDs())
        {
            if (!desIdSet.contains(newId))
            {
                desIdSet.add(newId);
            }
        }
        desJointIDs = desIdSet.toArray(new String[desIdSet.size()]);
    }

    public void addDesiredController(PhysicalController newController, String id)
    {
        desiredControllers.put(id, newController);

        List<String> idList = Arrays.asList(desJointIDs);
        HashSet<String> idSet = new HashSet<String>();
        idSet.addAll(idList);
        for (String newId : newController.getRequiredJointIDs())
        {
            if (!idSet.contains(newId))
            {
                idSet.add(newId);
            }
        }
        desJointIDs = idSet.toArray(new String[idSet.size()]);

        List<String> desIdList = Arrays.asList(desJointIDs);
        idSet.addAll(desIdList);
        for (String newId : newController.getDesiredJointIDs())
        {
            if (!idSet.contains(newId))
            {
                idSet.add(newId);
            }
        }
        desJointIDs = idSet.toArray(new String[idSet.size()]);
    }

    @Override
    public String getParameterValue(String name) throws ControllerParameterNotFoundException
    {
        // split name
        String[] splitName = name.split(":");
        if (splitName.length < 2) throw new RuntimeException(
                "Parameters in compound controller must always be prefixed with id of target controller and a colon.");
        // determine which controller... (by ID)

        PhysicalController c = requiredControllers.get(splitName[0]);
        if (c == null)
        {
            c = desiredControllers.get(splitName[0]);
        }
        if (c == null)
        {
            throw new ControllerParameterNotFoundException(name);
        }
        return c.getParameterValue(splitName[1]);
    }

    @Override
    public float getFloatParameterValue(String name) throws ControllerParameterNotFoundException
    {
        // split name
        String[] splitName = name.split(":");
        if (splitName.length < 2) throw new RuntimeException(
                "Parameters in compound controller must always be prefixed with id of target controller and a colon.");
        // determine which controller... (by ID)

        PhysicalController c = requiredControllers.get(splitName[0]);
        if (c == null)
        {
            c = desiredControllers.get(splitName[0]);
        }
        if (c == null)
        {
            throw new ControllerParameterNotFoundException(name);
        }
        return c.getFloatParameterValue(splitName[1]);
    }

    @Override
    public void setParameterValue(String name, String value) throws ControllerParameterException
    {
        String[] splitName = name.split(":");
        if (splitName.length < 2)
        {
            throw new ControllerParameterException(
                    "Parameters in compound controller must always be prefixed with id of target controller and a colon.");
        }

        PhysicalController c = requiredControllers.get(splitName[0]);
        if (c == null)
        {
            c = desiredControllers.get(splitName[0]);
        }
        if (c != null)
        {
            c.setParameterValue(splitName[1], value);
        }
        else
        {
            throw new ControllerParameterNotFoundException(name);
        }
    }

    @Override
    public void setParameterValue(String name, float value) throws ControllerParameterException
    {
        String[] splitName = name.split(":");
        if (splitName.length < 2) throw new RuntimeException(
                "Parameters in compound controller must always be prefixed with id of target controller and a colon.");

        PhysicalController c = requiredControllers.get(splitName[0]);
        if (c == null)
        {
            c = desiredControllers.get(splitName[0]);
        }
        if (c != null)
        {
            c.setParameterValue(splitName[1], value);
        }
        else
        {
            throw new ControllerParameterNotFoundException(name);
        }
    }

    /*
     * ================== XML ==================
     */

    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
    }

    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        while (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            if (tag.equals("required"))
            {
                tokenizer.takeSTag("required");
                while (tokenizer.atSTag(XMLController.xmlTag()))
                {
                    XMLController xc = new XMLController();
                    xc.readXML(tokenizer);
                    addRequiredController(xc.getController(), xc.getId());
                }
                tokenizer.takeETag("required");
            }
            else if (tag.equals("desired"))
            {
                tokenizer.takeSTag("desired");
                while (tokenizer.atSTag(XMLController.xmlTag()))
                {
                    XMLController xc = new XMLController();
                    xc.readXML(tokenizer);
                    addDesiredController(xc.getController(), xc.getId());
                }
                tokenizer.takeETag("desired");
            }
            else
            {
                throw new RuntimeException("Unkown content tag in CompoundController XML");
            }
        }
    }

    /*
     * The XML Stag for XML encoding
     */
    private static final String XMLTAG = "CompoundController";

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

    @Override
    public Set<String> getJoints()
    {
        return getRequiredJointIDs();
    }

}
