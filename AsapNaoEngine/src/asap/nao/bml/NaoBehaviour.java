/*******************************************************************************
 *******************************************************************************/
package asap.nao.bml;

import java.util.List;

import saiba.bml.core.Behaviour;
import saiba.bml.parser.SyncPoint;

import com.google.common.collect.ImmutableList;
/**
 * Abstract class for all Nao specific Behaviours.
 * 
 * @author Robin ten Buuren
 */
public abstract class NaoBehaviour extends Behaviour
{
    public NaoBehaviour(String bmlId)
    {
        super(bmlId);        
    }

    static final String NAONAMESPACE = "http://hmi.ewi.utwente.nl/nao";

    private static final List<String> DEFAULT_SYNCS = ImmutableList.of("start","end");
   
    public static List<String> getDefaultSyncPoints()
    {
        return DEFAULT_SYNCS;
    }
    @Override
    public void addDefaultSyncPoints()
    {
        for(String s:getDefaultSyncPoints())
        {
            addSyncPoint(new SyncPoint(bmlId, id, s));
        }        
    }

    @Override
    public String getNamespace()
    {
        return NAONAMESPACE;
    }



}
