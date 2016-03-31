/*******************************************************************************
 *******************************************************************************/
package asap.livemocapengine.inputs;

import hmi.faceembodiments.AUConfig;
import hmi.faceembodiments.Side;
import hmi.math.Vec3f;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;


/**
 * Allows control of pitch and yaw using the arrow keys
 * @author Herwin
 * 
 */
public class KeyboardInput implements EulerInput, FACSFaceInput, PositionInput
{
    private float pitch = 0;
    private float yaw = 0;
    private float roll = 0;
    private float au1lval = 0;
    private float au1rval = 0;
    private float pos[] = new float[3];

    private String id = "";
    private boolean keysPressed[] = new boolean[KeyEvent.KEY_LAST];

    private static final float YAW_TICK = 0.5f;
    private static final float PITCH_TICK = 0.5f;
    private static final float AU_TICK = 0.05f;
    private static final float POS_TICK = 0.01f;

    private class MyDispatcher implements KeyEventDispatcher
    {
        @Override
        public boolean dispatchKeyEvent(KeyEvent e)
        {
            if (e.getID() == KeyEvent.KEY_PRESSED)
            {
                keyPressed(e);
            }
            else if (e.getID() == KeyEvent.KEY_RELEASED)
            {
                keyReleased(e);
            }
            return false;
        }
    }

    public KeyboardInput()
    {
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new MyDispatcher());
    }

    public KeyboardInput(String id)
    {
        this();
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

    public void keyPressed(KeyEvent e)
    {
        keysPressed[e.getKeyCode()] = true;

        if (keysPressed[KeyEvent.VK_LEFT]) yaw += YAW_TICK;
        if (keysPressed[KeyEvent.VK_RIGHT]) yaw -= YAW_TICK;
        if (keysPressed[KeyEvent.VK_DOWN]) pitch += PITCH_TICK;
        if (keysPressed[KeyEvent.VK_UP]) pitch -= PITCH_TICK;
        if (keysPressed[KeyEvent.VK_O]) au1lval += AU_TICK;
        if (keysPressed[KeyEvent.VK_K]) au1lval -= AU_TICK;
        if (keysPressed[KeyEvent.VK_P]) au1rval += AU_TICK;
        if (keysPressed[KeyEvent.VK_L]) au1rval -= AU_TICK;
        if (keysPressed[KeyEvent.VK_I]) pos[1] += POS_TICK;
        if (keysPressed[KeyEvent.VK_K]) pos[1] -= POS_TICK;
        if (keysPressed[KeyEvent.VK_L]) pos[0] += POS_TICK;
        if (keysPressed[KeyEvent.VK_J]) pos[0] -= POS_TICK;
        pos[2] = 3;

        if (au1lval < 0) au1lval = 0;
        if (au1lval > 1) au1lval = 1;
        if (au1rval < 0) au1rval = 0;
        if (au1rval > 1) au1rval = 1;
        if (yaw > 180) yaw = 180;
        if (yaw < -180) yaw = -180;
        if (pitch > 180) pitch = 180;
        if (pitch < -180) pitch = -180;
        System.out.println("Pitch: " + pitch + " Yaw: " + yaw + " AU1L :" + au1lval + " AU1R " + au1rval);
        System.out.println("Pos: " + Vec3f.toString(pos));
    }

    public void keyReleased(KeyEvent e)
    {
        keysPressed[e.getKeyCode()] = false;
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public AUConfig[] getAUConfigs()
    {
        AUConfig[] conf = new AUConfig[2];
        conf[0] = new AUConfig(Side.LEFT, 1, au1lval);
        conf[1] = new AUConfig(Side.RIGHT, 1, au1rval);
        return conf;
    }

    @Override
    public float[] getPosition()
    {
        return Vec3f.getVec3f(pos);
    }
}
