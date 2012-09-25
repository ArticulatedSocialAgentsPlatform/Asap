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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Interfaces with an Ipaaca graphical environment. Should currently be initialized before that environment itself is initialized.
 * Currently Ipaaca graphcial environments contain only one character.
 * @author hvanwelbergen
 * 
 */
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
    
    static
    {
        Initializer.initializeIpaacaRsb();
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
        ImmutableSet<String> categories = new ImmutableSet.Builder<String>().add("jointDataConfigRequest").build();
        synchronized (ipaacaLock)
        {
            inBuffer = new InputBuffer("ipaacaenvironment" + id, categories);
        }
        JointDataConfigReqHandler eh = new JointDataConfigReqHandler();
        availableJoints.set(new ArrayList<String>());
        availableMorphs.set(new ArrayList<String>());
        inBuffer.registerHandler(new IUEventHandler(eh, EnumSet.of(IUEventType.ADDED), categories));
        while (availableJoints.get().isEmpty())
        {
        }// XXX ugly way to wait for joints to be filled...
        outBuffer = new OutputBuffer("ipaacaenvironment" + id);
    }

    private String getMatrix(float []m)
    {
        StringBuffer buf = new StringBuffer();
        for(float f:m)
        {
            buf.append(f);
            buf.append(" ");
        }
        return buf.toString().trim();
    }
    
    private String getJointMatrices(List<VJoint> jointList)
    {
        StringBuffer buf = new StringBuffer();
        for(VJoint vj:jointList)
        {
            buf.append(getMatrix(vj.getLocalMatrix()));
            buf.append(" ");
            float m[]=vj.getGlobalMatrix();
            float mRes[]=new float[16];
            float q[]=Quat4f.getQuat4f();
            vj.getRotation(q);
            float mQ[]=new float[16];
            Mat4f.setFromTR(mQ, Vec3f.getZero(), q);
            Mat4f.invertRigid(mQ);
            Mat4f.mul(mRes, m,mQ);
            buf.append(getMatrix(mRes));
            buf.append(" ");
        }
        return buf.toString().trim();
    }
    
    public void setJointData(List<VJoint> jointList, ImmutableMap<String, Float> morphTargets)
    {
        List<String> usedTargets = new ArrayList<>();
        List<String> values = new ArrayList<>();
        for(String morph:availableMorphs.get())
        {
            if(morphTargets.keySet().contains(morph))
            {
                usedTargets.add(morph);
                values.add(""+morphTargets.get(morph)*100);
            }
        }
        
        LocalMessageIU iu = new LocalMessageIU();     
        iu.setCategory("jointData");
        iu.getPayload().put("morph_data", toSpaceSeperatedList(values));
        if(jointList.isEmpty())
        {
            //XXX no jointlist: add dummy joint data to force re-render of face
            setUsed(ImmutableList.of(availableJoints.get().get(0)),usedTargets);
            iu.getPayload().put("joint_data", "1 0 0 0  0 1 0 0  0 0 1 0  0 0 0 1    1 0 0 0  0 1 0 0  0 0 1 0  0 0 0 1");
        }
        else
        {
            setUsed(usedJoints,usedTargets);
            iu.getPayload().put("joint_data", getJointMatrices(jointList));
        }
        outBuffer.add(iu);
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
        return toSeperatedList(strSet,",");
    }
    
    private String toSpaceSeperatedList(List<String> strSet)
    {
        return toSeperatedList(strSet," ");
    }
    
    private String toSeperatedList(List<String> strSet, String seperator)
    {
        StringBuffer sBuf = new StringBuffer();
        for (String s:strSet)
        {
            if(sBuf.length()>0)
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
        iuConfig.getPayload().put("joints_provided",toCommaSeperatedList(usedJoints));
        List<String> np = new ArrayList<>(availableJoints.get());
        np.removeAll(usedJoints);
        iuConfig.getPayload().put("joints_not_provided",toCommaSeperatedList(np));
        iuConfig.getPayload().put("morphs_provided",toCommaSeperatedList(usedMorphs));
        np = new ArrayList<>(availableMorphs.get());
        np.removeAll(usedMorphs);
        iuConfig.getPayload().put("morphs_not_provided",toCommaSeperatedList(np));
        outBuffer.add(iuConfig);
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
        if(!usedMorphs.equals(this.usedMorphs))
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
    }

    private void setAvailableMorphs(String[] morphs)
    {
        availableMorphs.set(ImmutableList.copyOf(morphs));
    }

    class JointDataConfigReqHandler implements HandlerFunctor
    {
        @Override
        public void handle(AbstractIU iu, IUEventType type, boolean local)
        {
            setAvailableJoints(iu.getPayload().get("joints").split(","));
            setAvailableMorphs(iu.getPayload().get("morphs").split(","));
        }
    }

}
