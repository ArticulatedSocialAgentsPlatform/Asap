package asap.rsbworldenvironment;

import hmi.animation.VJoint;
import hmi.environmentbase.Environment;
import hmi.util.ClockListener;
import hmi.worldobjectenvironment.VJointWorldObject;
import hmi.worldobjectenvironment.WorldObject;
import hmi.worldobjectenvironment.WorldObjectManager;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import rsb.AbstractDataHandler;
import rsb.Factory;
import rsb.InitializeException;
import rsb.Listener;
import rsb.RSBException;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import asap.rsbworldenvironment.Rsbworldenvironment.RSBWorldObject;
import asap.rsbworldenvironment.Rsbworldenvironment.RSBWorldObjects;

import com.google.common.primitives.Floats;
/**
 * Manages world objects that are sent from an external rendering environment through rsb.
 * @author hvanwelbergen
 * 
 */
@Slf4j
public class RsbWorldEnvironment implements ClockListener, Environment
{
    private final WorldObjectManager woManager;

    public static final String SCENEINFO_SCOPE = "/asap/sceneinfo";
    private volatile boolean shutdown = false;
    private final Listener listener;

    @Getter
    @Setter
    private String id = "";

    public RsbWorldEnvironment(WorldObjectManager wm)
    {
        woManager = wm;
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(RSBWorldObjects.getDefaultInstance()));
        
        Factory factory = Factory.getInstance();
        try
        {
            listener = factory.createListener(SCENEINFO_SCOPE);
        }
        catch (InitializeException e)
        {
            throw new RuntimeException(e);
        }
        try
        {

            listener.addHandler(new AbstractDataHandler<RSBWorldObjects>()
            {
                @Override
                public void handleEvent(RSBWorldObjects wos)
                {
                    for(RSBWorldObject wo: wos.getWorldObjectsList())
                    {
                        setWorldObject(wo.getObjectId(), Floats.toArray(wo.getPositionList()));
                    }                    
                }
            }, true);
        }
        catch (InterruptedException e)
        {
            Thread.interrupted();
        }
        try
        {
            listener.activate();
        }
        catch (InitializeException e)
        {
            throw new RuntimeException(e);
        }
        catch (RSBException e)
        {
            throw new RuntimeException(e);
        }        
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

    @Override
    public void initTime(double initTime)
    {

    }

    @Override
    public void time(double currentTime)
    {

    }

    @Override
    public boolean isShutdown()
    {
        return shutdown;
    }

    @Override
    public void requestShutdown()
    {
        try
        {
            listener.deactivate();
        }
        catch (RSBException e)
        {
            log.warn("",e);
        }
        catch (InterruptedException e)
        {
            Thread.interrupted();
        }
        shutdown = true;
    }

}
