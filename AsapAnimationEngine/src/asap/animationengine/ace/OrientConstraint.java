package asap.animationengine.ace;

import lombok.Getter;
import lombok.Setter;
import hmi.math.Vec3f;

/**
 * Orientation constraint for wrist rotation
 * @author hvanwelbergen
 *
 */
public class OrientConstraint
{
    @Getter private float [] d = Vec3f.getVec3f();  //extended finger direction:
    @Getter private float [] p = Vec3f.getVec3f();  //palm normal
    @Getter @Setter private GStrokePhaseID phase;
    @Getter private final String id;
    
    public OrientConstraint(String id)
    {
        this.id = id;
        Vec3f.set(d,0,0,0);
        Vec3f.set(p,0,0,0);
    }
    
    public void setP(float[]pNew)
    {
        Vec3f.set(p,pNew);
    }
    
    public void setD(float[]dNew)
    {
        Vec3f.set(d,dNew);
    }
    
    public OrientConstraint(String id, GStrokePhaseID ph,
            float[] d_dir, float[] p_dir)
    {
        this.id = id;
        phase = ph;
        Vec3f.set(d,d_dir);
        Vec3f.set(p,p_dir);        
    }
}
