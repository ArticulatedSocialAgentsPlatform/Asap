/*******************************************************************************
 *******************************************************************************/
package asap.murml;

import hmi.xml.XMLStructureAdapter;

/**
 * MURML element superklasse
 * @author Herwin
 *
 */
public class MURMLElement extends XMLStructureAdapter
{
    static final String MURMLNAMESPACE = "http://www.techfak.uni-bielefeld.de/ags/soa/murml";

    @Override
    public String getNamespace()
    {
        return MURMLNAMESPACE;
    }
}
