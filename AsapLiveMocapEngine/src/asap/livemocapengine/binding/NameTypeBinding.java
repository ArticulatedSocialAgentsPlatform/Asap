/*******************************************************************************
 *******************************************************************************/
package asap.livemocapengine.binding;

import java.util.HashMap;
import java.util.Map;

/**
 * Creates a map of name+(class)type to an instance of that type
 * @author welberge
 */
public class NameTypeBinding
{
    private Map<String, Map<String,Object>> typeMap = new HashMap<String, Map<String,Object>>();

    @SuppressWarnings("unchecked")
    public <T extends Object> T get(String name, Class<T> type)
    {
        if(typeMap.get(name)!=null)
        {
            return (T) typeMap.get(name).get(type.getName());
        }
        return null;
    }
    
    public <T extends Object> void put(String name, Class<T> type, T value)
    {
        Map<String,Object> map = typeMap.get(name);
        if(map==null)
        {
            map = new HashMap<String,Object>();
            typeMap.put(name, map);
        }
        map.put(type.getName(), value);        
    }
    
    public <T extends Object> void put(String name, String typeName, T value)
    {
        Map<String,Object> map = typeMap.get(name);
        if(map==null)
        {
            map = new HashMap<String,Object>();
            typeMap.put(name, map);
        }
        map.put(typeName, value);        
    }
    
    public void remove(String name)
    {
        typeMap.remove(name);
    }
}
