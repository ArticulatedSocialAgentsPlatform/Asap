package asap.rsbembodiments.util;

import hmi.animation.VJoint;
import hmi.animation.VJointUtils;
import hmi.math.Quat4f;
import hmi.math.Vec3f;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.primitives.Floats;

import asap.rsbembodiments.Rsbembodiments.Joint;

/**
 * Utilities to convert VJoint structures to/from their protocolbuffer equivalents
 * @author hvanwelbergen
 * 
 */
public final class VJointRsbUtils
{
    private VJointRsbUtils()
    {

    }

    public static VJoint toVJoint(List<Joint> joints)
    {
        Joint rootJoint = joints.get(0);
        VJoint vjRoot = new VJoint(rootJoint.getId(), rootJoint.getId());
        vjRoot.setRotation(Floats.toArray(rootJoint.getLocalRotationList()));
        return vjRoot;
    }
    
    public static List<Joint> toRsbJointList(VJoint root)
    {
        List<Joint> jointList = new ArrayList<asap.rsbembodiments.Rsbembodiments.Joint>();
        for (VJoint vj : root.getParts())
        {
            String id = VJointUtils.getSidNameId(vj);
            Joint.Builder builder = Joint.newBuilder().setId(id);
            if (vj != root)
            {
                builder.setParentId(VJointUtils.getSidNameId(vj.getParent()));
                float[] v = Vec3f.getVec3f();
                vj.getTranslation(v);
                builder.addAllLocalRotation(Floats.asList(v));                
            }
            else
            {
                builder.setParentId("-");
            }
            float q[]=Quat4f.getQuat4f();
            vj.getRotation(q);
            builder.addAllLocalRotation(Floats.asList(q));
            jointList.add(builder.build());
        }
        return jointList;
    }
}
