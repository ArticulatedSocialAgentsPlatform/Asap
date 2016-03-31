/*******************************************************************************
 *******************************************************************************/
package hmi.jnaoqiembodiment;

import hmi.headandgazeembodiments.EulerHeadEmbodiment;

import com.aldebaran.proxy.DCMProxy;


/**
 * Steers the Nao Head using dcm 
 * @author welberge
 */
public class NaoHeadEmbodiment implements EulerHeadEmbodiment
{
    private final String id;
    private DCMProxy dcmProxy;
    
    public NaoHeadEmbodiment(String id, DCMProxy dcmProxy)
    {
        this.dcmProxy = dcmProxy;        
        this.id = id;
        NaoDCMUtils.smoothlySetStiffness("HeadYaw", dcmProxy);
        NaoDCMUtils.smoothlySetStiffness("HeadPitch", dcmProxy);
        try
        {
            Thread.sleep(1000);
        }
        catch (InterruptedException e)
        {
            Thread.interrupted();
        }
    }
    
    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public void setHeadRollPitchYawDegrees(float roll, float pitch, float yaw)
    {
        NaoDCMUtils.setJointRotation("HeadYaw", (float)Math.toRadians(yaw), dcmProxy);
        NaoDCMUtils.setJointRotation("HeadPitch", (float)Math.toRadians(pitch), dcmProxy);
    }
    
    public void shutdown()
    {
        NaoDCMUtils.smoothlyResetStiffness("HeadYaw", dcmProxy);
        NaoDCMUtils.smoothlyResetStiffness("HeadPitch", dcmProxy);
    }

    @Override
    public void claimHeadResource()
    {
                
    }

    @Override
    public void releaseHeadResource()
    {
                
    }
}
