package asap.ipaacaembodiments;

import hmi.animation.VJoint;
import hmi.environmentbase.Embodiment;
import hmi.math.Mat4f;
import hmi.math.Quat4f;
import hmi.math.Vec3f;
import ipaaca.AbstractIU;
import ipaaca.HandlerFunctor;
import ipaaca.IUEventHandler;
import ipaaca.IUEventType;
import ipaaca.Initializer;
import ipaaca.InputBuffer;
import ipaaca.LocalMessageIU;
import ipaaca.OutputBuffer;
import ipaaca.util.ComponentNotifier;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.concurrent.GuardedBy;

import lombok.extern.slf4j.Slf4j;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Interfaces with an Ipaaca graphical environment.
 * Currently Ipaaca graphical environments are assumed to contain only one character.
 * @author hvanwelbergen
 * 
 */
@Slf4j
public class IpaacaEmbodiment implements Embodiment
{
    private String id;
    private final Object ipaacaLock = new Object();

    private InputBuffer inBuffer;
    private OutputBuffer outBuffer;

    private AtomicReference<List<String>> availableMorphs = new AtomicReference<>();
    private AtomicReference<List<String>> availableJoints = new AtomicReference<>();
    private Object availableJointsLock = new Object();
    
    private List<String> usedMorphs = new ArrayList<>();
    private List<String> usedJoints = new ArrayList<>();    
    private Object rootJointLock = new Object();
    private static final String COMPONENT_NAME = "ipaacaenvironment";
    
    @GuardedBy("rootJointLock")
    private VJoint rootJoint;
    
    static
    {
        Initializer.initializeIpaacaRsb();
    }

    public VJoint getRootJointCopy(String prefix)
    {
        VJoint copy;
        synchronized(rootJointLock)
        {
            copy = rootJoint.copyTree(prefix);
        }
        return copy;
    }
    
    public void setId(String id)
    {
        this.id = id;
    }

    public IpaacaEmbodiment()
    {

    }
    
    public void initialize()
    {
        availableJoints.set(new ArrayList<String>());
        availableMorphs.set(new ArrayList<String>());

        ImmutableSet<String> categories = ImmutableSet.of("jointDataConfigRequest", "componentNotify");
        
        synchronized (ipaacaLock)
        {
            inBuffer = new InputBuffer(COMPONENT_NAME + id, categories);
            outBuffer = new OutputBuffer(COMPONENT_NAME + id);
            inBuffer.registerHandler(new IUEventHandler(new JointDataConfigReqHandler(), EnumSet.of(IUEventType.ADDED, IUEventType.MESSAGE), ImmutableSet
                    .of("jointDataConfigRequest")));            
        }
        ComponentNotifier notifier = new ComponentNotifier(COMPONENT_NAME+id,"animationprovider",  ImmutableSet.of("jointDataConfigReply",
                "jointData"), ImmutableSet.of("jointDataConfigRequest"),outBuffer, inBuffer);
        notifier.initialize();        
    }

    private String getMatrix(float[] m)
    {
        StringBuffer buf = new StringBuffer();
        for (float f : m)
        {
            buf.append(f);
            buf.append(" ");
        }
        return buf.toString().trim();
    }

    private String getJointMatrices(List<float[]> jointMatrices)
    {
        StringBuffer buf = new StringBuffer();
        for(float[] m:jointMatrices)
        {
            buf.append(getMatrix(m));
            buf.append(" ");
            buf.append(getMatrix(Mat4f.getIdentity()));   //dummy global matrix
            buf.append(" ");
        }
        return buf.toString().trim();
    }
    
    public void waitForAvailableJoints()
    {
        synchronized (availableJointsLock)
        {
            while (availableJoints.get().isEmpty())
            {
                try
                {
                    availableJointsLock.wait();
                }
                catch (InterruptedException e)
                {
                    Thread.interrupted();
                }
            }
        }
    }

    public void setJointData(List<float[]>jointLocalMatrices, ImmutableMap<String, Float> morphTargets)
    {
        if (availableJoints.get().isEmpty() && availableMorphs.get().isEmpty())
        {
            log.warn("setJointData ignored, no available joints yet");
            return;
        }

        List<String> usedTargets = new ArrayList<>();
        List<String> values = new ArrayList<>();
        for (String morph : availableMorphs.get())
        {
            if (morphTargets.keySet().contains(morph))
            {
                usedTargets.add(morph);
                values.add("" + morphTargets.get(morph) * 100);
            }
        }

        LocalMessageIU iu = new LocalMessageIU();
        iu.setCategory("jointData");
        
        iu.getPayload().put("morph_data", Joiner.on(" ").join(values));
        if (jointLocalMatrices.isEmpty())
        {
            // XXX no jointlist: add dummy joint data to force re-render of face
            setUsed(ImmutableList.of(availableJoints.get().get(0)), usedTargets);
            iu.getPayload().put("joint_data", "1 0 0 0  0 1 0 0  0 0 1 0  0 0 0 1    1 0 0 0  0 1 0 0  0 0 1 0  0 0 0 1");
        }
        else
        {
            setUsed(usedJoints, usedTargets);
            iu.getPayload().put("joint_data", getJointMatrices(jointLocalMatrices));
        }
        synchronized (ipaacaLock)
        {
            outBuffer.add(iu);
        }
    }

    public ImmutableList<String> getAvailableJoints()
    {
        return ImmutableList.copyOf(availableJoints.get());
    }

    public ImmutableList<String> getAvailableMorphs()
    {
        return ImmutableList.copyOf(availableMorphs.get());
    }

    private void updateUsedSet()
    {
        LocalMessageIU iuConfig = new LocalMessageIU();
        iuConfig.setCategory("jointDataConfigReply");
        iuConfig.getPayload().put("joints_provided", Joiner.on(",").join(usedJoints));
        List<String> np = new ArrayList<>(availableJoints.get());
        np.removeAll(usedJoints);
        iuConfig.getPayload().put("joints_not_provided", Joiner.on(",").join(np));
        iuConfig.getPayload().put("morphs_provided", Joiner.on(",").join(usedMorphs));
        np = new ArrayList<>(availableMorphs.get());
        np.removeAll(usedMorphs);
        iuConfig.getPayload().put("morphs_not_provided", Joiner.on(",").join(np));
        synchronized (ipaacaLock)
        {
            outBuffer.add(iuConfig);
        }
    }

    public void setUsed(List<String> usedJoints, List<String> usedMorphs)
    {
        if (!usedJoints.equals(this.usedJoints) || !usedMorphs.equals(this.usedMorphs))
        {
            this.usedJoints = ImmutableList.copyOf(usedJoints);
            this.usedMorphs = ImmutableList.copyOf(usedMorphs);
            updateUsedSet();
        }
    }

    public void setUsedJoints(List<String> usedJoints)
    {
        if (!usedJoints.equals(this.usedJoints))
        {
            this.usedJoints = ImmutableList.copyOf(usedJoints);
            updateUsedSet();
        }
    }

    public void setUsedMorphs(List<String> usedMorphs)
    {
        if (!usedMorphs.equals(this.usedMorphs))
        {
            this.usedMorphs = ImmutableList.copyOf(usedMorphs);
            updateUsedSet();
        }
    }

    @Override
    public String getId()
    {
        return id;
    }

    public void shutdown()
    {
        synchronized (ipaacaLock)
        {
            if (inBuffer != null)
            {
                inBuffer.close();
            }
            if (outBuffer != null)
            {
                outBuffer.close();
            }
        }
    }

    private void setAvailableJoints(String[] joints)
    {
        availableJoints.set(ImmutableList.copyOf(joints));
        synchronized (availableJointsLock)
        {
            availableJointsLock.notifyAll();
        }        
    }

    private void setAvailableMorphs(String[] morphs)
    {
        availableMorphs.set(ImmutableList.copyOf(morphs));
    }

    private List<Integer> getRootIndices(String parents[])
    {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < parents.length; i++)
        {
            if (parents[i].equals("-"))
            {
                indices.add(i);
            }
        }
        return indices;
    }

    private float[] getVec3f(String str)
    {
        String vStr[] = str.trim().split("\\s+");
        return Vec3f.getVec3f(Float.parseFloat(vStr[0]), Float.parseFloat(vStr[1]), Float.parseFloat(vStr[2]));
    }

    private float[] getQuat4f(String str)
    {
        String vStr[] = str.trim().split("\\s+");
        return Quat4f.getQuat4f(Float.parseFloat(vStr[0]), Float.parseFloat(vStr[1]), Float.parseFloat(vStr[2]), Float.parseFloat(vStr[3]));
    }

    private VJoint constructJoint(int index, String joints[], String parents[], String translations[], String rotations[])
    {
        VJoint vj = new VJoint(joints[index], joints[index]);
        vj.setTranslation(getVec3f(translations[index]));
        vj.setRotation(getQuat4f(rotations[index]));

        for (int i = 0; i < parents.length; i++)
        {
            if(parents[i].equals(joints[index]))
            {
                VJoint vjChild = constructJoint(i, joints, parents, translations, rotations);
                vj.addChild(vjChild);
            }
        }
        return vj;
    }

    private VJoint constructSkeleton(String joints[], String parents[], String translations[], String rotations[])
    {
        List<Integer> indices = getRootIndices(parents);
        VJoint vj;
        if(indices.size()==1)
        {
            vj = constructJoint(indices.get(0), joints, parents, translations, rotations);
        }
        else
        {
            //introduce artifical root joint to bind stuff together
            vj = new VJoint("","");
            for (int i: getRootIndices(parents))
            {
                vj.addChild(constructJoint(i, joints, parents, translations, rotations));
            }
        }
        return vj;
    }

    class JointDataConfigReqHandler implements HandlerFunctor
    {
        @Override
        public void handle(AbstractIU iu, IUEventType type, boolean local)
        {
            if(iu==null) 
            {
                log.warn("IpaacaEmbodiment jointDataConfigRequest null iu!");
                return;
            }
            if(iu.getPayload()==null) 
            {
                log.warn("IpaacaEmbodiment jointDataConfigRequest null payload!");
                return;
            }
            log.info("IpaacaEmbodiment jointDataConfigRequest, payload: {}",iu.getPayload());
            log.info("IpaacaEmbodiment jointDataConfigRequest, morphs: {}",iu.getPayload().get("morphs"));
            
            String[] joints = iu.getPayload().get("joints").split("\\s*,\\s*");
            String[] parents = iu.getPayload().get("joint_parents").split("\\s*,\\s*");
            String[] translations = iu.getPayload().get("joint_translations").split("\\s*,\\s*");
            String[] rotations = iu.getPayload().get("joint_rotations").split("\\s*,\\s*");
            synchronized(rootJointLock)
            {
                rootJoint = constructSkeleton(joints, parents, translations, rotations);
            }
            
            
            setAvailableMorphs(iu.getPayload().get("morphs").split("\\s*,\\s*"));
            setAvailableJoints(joints);
            
            log.debug("Available joints received {}", iu.getPayload().get("joints"));
        }
    }

}
