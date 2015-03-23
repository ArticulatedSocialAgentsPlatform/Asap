/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.controller;

import hmi.testutil.xml.XSDValidationTest;
import hmi.util.Resources;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;

import org.junit.Before;
import org.junit.runners.Parameterized.Parameters;

/**
 * Validates the XML of compound controllers in /HmiResource/HmiHumanoidBodyControl/resource/Humanoids/shared/controllers
 * and /HmiResource/HmiHumanoidBodyControl/resource/Humanoids/armandia/controllers
 * @author welberge
 * 
 */
public class CompoundControllerXSDValidationIntegrationTest extends XSDValidationTest
{
    private static final Resources CC_XSD_RES = new Resources("xsd");
    private static final String CC_XSD = "compoundcontroller.xsd";
    private static final String CC_DIRS[] = {
            System.getProperty("shared.project.root") + "/asapresource/AsapHumanoidControlShared/resource/Humanoids/shared/controllers",
            System.getProperty("shared.project.root") + "/asapresource/AsapHumanoidControlArmandia/resource/Humanoids/armandia/controllers" };

    @Before
    public void setup()
    {
        xsdReader = CC_XSD_RES.getReader(CC_XSD);
    }

    @Override
    protected InputStream getXSDStream(String fileName)
    {
        return CC_XSD_RES.getInputStream(fileName);
    }

    @Parameters
    public static Collection<Object[]> configs()
    {
        return configs(CC_DIRS);
    }

    public CompoundControllerXSDValidationIntegrationTest(String label, File f)
    {
        super(label, f);
    }
}
