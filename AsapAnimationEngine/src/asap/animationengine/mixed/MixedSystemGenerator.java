/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.mixed;

import hmi.animation.Hanim;
import hmi.animation.VJoint;
import hmi.math.Vec3f;
import hmi.physics.PhysicalHumanoid;
import hmi.physics.PhysicalSegment;
import hmi.physics.assembler.IDBranchAssembler;
import hmi.physics.assembler.IDSegmentAssembler;
import hmi.physics.assembler.MixedSystemAssembler;
import hmi.physics.assembler.PhysicalHumanoidAssembler;
import hmi.physics.assembler.PhysicalSegmentAssembler;
import hmi.physics.inversedynamics.IDBranch;
import hmi.physics.inversedynamics.IDSegment;
import hmi.physics.mixed.MixedSystem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Can generate a mixed system given a list of required and desired physical joints
 * @author Herwin
 *
 */
public class MixedSystemGenerator
{
    private final PhysicalHumanoid fullBodyPh;
    private final float gravity[]=new float[3];    
    
    public MixedSystemGenerator(PhysicalHumanoid fbPh, float g[])
    {
        fullBodyPh = fbPh;
        Vec3f.set(gravity,g);        
    }
    
    private void addPhysicalSegments(String jointName, PhysicalHumanoid pHuman, VJoint human,PhysicalHumanoidAssembler pha)
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
                    PhysicalSegmentAssembler psa = new PhysicalSegmentAssembler(human, pHuman, ps);
                    newSeg.set(ps);
                    psa.startJoint = human.getPartById(ps.getSid());
                    psa.endJoints = findEndJoints(human.getPartBySid(ps.getSid()));
                    pha.addPhysicalSegmentAssembler(psa);                    
                }
            }
        }  
    }
    
    private PhysicalSegment getParent(PhysicalSegment ps, VJoint human)
    {
        return getParent(ps, fullBodyPh, human);        
    }
    
    private PhysicalSegment getParent(PhysicalSegment ps, PhysicalHumanoid ph, VJoint human)
    {
        VJoint vj = human.getPart(ps.getSid());
        VJoint parent = vj.getParent();
        while(ph.getSegment(parent.getSid())==null)
        {
            parent = parent.getParent();            
        }
        return ph.getSegment(parent.getSid());
    }
    
    private List<String> findPathToPhuman(PhysicalSegment ps, PhysicalHumanoid ph,VJoint human)
    {
        List<String> path = new ArrayList<String>();
        PhysicalSegment pIter = ps;                
        while(ph.getSegment(pIter.getSid())==null)
        {
            path.add(0,pIter.getSid());
            pIter = getParent(pIter,human);
        }
        return path;
    }
    
    private List<VJoint> findEndJoints(VJoint vj)
    {
        List<VJoint> endJoints = new ArrayList<VJoint>();
        for(VJoint vjChild:vj.getChildren())
        {
            if(fullBodyPh.getSegment(vjChild.getSid())!=null)
            {
                endJoints.add(vjChild);
            }
            else
            {
                endJoints.addAll(findEndJoints(vjChild));
            }
        }
        return endJoints;
    }
    
    public MixedSystem generateMixedSystem(String phId, Collection<String> requiredJoints, Collection<String>desiredJoints,VJoint human)
    {
        PhysicalHumanoid pHuman = fullBodyPh.createNew(phId);        
        MixedSystem ms = new MixedSystem(gravity,pHuman);
        if(requiredJoints.isEmpty() && desiredJoints.isEmpty())return ms;
        
        MixedSystemAssembler msa = new MixedSystemAssembler(human,pHuman,ms);        
        
        PhysicalHumanoidAssembler pha = msa.pha;        
        PhysicalSegment rootSegment = fullBodyPh.createSegment(phId+"-HumanoidRoot", Hanim.HumanoidRoot);
        PhysicalSegmentAssembler psaRoot = new PhysicalSegmentAssembler(human, pHuman, rootSegment);
        psaRoot.setRoot(true);        
        rootSegment.set(fullBodyPh.getRootSegment());
        psaRoot.startJoint = human.getPartBySid(Hanim.HumanoidRoot);
        psaRoot.endJoints = findEndJoints(human.getPartBySid(Hanim.HumanoidRoot));
        pha.setRootSegmentAssembler(psaRoot);
        
        for(String jointName:requiredJoints)
        {
            addPhysicalSegments(jointName, pHuman, human,pha);
        }        
        for(String jointName:desiredJoints)
        {
            addPhysicalSegments(jointName, pHuman, human,pha);
        }
        pha.setupJoints(Hanim.HumanoidRoot);
        
        
        List<List<String>> branches = new ArrayList<List<String>>();        
        
        //find Branches
        for(PhysicalSegment ps:fullBodyPh.getSegments())
        {
            if(pHuman.getSegment(ps.getSid())!=null)continue;   //segment is in pHuman
            List<String> path = findPathToPhuman(ps, pHuman, human);
            
            boolean insert = true;
            for(List<String> branch:branches)
            {
                if(branch.containsAll(path))
                {
                    insert = false;
                    break;
                }
                else if(path.containsAll(branch))
                {
                    branch.clear();
                    branch.addAll(path);
                    insert = false;
                    break;
                }
            }
            if(insert)branches.add(path);
        }
        
        for(List<String> branch:branches)
        {
            IDBranch b = new IDBranch();
            IDBranchAssembler ba = new IDBranchAssembler(human,b);
            
            IDSegment psRoot = new IDSegment();
            IDSegmentAssembler psaIDRoot = new IDSegmentAssembler(human,psRoot);
            psaIDRoot.setRoot(true);            
            psaIDRoot.createFromPhysicalSegment(fullBodyPh.getSegment(branch.get(0)),findEndJoints(human.getPartBySid(branch.get(0))));
            
            ba.setRootSegmentAssembler(psaIDRoot);            
            branch.remove(0);
            
            for(String idseg:branch)
            {
                IDSegment ps = new IDSegment();
                IDSegmentAssembler psa = new IDSegmentAssembler(human,ps);
                psa.createFromPhysicalSegment(fullBodyPh.getSegment(idseg),findEndJoints(human.getPartBySid(idseg)));
                ba.addPhysicalSegmentAssembler(psa);
            }
            msa.addBranchAssembler(ba);
        }
        msa.setup();
        return ms;
    }
}
