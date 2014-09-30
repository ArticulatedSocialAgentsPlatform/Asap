package asap.srnao.robot;

import lombok.Getter;
import lombok.Setter;

/**
 * Stores the state (angle) of a single Nao joint
 * @author davisond
 *
 */
public class NaoJoint {

	@Getter private String id;
	@Getter @Setter private float angle;
	@Getter @Setter private float minAngle;
	@Getter @Setter private float maxAngle;
	
	public NaoJoint(String id)
	{
		this(id, 0f);
	}
	
	public NaoJoint(String id, float angle)
	{
		this.id = id;
		this.angle = angle;
	}
	
}
