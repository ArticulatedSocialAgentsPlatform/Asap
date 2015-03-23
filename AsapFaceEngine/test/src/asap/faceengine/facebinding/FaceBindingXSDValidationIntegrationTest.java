/*******************************************************************************
 *******************************************************************************/
package asap.faceengine.facebinding;

import hmi.testutil.xml.XSDValidationTest;
import hmi.util.Resources;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests the validity of facebinding resources in /HmiResource/HmiHumanoidFaceControl/resource/Humanoids/armandia/facebinding
 * @author welberge
 */
public class FaceBindingXSDValidationIntegrationTest extends XSDValidationTest
{
    private static final Resources XSD_RES = new Resources("xsd");
    private static final String XSD = "facebinding.xsd";
    private static final String DIRS[] = { System.getProperty("shared.project.root")
            + "/asapresource/AsapHumanoidControlArmandia/resource/Humanoids/armandia/facebinding" };

    @Before
    public void setup()
    {
        xsdReader = XSD_RES.getReader(XSD);
    }

    @Parameters
    public static Collection<Object[]> configs()
    {
        Collection<Object[]> objs = configs(DIRS);
        Collection<Object[]> filteredObjs = new ArrayList<Object[]>();
        for (Object[] obj : objs)
        {
            if (obj[0].toString().contains("facebinding.xml"))
            {
                filteredObjs.add(obj);
            }
        }
        return filteredObjs;
    }

    public FaceBindingXSDValidationIntegrationTest(String label, File f)
    {
        super(label, f);
    }
}
