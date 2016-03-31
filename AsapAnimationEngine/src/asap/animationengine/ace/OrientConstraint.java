/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.ace;

import hmi.math.Vec3f;
import lombok.Getter;

/**
 * Orientation constraint for wrist rotation
 * @author hvanwelbergen
 *
 */
public class OrientConstraint
{
    private float [] d = Vec3f.getVec3f();  //extended finger direction:
    private float [] p = Vec3f.getVec3f();  //palm normal
    
    public float[] getD()
    {
        return Vec3f.getVec3f(d);
    }
    
    public float[] getP()
    {
        return Vec3f.getVec3f(p);
    }
    
    @Getter private final GStrokePhaseID phase;
    @Getter private final String id;
    
    public OrientConstraint(String id, GStrokePhaseID ph,
            float[] d_dir, float[] p_dir)
    {
        this.id = id;
        phase = ph;
        Vec3f.set(d,d_dir);
        Vec3f.set(p,p_dir);        
    }
    
    public OrientConstraint(String id, GStrokePhaseID ph)
    {        
        this(id, ph,Vec3f.getVec3f(0,0,0), Vec3f.getVec3f(0,0,0));
    }
    
    public void setP(float[]pNew)
    {
        Vec3f.set(p,pNew);
    }
    
    public void setD(float[]dNew)
    {
        Vec3f.set(d,dNew);
    }
    
    
}
