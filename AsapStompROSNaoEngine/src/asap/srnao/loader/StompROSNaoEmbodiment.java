/*******************************************************************************
 * 
 * Copyright (C) 2009 Human Media Interaction, University of Twente, the Netherlands
 * 
 * This file is part of the Elckerlyc BML realizer.
 * 
 * Elckerlyc is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Elckerlyc is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Elckerlyc.  If not, see http://www.gnu.org/licenses/.
 ******************************************************************************/
package asap.srnao.loader;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

import lombok.Getter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pk.aamir.stompj.Connection;
import pk.aamir.stompj.Message;
import pk.aamir.stompj.MessageHandler;
import pk.aamir.stompj.StompJException;
import hmi.environmentbase.Embodiment;
import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.xml.XMLTokenizer;
import asap.srnao.NaoPlanner;
import asap.srnao.robot.NaoJoint;
import asap.srnao.robot.NaoRobot;
import asap.srnao.robot.Robot;

/**
*  This embodiment initiates the Stomp connection used to send messages to ROS
*  The various control primitives are mapped to corresponding Stomp messages
*/
public class StompROSNaoEmbodiment implements Embodiment, EmbodimentLoader
{
    private String id = "";
	private Connection con;
	private boolean clipRunning = false;
    private static Logger logger = LoggerFactory.getLogger(NaoPlanner.class.getName());

    //Command used to instruct the bridge to forward messages on a topic from one publisher to another
    private static final String BRIDGE_CONFIG = "<config>\r\n" + 
    		"    <relay publisher=\"%s\" name=\"%s\" />\r\n" + 
    		"</config>\r\n" + 
    		"";
    
	//apparantly a status of "3" corresponds to a completed behavior, as observed in test cases
    private static final int STATUS_BEHAVIOR_COMPLETE = 3;
    
    @Getter private Robot nao = new NaoRobot();
    
    public void setId(String id)
    {
        this.id = id;
    }

    @Override
    public String getId()
    {
        return id;
    }

    /**
     * Connects to and registers some producers and consumers for the various topics made available by the ROS Bridge
     */
    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments, Loader ... requiredLoaders) 
    	throws IOException
    {
        setId(loaderId);
        
        try {
            con = new Connection("127.0.0.1", 61613, "admin", "password");
			con.connect();
		} catch (StompJException e) {
			logger.error("Error while initialising STOMP connection: "+e.getMessage());
			e.printStackTrace();
		}
    
    	//currently the ros bridge will make duplicate subscriptions each time this config is called, so the bridge must check for this or there will be an overwhelming increase in the amount of messages
		//now that the connection is made we need to tell the Apollo - ROS bridge which topics we want to subscribe and listen to
    	con.send(String.format(BRIDGE_CONFIG,"apollo","run_behavior/goal"), "/topic/test_bridge_config");
    	con.send(String.format(BRIDGE_CONFIG,"apollo","joint_angles"), "/topic/test_bridge_config");

    	con.send(String.format(BRIDGE_CONFIG,"ros","joint_states"), "/topic/test_bridge_config");
    	con.send(String.format(BRIDGE_CONFIG,"ros","run_behavior/result"), "/topic/test_bridge_config");
    	
    	//we want to maintain an up-to-date robot representation locally, so that we can give appropriate feedback to the ASAP planners when necessary
    	con.subscribe("/topic/test_joint_states", true);
    	con.addMessageHandler("/topic/test_joint_states", new MessageHandler() {
    	    public void onMessage(Message msg) {
    	    	//System.out.println(msg.getContentAsString());
    	    	XMLTokenizer tokenizer = new XMLTokenizer(msg.getContentAsString());
    	    	
    	    	//Joint angles are sent as two lists: <name><e>HeadYaw</e><e>...</e></name><position><e>0.5</e><e>...</e></position>
    	    	//In order to store these values appropriately we need to read both lists completely
    	    	Deque<String> names = new ArrayDeque<String>();
    	    	Deque<Float> angles = new ArrayDeque<Float>();

	    		try {
	    			tokenizer.takeSTag("data");
	    			while(!tokenizer.atETag("data"))
	    			{
		    			if(tokenizer.atSTag("name"))
		    	    	{
	    	    			tokenizer.takeSTag("name");
							while(tokenizer.atSTag("e"))
							{
								names.push(tokenizer.takeTextElement("e"));
							}
							tokenizer.takeETag("name");
		    	    	}
		    			else if(tokenizer.atSTag("position"))
		    	    	{
	    	    			tokenizer.takeSTag("position");
							while(tokenizer.atSTag("e"))
							{
								angles.push(tokenizer.takeFloatElement("e"));
							}
							tokenizer.takeETag("position");
		    	    	}
		    			else
		    			{
		    				tokenizer.skipTag();
		    			}
	    			}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    		
	    		//now we can re-populate the robot representation object with the correct joint angles
	    		while(!names.isEmpty() && !angles.isEmpty())
	    		{
	    			String name = names.pop();
	    			float angle = angles.pop();
	    			nao.updateJointState(name, new NaoJoint(name, angle));
	    		}
	    		
	    		//System.out.println(nao.toString());
    	    }
    	});
    	
    	//there is not much feedback available for running behaviors, except a simple status update once the behavior is finished
    	con.subscribe("/topic/test_run_behavior.result", true);
    	con.addMessageHandler("/topic/test_run_behavior.result", new MessageHandler() {
    	    public void onMessage(Message msg) {
    	    	XMLTokenizer tokenizer = new XMLTokenizer(msg.getContentAsString());
				try {
					while(tokenizer.recoverAtSTag("status") && !"int".equals(tokenizer.getAttribute("type")))
					{
						//just consume all the xml until we reach the desired block: <status type="int">
						tokenizer.takeSTag();
					}
					
					//we are now either at the end of the file or we have our desired xml block
					if(tokenizer.atSTag("status"))
					{
						if(tokenizer.takeIntElement("status") == STATUS_BEHAVIOR_COMPLETE)
						{
							clipRunning = false;
						}
					}
					else
					{
						logger.error("Error while parsing status of completed behavior: status tag not found");
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    	    }
    	});
	    	
        
        return;
    }

    @Override
    public void unload()
    {
    	con.disconnect();
    }

    @Override
    public Embodiment getEmbodiment()
    {
        return this;
    }
    
    /**
     * Returns whether or not a clip is currently running.
     * This function returns true after runChoregrapheClip() is called, and will only return false after a status update message is recieved from ROS, indicating that the clip has finished.
     * @return
     */
	public boolean isClipRunning() {
		return clipRunning;
	}
    
    /**
     * this embodiment exposes as control primitive the option to run a named choregraphe animation that has been stored in the nao.
     * To execute this primitve, the embodiment sends an appropriate message over the stomrosnao messageing architecture
     * 
     * @param behaviorName
     */
    public void runChoregrapheClip(String clipName)
    {
    	if(!clipRunning)
    	{
	    	clipRunning = true;
	    	String msg = String.format("<data>\r\n" + 
	    			"  <header type=\"dict\">\r\n" + 
	    			"    <stamp type=\"time\">0 0</stamp>\r\n" + 
	    			"    <frame_id type=\"str\" />\r\n" + 
	    			"    <seq type=\"int\">0</seq>\r\n" + 
	    			"  </header>\r\n" + 
	    			"  <goal_id type=\"dict\">\r\n" + 
	    			"    <stamp type=\"time\">0 0</stamp>\r\n" + 
	    			"    <id type=\"str\" />\r\n" + 
	    			"  </goal_id>\r\n" + 
	    			"  <goal type=\"dict\">\r\n" + 
	    			"    <behavior type=\"str\">%s</behavior>\r\n" + 
	    			"  </goal>\r\n" + 
	    			"</data>",clipName);
	    	//System.out.println(msg);
	    	con.send(msg, "/topic/test_run_behavior.goal");
    	}
    }
    
    /**
     * This control primitive can be used to set a certain joint to a specified angle (in radians).
     * Joint names and acceptable angle ranges can be found on the nao api page: https://community.aldebaran.com/doc/1-14/naoqi/motion/control-joint.html
     * The embodiment sends a message over the Stomp-ROS-Nao messaging bridge
     * @param jointName the name of the joint to be set, this should match a joint as specified in the Nao API
     * @param angle the target angle of the joint in radians, should be within the specified min-max range as stated in the Nao API
     * @param speed specify how fast the joint should move from current angle to target angle. Use value between 0 (slow) and 1 (fast)
     */
    public void setJointAngle(String jointName, float angle, float speed)
    {
    	String msg = String.format("<data>\r\n" + 
    			"  <relative type=\"int\">0</relative>\r\n" + 
    			"  <header type=\"dict\">\r\n" + 
    			"    <stamp type=\"time\">0 0</stamp>\r\n" + 
    			"    <frame_id type=\"str\" />\r\n" + 
    			"    <seq type=\"int\">0</seq>\r\n" + 
    			"  </header>\r\n" + 
    			"  <joint_names type=\"tuple\">\r\n" + 
    			"    <e type=\"str\">%s</e>\r\n" + 
    			"  </joint_names>\r\n" + 
    			"  <joint_angles type=\"tuple\">\r\n" + 
    			"    <e type=\"float\">%f</e>\r\n" + 
    			"  </joint_angles>\r\n" + 
    			"  <speed type=\"float\">%f</speed>\r\n" + 
    			"</data>",jointName, angle, speed);
    	//System.out.println(msg);
    	con.send(msg, "/topic/test_joint_angles");
    }

}
