package asap.rsbembodiments;

import hmi.animation.VJoint;
import hmi.animationembodiments.SkeletonEmbodiment;

import java.util.Arrays;

import javax.annotation.concurrent.GuardedBy;

import lombok.Getter;
import rsb.Factory;
import rsb.Informer;
import rsb.InitializeException;
import rsb.RSBException;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import asap.rsbembodiments.Rsbembodiments.JointData;

/**
 * Interfaces with an rsb graphical environment.
 * Currently rsb graphical environments are assumed to contain only one character.
 * @author hvanwelbergen
 * 
 */
public class RsbBodyEmbodiment implements SkeletonEmbodiment
{
    @Getter
    private String id;
    
    private Informer<JointData> jointDataInformer;
    private static final String JOINTDATA_CATEGORY = "/rsbembodiment/jointData";
    @GuardedBy("submitJointLock")
    private VJoint submitJoint;
    
    public RsbBodyEmbodiment(String id)
    {
        this.id = id;        
    }

    private void initInformer()
    {
        final ProtocolBufferConverter<JointData> converter = new ProtocolBufferConverter<JointData>(JointData.getDefaultInstance());
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(converter);
        try
        {
            jointDataInformer = Factory.getInstance().createInformer(JOINTDATA_CATEGORY);
            jointDataInformer.activate();
        }
        catch (InitializeException e)
        {
            throw new RuntimeException(e);
        }
    }
    public void initialize()
    {
        initInformer();
        // get joints
    }

    @Override
    public void copy()
    {
        // construct float list for rotations, send with informer
        JointData jd = JointData.newBuilder().addAllData(Arrays.asList(1f,2f,3f)).build();
        try
        {
            jointDataInformer.send(jd);
        }
        catch (RSBException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public VJoint getAnimationVJoint()
    {
        return submitJoint;
    }
}
