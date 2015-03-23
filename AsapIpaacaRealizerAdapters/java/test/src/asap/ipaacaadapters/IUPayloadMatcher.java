/*******************************************************************************
 *******************************************************************************/
package asap.ipaacaadapters;

import ipaaca.AbstractIU;

import java.util.Map;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

class IUPayloadMatcher<E extends AbstractIU> extends TypeSafeMatcher<E>
{
    private Map<String, String> expectedPayLoad;
    
    public IUPayloadMatcher(Map<String, String> expectedPayLoad)
    {
        this.expectedPayLoad = expectedPayLoad;
    }
    
    @Override
    public void describeTo(Description description)
    {
        description.appendText("Expected LocalIU with payload: ");
        description.appendValue(expectedPayLoad);                  
    }
    
    @Override
    protected boolean matchesSafely(E actual)
    {
        if (!expectedPayLoad.equals(actual.getPayload()))
        {
            return false;
        }
        return true;
    }
}