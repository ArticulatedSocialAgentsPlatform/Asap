/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.gesturebinding;

import hmi.testutil.xml.XSDValidationTest;
import hmi.util.Resources;

import java.io.File;
import java.util.Collection;

import org.junit.Before;
import org.junit.runners.Parameterized.Parameters;

/**
 * Validates all gesturebindings in 
 * asapresource/AsapHumanoidControlShared/resource/Humanoids/shared/gesturebinding and  
 * asapresource/AsapHumanoidControlArmandia/resource/Humanoids/armandia/gesturebinding
 * using the xsd
 * @author welberge
 *
 */
public class GestureBindingXSDValidationTest extends XSDValidationTest
{
    private static final Resources GB_XSD_RES = new Resources("xsd");
    private static final String GB_XSD = "gesturebinding.xsd";
    private static final String GB_DIRS[] = {
        System.getProperty("shared.project.root")+"/asapresource/AsapHumanoidControlShared/resource/Humanoids/shared/gesturebinding",
        System.getProperty("shared.project.root")+"/asapresource/AsapHumanoidControlArmandia/resource/Humanoids/armandia/gesturebinding"
    };
        
    
    @Before
    public void setup()
    {
        xsdReader = GB_XSD_RES.getReader(GB_XSD);        
    }
    
    @Parameters
    public static Collection<Object[]> configs()
    {
        return configs(GB_DIRS);        
    }
    
    public GestureBindingXSDValidationTest(String label, File f)
    {
        super(label, f);        
    }
}
