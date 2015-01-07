/*******************************************************************************
 *******************************************************************************/
package hmi.jnaoqiembodiment;

import hmi.faceembodiments.AUConfig;
import hmi.faceembodiments.FACSFaceEmbodiment;
import hmi.faceembodiments.Side;

import com.aldebaran.proxy.ALLedsProxy;


/**
 * 
 * @author welberge
 */
public class NaoFaceEmbodiment implements FACSFaceEmbodiment
{
    private final ALLedsProxy ledsProxy;
    private final String id;
    
    @Override
    public String getId()
    {
        return id;
    }
    
    public NaoFaceEmbodiment(String id, ALLedsProxy ledsProxy)
    {
        this.id = id;
        this.ledsProxy = ledsProxy;
        disableAllEyeLeds();
    }

    private void disableAllEyeLeds(String color)
    {
        for (int angle = 0; angle < 360; angle += 45)
        {
            ledsProxy.setIntensity("Face/Led/" + color + "/Left/" + angle + "Deg/Actuator/Value", 0);
            ledsProxy.setIntensity("Face/Led/" + color + "/Right/" + angle + "Deg/Actuator/Value", 0);
        }
    }

    public void disableAllEyeLeds()
    {
        disableAllEyeLeds("Red");
        disableAllEyeLeds("Green");
        disableAllEyeLeds("Blue");
    }

    private void setFacs1(String side, float intensity)
    {
        ledsProxy.setIntensity("Face/Led/Red/"+side+"/0Deg/Actuator/Value", intensity);
        ledsProxy.setIntensity("Face/Led/Red/"+side+"/315Deg/Actuator/Value", intensity);
        ledsProxy.setIntensity("Face/Led/Red/"+side+"/45Deg/Actuator/Value", intensity);
        
    }
        
    private void setFacs1(Side side, float intensity)
    {
        switch (side)
        {
        case LEFT: 
            setFacs1("Left",intensity);
            break;
        case RIGHT:
            setFacs1("Right",intensity);
            break;
        default:
            setFacs1("Left",intensity);
            setFacs1("Right",intensity);
            break;
        }        
    }
    
    @Override
    public void setAUs(AUConfig... configs)
    {
        for (AUConfig config : configs)
        {
            if (config != null && config.getAu() == 1)
            {
                setFacs1(config.getSide(),config.getValue());
            }
        }
    }

    public void shutdown()
    {
        disableAllEyeLeds();
    }

    
}
