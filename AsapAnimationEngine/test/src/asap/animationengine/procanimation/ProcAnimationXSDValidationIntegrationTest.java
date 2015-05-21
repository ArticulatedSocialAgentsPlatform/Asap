/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.procanimation;

import hmi.testutil.xml.XSDValidationTest;
import hmi.util.Resources;

import java.io.File;
import java.util.Collection;

import org.junit.Before;
import org.junit.runners.Parameterized.Parameters;

/**
 * Testcase to validates the procanimation resource data against the procanimation xsd
 * @author hvanwelbergen
 *
 */
public class ProcAnimationXSDValidationIntegrationTest extends
        XSDValidationTest
{
    private static final Resources PROCANIMATION_XSD_RES = new Resources("xsd");
    private static final String PROCANIMATION_XSD = "procanimation.xsd";
    private static final String PROCANIMATION_DIRS[] = {
        //System.getProperty("shared.project.root")+ "/AsapResource/AsapHumanoidControlShared/resource/Humanoids/shared/procanimation"};
        //System.getProperty("shared.project.root")+ "/AsapResource/AsapHumanoidControlArmandia/resource/Humanoids/blueguy/procanimation",
        System.getProperty("shared.project.root")+ "/asapresource/AsapHumanoidControlArmandia/resource/Humanoids/armandia/procanimation"};
    
    @Before
    public void setup()
    {
        xsdReader = PROCANIMATION_XSD_RES.getReader(PROCANIMATION_XSD);
    }

    @Parameters
    public static Collection<Object[]> configs()
    {
        return configs(PROCANIMATION_DIRS);
    }

    public ProcAnimationXSDValidationIntegrationTest(String label, File f)
    {
        super(label, f);
    }
}
