/*******************************************************************************
 *******************************************************************************/
package asap.faceengine.viseme;

import java.util.Collection;

import lombok.Data;

import com.google.common.collect.ImmutableSet;

/**
 * Describes a morph frame
 * @author hvanwelbergen
 * 
 */
@Data
public class MorphVisemeDescription
{
    public MorphVisemeDescription(String[] morphs, float intensity)
    {
        this.intensity = intensity;
        morphNames = ImmutableSet.copyOf(morphs);
    }
    
    public MorphVisemeDescription(Collection<String> morphs, float intensity)
    {
        this.intensity = intensity;
        morphNames = ImmutableSet.copyOf(morphs);
    }

    final ImmutableSet<String> morphNames;
    final float intensity;
}
