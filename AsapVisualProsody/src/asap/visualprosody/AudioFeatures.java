package asap.visualprosody;

import lombok.Data;

@Data
public class AudioFeatures
{
    private final double[] f0;
    private final double[] rmsEnergy;
    private final double frameDuration;    
}
