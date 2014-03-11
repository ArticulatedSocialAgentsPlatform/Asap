package asap.rsbembodiments;

import hmi.animation.VJoint;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import rsb.AbstractDataHandler;
import rsb.Event;
import rsb.Factory;
import rsb.Listener;
import rsb.RSBException;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import rsb.patterns.EventCallback;
import rsb.patterns.LocalServer;
import asap.rsbembodiments.Rsbembodiments.AnimationData;
import asap.rsbembodiments.Rsbembodiments.AnimationDataConfigReply;
import asap.rsbembodiments.Rsbembodiments.AnimationDataConfigRequest;
import asap.rsbembodiments.Rsbembodiments.AnimationSelection;
import asap.rsbembodiments.util.VJointRsbUtils;

import com.google.common.primitives.Floats;

/**
 * Testing stub for a VJoint skeleton steered through rsb
 * @author hvanwelbergen
 *
 */
public class StubBody
{
    private final LocalServer server;
    private final VJoint vjoint;
    
    @Getter
    private List<String> jointList = new ArrayList<String>();

    private class JointDataConfigCallback extends EventCallback
    {
        @Override
        public Event invoke(final Event request) throws Throwable
        {
            return new Event(AnimationDataConfigReply.class, AnimationDataConfigReply.newBuilder()
                    .setSkeleton(VJointRsbUtils.toRsbSkeleton(vjoint)).build());
        }
    }

    public void deactivate() throws RSBException, InterruptedException
    {
        server.deactivate();
    }

    public void startServer() throws RSBException
    {
        new Thread()
        {
            @Override
            public void run()
            {
                // Block until server.deactivate or process shutdown
                server.waitForShutdown();
            }
        };
    }

    private class AnimationDataHandler extends AbstractDataHandler<AnimationData>
    {
        @Override
        public void handleEvent(AnimationData aData)
        {
            float q[] = Floats.toArray(aData.getJointQuatsList());
            for (int i = 0; i < jointList.size(); i++)
            {
                vjoint.getPart(jointList.get(i)).setRotation(q, i * 4);
            }
        }

    }

    private class AnimationSelectionHandler extends AbstractDataHandler<AnimationSelection>
    {
        @Override
        public void handleEvent(AnimationSelection aSelection)
        {
            jointList = new ArrayList<String>(aSelection.getSelectedJointsList());
        }
    }

    public StubBody(VJoint root) throws RSBException, InterruptedException
    {
        this.vjoint = root;
        final ProtocolBufferConverter<AnimationData> jointDataConverter = new ProtocolBufferConverter<AnimationData>(
                AnimationData.getDefaultInstance());
        final ProtocolBufferConverter<AnimationDataConfigRequest> jointDataReqConverter = new ProtocolBufferConverter<AnimationDataConfigRequest>(
                AnimationDataConfigRequest.getDefaultInstance());
        final ProtocolBufferConverter<AnimationDataConfigReply> jointDataConfigReplyConverter = new ProtocolBufferConverter<AnimationDataConfigReply>(
                AnimationDataConfigReply.getDefaultInstance());
        final ProtocolBufferConverter<AnimationSelection> animationSelection = new ProtocolBufferConverter<AnimationSelection>(
                AnimationSelection.getDefaultInstance());
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(jointDataConverter);
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(jointDataReqConverter);
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(jointDataConfigReplyConverter);
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(animationSelection);

        final Factory factory = Factory.getInstance();
        server = factory.createLocalServer(RSBEmbodimentConstants.ANIMATIONDATACONFIG_CATEGORY);
        server.activate();

        final Listener jointDataListener = factory.createListener(RSBEmbodimentConstants.ANIMATIONDATA_CATEGORY);
        final Listener jointSelectionListener = factory.createListener(RSBEmbodimentConstants.ANIMATIONSELECTION_CATEGORY);
        jointDataListener.activate();
        jointSelectionListener.activate();
        jointDataListener.addHandler(new AnimationDataHandler(), true);
        jointSelectionListener.addHandler(new AnimationSelectionHandler(), true);

        server.addMethod(RSBEmbodimentConstants.ANIMATIONDATACONFIG_REQUEST_FUNCTION, new JointDataConfigCallback());
    }
}
