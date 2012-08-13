package asap.animationengine.ace;

import hmi.math.Mat4f;
import hmi.math.Vec3f;
import lombok.Getter;

/**
 * ase class for strokes, which define a trajectory
 * representation consisting of subsequent guiding strokes
 * @author hvanwelbergen
 * @author Stefan Kopp (original C++ version)
 */
public class GuidingStroke
{
    enum GStrokePhaseID
    {
        STP_UNDEF, STP_PREP, STP_STROKE, STP_RETRACT, STP_HOLD, STP_FINISH;
    }

    private float[] endPos = Vec3f.getVec3f();
    private float[] endDir = Vec3f.getVec3f(); // optional
    private double vGain;   // velocity gain factor
    private double stress;  // stress/accentuation
    private double eDt;     // anticipated duration
    private TPConstraint eT; //target/end time
    
    @Getter
    GStrokePhaseID phaseId;
    
    public GuidingStroke()
    {
        phaseId = GStrokePhaseID.STP_PREP;
        vGain = 1;
        stress = 0.5;
        eDt = 0;
    }
    
    public GuidingStroke(GStrokePhaseID phaseId, TPConstraint et, float[]ep, float ed[])
    {
        eT = et;
        endPos = ep;
        endDir = ed;
        this.phaseId = phaseId;
        vGain = 1;
        stress = 0.5;
        eDt = 0;
        
    }    
    
    /**
     * Transforms the boundary constraints with 4x4 matrix m
     */
    public void transform(float []m)
    {
        Mat4f.transformPoint(m, endPos);
        Mat4f.transformVector(m, endDir);        
    }
    
    @Override
    public String toString()
    {
        return "GStroke: -> "+Vec3f.toString(endPos)+"("+eT+"),"+Vec3f.toString(endDir)+"(Phase:"+phaseId+")";
    }
}
