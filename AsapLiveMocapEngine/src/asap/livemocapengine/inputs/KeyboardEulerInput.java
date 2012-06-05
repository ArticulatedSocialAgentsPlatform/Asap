package asap.livemocapengine.inputs;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Allows control of pitch and yaw using the arrow keys
 * @author Herwin
 *
 */
public class KeyboardEulerInput implements EulerInput, KeyListener
{
    private float pitch = 0;
    private float yaw = 0;
    private float roll = 0;
    private String id="";
    private boolean keysPressed[]=new boolean[KeyEvent.KEY_LAST];
    
    private static final float YAW_TICK = 0.5f;
    private static final float PITCH_TICK = 0.5f;

    public KeyboardEulerInput()
    {
        
    }
    
    public KeyboardEulerInput(String id)
    {
        this.id = id;
    }
    
    @Override
    public float getPitchDegrees()
    {
        return pitch;
    }

    @Override
    public float getYawDegrees()
    {
        return yaw;
    }

    @Override
    public float getRollDegrees()
    {
        return roll;
    }

    @Override
    public void keyTyped(KeyEvent e)
    {

    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        keysPressed[e.getKeyCode()] = true;
        
        if (keysPressed[KeyEvent.VK_LEFT]) yaw += YAW_TICK;
        if (keysPressed[KeyEvent.VK_RIGHT]) yaw -= YAW_TICK;
        if (keysPressed[KeyEvent.VK_DOWN]) pitch -= PITCH_TICK;
        if (keysPressed[KeyEvent.VK_UP]) pitch += PITCH_TICK;
        
        if (yaw > 180) yaw = 180;
        if (yaw < -180) yaw = -180;
        if (pitch > 180) pitch = 180;
        if (pitch < -180) pitch = -180;
        System.out.println("Pitch: "+pitch+" Yaw: "+yaw);
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
        keysPressed[e.getKeyCode()] = false;
    }

    @Override
    public String getId()
    {
        return id;
    }
}
