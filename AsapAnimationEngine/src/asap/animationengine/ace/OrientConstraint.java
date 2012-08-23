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
    @Getter private double tp;
    @Getter @Setter private double t;
    @Getter @Setter private GStrokePhaseID phase;
    @Getter private boolean def;
    
    public OrientConstraint()
    {
        Vec3f.set(d,0,0,0);
        Vec3f.set(p,0,0,0);        
        t  = -1;
        tp = -1;
        def = false;
    }
    
    public void setP(float[]pNew)
    {
        Vec3f.set(p,pNew);
    }
    
    public void setD(float[]dNew)
    {
        Vec3f.set(d,dNew);
    }
    
    public OrientConstraint(GStrokePhaseID ph, double ti,
            float[] d_dir, float[] p_dir)
    {
        phase = ph;
        t = ti;
        tp = -1;
        Vec3f.set(d,d_dir);
        Vec3f.set(p,p_dir);
        def = true;
    }
}
