package asap.srnao.robot;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation for the Nao robot, provides access to up-to-date joint states
 * @author davisond
 *
 */
public class NaoRobot implements Robot {

	private Map<String,NaoJoint> naoJoints = new HashMap<String,NaoJoint>();
	
	@Override
	public NaoJoint getJointState(String id) {
		return (naoJoints.get(id));
	}

	@Override
	public void updateJointState(String id, NaoJoint naoJoint) {
		naoJoints.put(id, naoJoint);
	}
	
	public String toString()
	{
		String ret = "";
		
		for(NaoJoint joint : naoJoints.values())
		{
			ret += "Jointname: "+joint.getId()+" - Angle: "+joint.getAngle()+"\r\n";
		}
		
		return ret;
	}

}
