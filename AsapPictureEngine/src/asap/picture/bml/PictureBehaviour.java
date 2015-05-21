/*******************************************************************************
 *******************************************************************************/
package asap.picture.bml;

import java.util.List;

import saiba.bml.core.Behaviour;
import saiba.bml.parser.SyncPoint;

import com.google.common.collect.ImmutableList;
/**
 * Abstract class for all Picture specific Behaviours.
 * 
 */
public abstract class PictureBehaviour extends Behaviour
{
    public PictureBehaviour(String bmlId)
    {
        super(bmlId);        
    }

    static final String PICTURENAMESPACE = "http://hmi.ewi.utwente.nl/pictureengine";

    @Override
    public String getNamespace()
    {
        return PICTURENAMESPACE;
    }

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
}
