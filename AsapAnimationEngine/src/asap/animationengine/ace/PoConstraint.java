/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.ace;

import lombok.Data;
import lombok.Getter;

/**
 * holds a palm orientation and the corresponding phase
 * @author hvanwelbergen
 *
 */
@Data
public class PoConstraint
{
    @Getter
    private final double po;
    @Getter
    private final GStrokePhaseID phase;
    @Getter private final String id;
}
