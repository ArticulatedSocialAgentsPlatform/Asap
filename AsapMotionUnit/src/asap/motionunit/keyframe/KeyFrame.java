package asap.motionunit.keyframe;

import java.util.Arrays;

public class KeyFrame
{
    private double ftime;
    private float dofs[];
    
    public KeyFrame(double ftime, float dofs[])
    {
        this.ftime = ftime;
        this.dofs = Arrays.copyOf(dofs, dofs.length);
    }
    
    public float [] getDofs()
    {
        return Arrays.copyOf(dofs, dofs.length);
    }
    
    public double getFrameTime()
    {
        return ftime;
    }
    
}
