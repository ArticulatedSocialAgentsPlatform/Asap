/*******************************************************************************
 *******************************************************************************/
package asap.murml;

import hmi.testutil.LabelledParameterized;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test reading of all gestures in test/resource/gestures
 * @author hvanwelbergen
 *
 */
@RunWith(LabelledParameterized.class)
public class MURMLDescriptionIntegrationTest
{
    private final File file;
    private static String GESTURE_PATH = System.getProperty("shared.project.root")+"/Asap/AsapMURML/test/resource/gestures"; 
    
    @Parameters
    public static Collection<Object[]> configs()
    {
        Collection<Object[]> objs = new ArrayList<Object[]>();
        File dir = new File(GESTURE_PATH);
        
        for (File f : dir.listFiles())
        {
            if (f.isFile() && f.getName().endsWith(".xml"))
            {
                Object obj[] = new Object[2];
                obj[0] = f.getAbsolutePath();
                obj[1] = f;
                objs.add(obj);                
            }
        }
        return objs;
    }
    
    public MURMLDescriptionIntegrationTest(String label, File f)
    {
        file = f;        
    }    
    
    @Test
    public void test() throws IOException
    {
        MURMLDescription desc = new MURMLDescription();
        desc.readXML(file);
    }
}
