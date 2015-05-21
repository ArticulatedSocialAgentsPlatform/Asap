/*******************************************************************************
 *******************************************************************************/
package asap.emitterengine.bml;

import java.util.List;

import saiba.bml.parser.SyncPoint;
import asap.bml.ext.bmlt.BMLTBehaviour;

import com.google.common.collect.ImmutableList;
/**
 * Abstract class for all emitter engine specific Behaviours.
 * 
 * @author Dennis Reidsma
 */
public class EmitterBehaviour extends BMLTBehaviour
{
    public EmitterBehaviour(String bmlId)
    {
        super(bmlId);        
    }

    @Override
    public void addDefaultSyncPoints()
    {
        for(String s:getDefaultSyncPoints())
        {
            addSyncPoint(new SyncPoint(bmlId, id, s));
        }        
    }
    
    private static final List<String> DEFAULT_SYNCS = ImmutableList.of("start","end");
    public static List<String> getDefaultSyncPoints()
    {
        return DEFAULT_SYNCS;
    }

}
