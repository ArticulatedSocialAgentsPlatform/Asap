/*******************************************************************************
 *******************************************************************************/
package asap.ipaacaworldenvironment;

import hmi.animation.VJoint;
import hmi.environmentbase.Environment;
import hmi.util.ClockListener;
import hmi.worldobjectenvironment.VJointWorldObject;
import hmi.worldobjectenvironment.WorldObject;
import hmi.worldobjectenvironment.WorldObjectManager;
import ipaaca.AbstractIU;
import ipaaca.HandlerFunctor;
import ipaaca.IUEventHandler;
import ipaaca.IUEventType;
import ipaaca.Initializer;
import ipaaca.InputBuffer;
import ipaaca.LocalMessageIU;
import ipaaca.OutputBuffer;

import java.util.EnumSet;

import lombok.Getter;
import lombok.Setter;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.primitives.Floats;

/**
 * Manages world objects that are sent from an external rendering environment through ipaaca.
 * @author hvanwelbergen
 * 
 */
public class IpaacaWorldEnvironment implements ClockListener, Environment
{
    private final WorldObjectManager woManager;
    private final OutputBuffer outBuffer;
    private final InputBuffer inBuffer;

    private static final String SCENEINFO_CAT = "sceneinfo";
    private static final String COMMAND_KEY = "cmd";
    private static final String DATA_KEY = "data";
    private static final String REPORT_CMD = "report";
    private static final String REPORTING_CMD = "reporting";
    private volatile boolean shutdown = false;

    @Getter
    @Setter
    private String id = "";

    static
    {
        Initializer.initializeIpaacaRsb();
    }

    public IpaacaWorldEnvironment(WorldObjectManager wm)
    {
        woManager = wm;
        outBuffer = new OutputBuffer("IpaacaWorldEnvironment");
        inBuffer = new InputBuffer("IpaacaWorldEnvironment", ImmutableSet.of(SCENEINFO_CAT));
        inBuffer.registerHandler(new IUEventHandler(new SceneInfoHandler(), EnumSet.of(IUEventType.MESSAGE, IUEventType.ADDED, IUEventType.UPDATED),
                ImmutableSet.of(SCENEINFO_CAT)));
    }

    private void getReport()
    {
        LocalMessageIU message = new LocalMessageIU();
        message.setCategory(SCENEINFO_CAT);
        message.getPayload().put(COMMAND_KEY, REPORT_CMD);
        outBuffer.add(message);
    }

    private void setWorldObject(String name, float[] pos)
    {
        WorldObject wo = woManager.getWorldObject(name);
        VJoint vj = null;
        if (wo == null)        
        {
            vj = new VJoint(name, name);
            wo = new VJointWorldObject(vj);
            woManager.addWorldObject(name, wo);
        }
        wo.setTranslation(pos);
    }

    class SceneInfoHandler implements HandlerFunctor
    {
        @Override
        public void handle(AbstractIU iu, IUEventType type, boolean local)
        {
            if (iu.getPayload().get(COMMAND_KEY).equals(REPORTING_CMD))
            {
                String data = iu.getPayload().get(DATA_KEY);                
                if (data.length() > 2)
                {
                    String elems[] = Iterables.toArray(
                            Splitter.on(")(").trimResults().omitEmptyStrings().split(data.substring(1, data.length() - 1)), String.class);
                    for (String elem : elems)
                    {
                        String s[] = elem.split("\\s+");
                        if (s.length >= 4)
                        {
                            String name = elem.substring(0, elem.indexOf(s[s.length - 3])).trim();
                            float x = Floats.tryParse(s[s.length - 3]);
                            float y = Floats.tryParse(s[s.length - 2]);
                            float z = Floats.tryParse(s[s.length - 1]);
                            setWorldObject(name, new float[] { x, y, z });
                        }
                    }
                }
            }
            else
            {
                //log.warn("Invalid command {}.", iu.getPayload().get(COMMAND_KEY));
            }
        }
    }

    @Override
    public void initTime(double initTime)
    {
        getReport();
    }

    @Override
    public void time(double currentTime)
    {
        getReport();
    }

    @Override
    public boolean isShutdown()
    {
        return shutdown;
    }

    @Override
    public void requestShutdown()
    {
        outBuffer.close();
        inBuffer.close();
        shutdown = true;
    }

}
