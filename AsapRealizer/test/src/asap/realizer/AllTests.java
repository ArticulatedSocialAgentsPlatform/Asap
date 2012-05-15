package asap.realizer;

import org.junit.extensions.cpsuite.ClasspathSuite;
import org.junit.extensions.cpsuite.ClasspathSuite.ClassnameFilters;
import org.junit.runner.RunWith;
/**
 * Runs all Elckerlyc integration and unit tests
 * @author Herwin
 */
@RunWith(ClasspathSuite.class)
@ClassnameFilters({"hmi.elckerlyc.*Test"})
public class AllTests
{

}
