package hmi.jnaoqiembodiment;

import hmi.environmentbase.Embodiment;

import com.aldebaran.proxy.ALLedsProxy;
import com.aldebaran.proxy.DCMProxy;

/**
 * Manages all NaoQi Proxies 
 * @author welberge
 */
public class NaoQiEmbodiment implements Embodiment
{
    private final String ip;
    private final int port;
    private DCMProxy dcmProxy;
    private ALLedsProxy ledsProxy;
    private String id = "";
    
    static
    {
      System.loadLibrary("JNaoQi");
    }
    
    public NaoQiEmbodiment(String id, String ip, int port)
    {
        this.id = id;
        this.ip = ip;
        this.port = port;
    }
    
    public NaoQiEmbodiment(String ip, int port)
    {
        this("",ip,port);
    }
    
    public DCMProxy getDCMProxy()
    {
        if(dcmProxy==null)
        {
            dcmProxy = new DCMProxy(ip,port);
        }        
        return dcmProxy;
    }

    public ALLedsProxy getLedsProxy()
    {
        if(ledsProxy==null)
        {
            ledsProxy = new ALLedsProxy(ip,port);
        }
        return ledsProxy;
    }
    
    @Override
    public String getId()
    {
        return id;
    }
}
