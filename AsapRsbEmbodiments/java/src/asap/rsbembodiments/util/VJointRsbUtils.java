package asap.rsbembodiments.util;

import hmi.animation.VJoint;
import hmi.animation.VJointUtils;
import hmi.math.Mat4f;
import hmi.math.Quat4f;
import hmi.math.Vec3f;
import asap.rsbembodiments.Rsbembodiments.Skeleton;

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

    private static VJoint createVJoint(int i, Skeleton skeleton)
    {
        VJoint vj = new VJoint(skeleton.getJoints(i), skeleton.getJoints(i));
        float transformations[] = Floats.toArray(skeleton.getLocalTransformationList());
        vj.setLocalTransform(transformations, i*16);
        return vj;
    }

    private static void setupChildren(VJoint parent, Skeleton skeleton)
    {
        for (int i = 0; i < skeleton.getJointsCount(); i++)
        {
            if (skeleton.getParents(i).equals(parent.getId()))
            {
                VJoint vj = createVJoint(i, skeleton);
                parent.addChild(vj);
                setupChildren(vj, skeleton);
            }
        }
    }

    private static int findRoot(Skeleton skeleton)
    {
        for (int i = 0; i < skeleton.getJointsCount(); i++)
        {
            if (!skeleton.getJointsList().contains(skeleton.getParents(i)))
            {
                return i;
            }
        }
        return -1;
    }

    public static VJoint toVJoint(Skeleton skeleton)
    {
        int rootIndex = findRoot(skeleton);
        VJoint vjRoot = new VJoint(skeleton.getJoints(rootIndex), skeleton.getJoints(rootIndex));
        float transformations[] = Floats.toArray(skeleton.getLocalTransformationList());
        float q[] = Quat4f.getQuat4f();
        Quat4f.setFromMat4f(q, 0, transformations, rootIndex*16);
        vjRoot.setRotation(q);
        setupChildren(vjRoot, skeleton);
        return vjRoot;
    }

    public static Skeleton toRsbSkeleton(VJoint root)
    {
        Skeleton.Builder sBuilder = Skeleton.newBuilder();
        for (VJoint vj : root.getParts())
        {
            String id = VJointUtils.getSidNameId(vj);
            sBuilder.addJoints(id);
            float[] v = Vec3f.getVec3f(0, 0, 0);
            if (vj != root)
            {
                sBuilder.addParents(VJointUtils.getSidNameId(vj.getParent()));
                vj.getTranslation(v);
            }
            else
            {
                sBuilder.addParents("root");
            }
            float m[] = Mat4f.getMat4f();
            float q[] = Quat4f.getQuat4f();
            vj.getRotation(q);
            Mat4f.setFromTR(m, v, q);
            sBuilder.addAllLocalTransformation(Floats.asList(m));
        }
        return sBuilder.build();
    }
}
