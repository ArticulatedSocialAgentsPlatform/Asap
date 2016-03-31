package asap.srnao.robot;

/**
 * Interface for robot objects, provides access to joint states
 * @author davisond
 *
 */
public interface Robot {

	/**
	 * Returns the most recent state of the specified joint. No guarantees can be made on the correctness and actuality of the joint states.
	 * This function returns null if the joint is not found
	 * @param id the name of the joint
	 * @return the joint state, or null if id is not found
	 */
	public NaoJoint getJointState(String id);
	public void updateJointState(String id, NaoJoint naoJoint);
	
}
