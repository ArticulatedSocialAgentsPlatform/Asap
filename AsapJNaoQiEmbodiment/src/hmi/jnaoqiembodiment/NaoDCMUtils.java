package hmi.jnaoqiembodiment;

import com.aldebaran.proxy.DCMProxy;
import com.aldebaran.proxy.Variant;

/**
 * Utility class for DCM
 * @author welberge
 */
public final class NaoDCMUtils
{
    private NaoDCMUtils(){}
    
    private static void set(String actuator, float value, DCMProxy dcmProxy)
    {
        set(actuator, value, 0, dcmProxy);
    }
    
    private static void set(String actuator, float value, int timeOffset, DCMProxy dcmProxy)
    {
        Variant v;
        Variant valsArray;
        
        v = new Variant();
        v.push_back(new Variant(actuator));
        v.push_back(new Variant("Merge"));
        
        valsArray = new Variant();
        Variant vals = new Variant();
        vals.push_back(new Variant(value));        
        vals.push_back(new Variant(dcmProxy.getTime(timeOffset)));
        valsArray.push_back(vals);
        v.push_back(valsArray);        
        dcmProxy.set(v);
    }
    
    /**
     * Smoothly sets the stiffnes (to 1) over a duration of 1 second
     */
    public static void smoothlySetStiffness(String joint, DCMProxy dcmProxy)
    {
        set(joint+"/Hardness/Actuator/Value",1,1000,dcmProxy);
    }
    
    /**
     * Smoothly resets the stiffnes (to 0) over a duration of 1 second
     */
    public static void smoothlyResetStiffness(String joint, DCMProxy dcmProxy)
    {
        set(joint+"/Hardness/Actuator/Value",0,1000,dcmProxy);
    }
    
    public static void setJointRotation(String joint, float value, DCMProxy dcmProxy)
    {
        set(joint+"/Position/Actuator/Value",value,dcmProxy);
    }
    
    public static void setJointRotation(String joint, float value, int timeOffset, DCMProxy dcmProxy)
    {
        set(joint+"/Position/Actuator/Value",value,timeOffset, dcmProxy);
    }
}
