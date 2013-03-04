package asap.realizerembodiments;

import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JComponent;

import hmi.environmentbase.Environment;
import lombok.Getter;
import lombok.Setter;

/**
 * Manages and constructs a bunch of JComponentEmbodiments
 * @author hvanwelbergen
 *
 */
public class JComponentEnvironment implements Environment
{
    private ConcurrentHashMap<String, JComponentEmbodiment> componentMap = new ConcurrentHashMap<>();
    private volatile boolean shutdown = false;

    public void registerComponent(String id, JComponent jc)
    {
        JComponentEmbodiment jce = new JComponentEmbodiment();
        jce.setId(id);
        jce.setMasterComponent(jc);
        componentMap.put(id, jce);
    }
    
    public JComponentEmbodiment getJComponentEmbodiment(String id)
    {
        return componentMap.get(id);
    }

    @Getter
    @Setter
    private String id = "";

    @Override
    public void requestShutdown()
    {
        shutdown = true;
    }

    @Override
    public boolean isShutdown()
    {
        return shutdown;
    }

}
