package hmi.elckerlyc;

import org.junit.extensions.cpsuite.ClasspathSuite;
import org.junit.extensions.cpsuite.ClasspathSuite.ClassnameFilters;
import org.junit.runner.RunWith;

/**
 * Runs all unit test cases in HmiElckerlyc
 * @author welberge
 */
@RunWith(ClasspathSuite.class)
@ClassnameFilters({ "hmi.elckerlyc.*Test", "!.*IntegrationTest" })
public class UnitTestSuite
{
}
