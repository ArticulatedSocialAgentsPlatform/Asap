package asap.animationengine.mixed;

import java.util.Collection;
import java.util.List;

import hmi.animation.Hanim;
import hmi.animation.VJoint;
import hmi.math.Vec3f;
import hmi.physics.PhysicalHumanoid;
import hmi.physics.PhysicalSegment;
import hmi.physics.inversedynamics.IDSegment;

public class MixedSystemGenerator
{
    //private static final Logger logger = LoggerFactory.getLogger(MixedSystemGenerator.class.getName());
    private final PhysicalHumanoid fullBodyPh;
    private final float gravity[];    
    
    public MixedSystemGenerator(PhysicalHumanoid fbPh, float g[])
    {
        fullBodyPh = fbPh;
        gravity = g;        
    }
    
    private void addPhysicalSegments(String jointName, PhysicalHumanoid pHuman, VJoint human)
    {
        List<VJoint> path = human.getPath(human.getPart(jointName));
        for(VJoint vj:path)
        {
            if(pHuman.getSegment(vj.getSid())==null)
            {
                PhysicalSegment ps = fullBodyPh.getSegment(vj.getSid());
                if(ps!=null)
                {
                    PhysicalSegment newSeg = fullBodyPh.createSegment(pHuman.getId()+"-"+ps.getSid(),ps.getSid());
                    newSeg.set(ps);
                    pHuman.addSegment(newSeg);
                }
            }
        }  
    }
    
    private PhysicalSegment getParent(PhysicalSegment ph, VJoint human)
    {
        VJoint vj = human.getPart(ph.getSid());
        VJoint parent = vj.getParent();
        while(fullBodyPh.getSegment(parent.getSid())==null)
        {
            parent = parent.getParent();            
        }
        return fullBodyPh.getSegment(parent.getSid());
    }
    
    public MixedSystem generateMixedSystem(String phId, Collection<String> requiredJoints, Collection<String>desiredJoints,VJoint human)
    {
        PhysicalHumanoid pHuman = fullBodyPh.createNew(phId);
        PhysicalSegment rootSegment = fullBodyPh.createSegment(phId+"-HumanoidRoot", Hanim.HumanoidRoot);
        rootSegment.set(fullBodyPh.getRootSegment());
        pHuman.addRootSegment(rootSegment);
        
        for(String jointName:requiredJoints)
        {
            addPhysicalSegments(jointName, pHuman, human);
        }        
        for(String jointName:desiredJoints)
        {
            addPhysicalSegments(jointName, pHuman, human);
        }
        
        //hook up physical joints
        for(PhysicalSegment ps:pHuman.getSegments())
        {
            if(ps.equals(rootSegment))continue;
            PhysicalSegment parent = getParent(ps,human);
            //PhysicalSegment ps1 = fullBodyPh.getSegment(ps.getSid());
            float center[] = Vec3f.getVec3f();
            ps.getTranslation(center);
            Vec3f.add(center, ps.startJointOffset);
            pHuman.setupJoint(ps.getSid(),parent,ps,center);
        }        
        MixedSystem ms = new MixedSystem(gravity,pHuman);
        
        //find Branches
        
        
        //create IDBranches containing IDSegments for all remaining physical segments
        for(PhysicalSegment ps:fullBodyPh.getSegments())
        {
            if(pHuman.getSegment(ps.getSid())!=null)continue;
            IDSegment idSeg = new IDSegment();
            idSeg.mass = ps.box.getMass();
            
            float jointPos[] = Vec3f.getVec3f();
            human.getPart(ps.getSid()).getPathTranslation(human.getParent(), jointPos);
            ps.getTranslation(idSeg.com);
            Vec3f.sub(idSeg.com, jointPos);
            
            ps.box.getInertiaTensor(idSeg.I);
            
            idSeg.name = ps.getSid();            
            
            //TODO: create IDBranches, set idseg translations
            //IDBranch idBranch = new IDBranch();
        }
        return ms;
    }
}
