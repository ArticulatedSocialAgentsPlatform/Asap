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
import ipaaca.LocalIU;
import ipaaca.LocalMessageIU;
import ipaaca.OutputBuffer;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import lombok.extern.slf4j.Slf4j;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Interfaces with an Ipaaca graphical environment. Should currently be initialized before that environment itself is initialized.
 * Currently Ipaaca graphcial environments contain only one character.
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
    private List<String> usedMorphs = new ArrayList<>();
    private List<String> usedJoints = new ArrayList<>();
    private List<AvailableTargetsUpdateListener> targetUpdateListeners = new ArrayList<>();
    
    static
    {
        Initializer.initializeIpaacaRsb();
    }

    public void addTargetUpdateListeners(AvailableTargetsUpdateListener listener)
    {
        targetUpdateListeners.add(listener);
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
            inBuffer = new InputBuffer("ipaacaenvironment" + id, categories);
            inBuffer.registerHandler(new IUEventHandler(new JointDataConfigReqHandler(), EnumSet.of(IUEventType.ADDED), ImmutableSet
                    .of("jointDataConfigRequest")));
            inBuffer.registerHandler(new IUEventHandler(new ComponentNotifyHandler(), EnumSet.of(IUEventType.ADDED), ImmutableSet
                    .of("componentNotify")));
            outBuffer = new OutputBuffer("ipaacaenvironment" + id);
        }
        submitNotify(true);        
    }

    private void submitNotify(boolean isNew)
    {
        LocalIU notifyIU = new LocalIU();
        notifyIU.setCategory("componentNotify");
        notifyIU.getPayload().put("name", "IpaacaEmbodiment");
        notifyIU.getPayload().put("function", "realizer");
        notifyIU.getPayload().put("send_categories", "jointDataConfigReply, jointData, componentNotify");
        notifyIU.getPayload().put("recv_categories", "jointDataConfigRequest, componentNotify");
        notifyIU.getPayload().put("state", isNew?"new":"old");
        synchronized (ipaacaLock)
        {
            outBuffer.add(notifyIU);
        }
        log.debug("Notify submitted");
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

    private float[] coordinateRelocate(float m[])
    {
        // x <= z
        // y <= x
        // z <= y
        float res[] = Mat4f.getMat4f();
        Mat4f.setIdentity(res);
        float t[] = Vec3f.getVec3f();
        Mat4f.getTranslation(t, m);
        float x = t[0], y = t[1], z = t[2];
        t[0] = z;
        t[1] = x;
        t[2] = y;
        float q[] = Quat4f.getQuat4f();
        Quat4f.setFromMat4f(q, m);
        x = q[Quat4f.x];
        y = q[Quat4f.y];
        z = q[Quat4f.z];
        q[Quat4f.x] = z;
        q[Quat4f.y] = x;
        q[Quat4f.z] = y;
        Mat4f.setRotation(res, q);
        Mat4f.setTranslation(res, t);
        return res;
    }

    private String getJointMatrices(List<VJoint> jointList)
    {
        StringBuffer buf = new StringBuffer();
        for (VJoint vj : jointList)
        {
            System.out.println("coord relocate for joint " + vj.getSid());
            buf.append(getMatrix(coordinateRelocate(vj.getLocalMatrix())));
            //buf.append(getMatrix(vj.getLocalMatrix()));
            buf.append(" ");

            float m[] = new float[16];
            Mat4f.set(m, vj.getGlobalMatrix());
            float mRes[] = new float[16];
            float q[] = Quat4f.getQuat4f();
            vj.getRotation(q);
            float mQ[] = new float[16];
            Mat4f.setFromTR(mQ, Vec3f.getZero(), q);
            Mat4f.invertRigid(mQ);
            Mat4f.mul(mRes, m, mQ);
            buf.append(getMatrix(coordinateRelocate(mRes)));
            // buf.append(getMatrix(coordinateRelocate(vj.getGlobalMatrix())));
            buf.append(" ");
        }
        return buf.toString().trim();
    }

    public void waitForAvailableJoints()
    {
        synchronized (availableJoints) 
        {
            if(!availableJoints.get().isEmpty())return;            
            try
            {
                availableJoints.wait();
            }
            catch (InterruptedException e)
            {
                Thread.interrupted();
            }
        }
    }
    
    public void setJointData(List<VJoint> jointList, ImmutableMap<String, Float> morphTargets)
    {
        if(availableJoints.get().isEmpty() && availableMorphs.get().isEmpty())
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
        iu.getPayload().put("morph_data", toSpaceSeperatedList(values));
        if (jointList.isEmpty())
        {
            // XXX no jointlist: add dummy joint data to force re-render of face
            setUsed(ImmutableList.of(availableJoints.get().get(0)), usedTargets);
            iu.getPayload().put("joint_data", "1 0 0 0  0 1 0 0  0 0 1 0  0 0 0 1    1 0 0 0  0 1 0 0  0 0 1 0  0 0 0 1");
        }
        else
        {
            setUsed(usedJoints, usedTargets);
            iu.getPayload().put("joint_data", getJointMatrices(jointList));
        }
        synchronized (ipaacaLock)
        {
            outBuffer.add(iu);
        }
    }

    public ImmutableSet<String> getAvailableJoints()
    {
        return ImmutableSet.copyOf(availableJoints.get());
    }

    public ImmutableSet<String> getAvailableMorphs()
    {
        return ImmutableSet.copyOf(availableMorphs.get());
    }

    private String toCommaSeperatedList(List<String> strSet)
    {
        return toSeperatedList(strSet, ",");
    }

    private String toSpaceSeperatedList(List<String> strSet)
    {
        return toSeperatedList(strSet, " ");
    }

    private String toSeperatedList(List<String> strSet, String seperator)
    {
        StringBuffer sBuf = new StringBuffer();
        for (String s : strSet)
        {
            if (sBuf.length() > 0)
            {
                sBuf.append(seperator);
            }
            sBuf.append(s);
        }
        return sBuf.toString();
    }

    private void updateUsedSet()
    {
        LocalMessageIU iuConfig = new LocalMessageIU();
        iuConfig.setCategory("jointDataConfigReply");
        iuConfig.getPayload().put("joints_provided", toCommaSeperatedList(usedJoints));
        List<String> np = new ArrayList<>(availableJoints.get());
        np.removeAll(usedJoints);
        iuConfig.getPayload().put("joints_not_provided", toCommaSeperatedList(np));
        iuConfig.getPayload().put("morphs_provided", toCommaSeperatedList(usedMorphs));
        np = new ArrayList<>(availableMorphs.get());
        np.removeAll(usedMorphs);
        iuConfig.getPayload().put("morphs_not_provided", toCommaSeperatedList(np));
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
        synchronized(availableJoints)
        {
            availableJoints.notifyAll();
        }        
        availableJoints.set(ImmutableList.copyOf(joints));
    }

    private void setAvailableMorphs(String[] morphs)
    {
        availableMorphs.set(ImmutableList.copyOf(morphs));
    }

    class ComponentNotifyHandler implements HandlerFunctor
    {
        @Override
        public void handle(AbstractIU iu, IUEventType type, boolean local)
        {
            if (iu.getPayload().get("state").equals("new"))
            {
                submitNotify(false);
            }
            log.debug("Notified IpaacaEmbodiment");
        }

    }

    class JointDataConfigReqHandler implements HandlerFunctor
    {
        @Override
        public void handle(AbstractIU iu, IUEventType type, boolean local)
        {
            setAvailableJoints(iu.getPayload().get("joints").split("\\s*,\\s*"));
            setAvailableMorphs(iu.getPayload().get("morphs").split("\\s*,\\s*"));
            for (AvailableTargetsUpdateListener l:targetUpdateListeners)
            {
                l.update();
            }
            log.debug("Available joints received");
        }
    }

}
