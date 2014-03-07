package asap.rsbembodiments.util;

import hmi.animation.VJoint;
import hmi.animation.VJointUtils;
import hmi.math.Quat4f;
import hmi.math.Vec3f;

import java.util.ArrayList;
import java.util.List;

import asap.rsbembodiments.Rsbembodiments.Joint;

import com.google.common.primitives.Floats;

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

    private static VJoint createVJoint(Joint j)
    {
        VJoint vj = new VJoint(j.getId(), j.getId());
        vj.setRotation(Floats.toArray(j.getLocalRotationList()));
        vj.setTranslation(Floats.toArray(j.getLocalTranslationList()));
        return vj;
    }

    private static void setupChildren(VJoint parent, List<Joint> joints)
    {
        for (Joint j : joints)
        {
            if (j.getParentId().equals(parent.getId()))
            {
                VJoint vj = createVJoint(j);
                parent.addChild(vj);
                setupChildren(vj, joints);
            }
        }
    }

    public static VJoint toVJoint(List<Joint> joints)
    {
        Joint rootJoint = joints.get(0);
        VJoint vjRoot = new VJoint(rootJoint.getId(), rootJoint.getId());
        vjRoot.setRotation(Floats.toArray(rootJoint.getLocalRotationList()));
        setupChildren(vjRoot, joints);
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
                builder.addAllLocalTranslation(Floats.asList(v));
            }
            else
            {
                builder.setParentId("-");
                builder.addAllLocalTranslation(Floats.asList(0,0,0));
            }
            float q[] = Quat4f.getQuat4f();
            vj.getRotation(q);
            builder.addAllLocalRotation(Floats.asList(q));
            jointList.add(builder.build());
        }
        return jointList;
    }
}
