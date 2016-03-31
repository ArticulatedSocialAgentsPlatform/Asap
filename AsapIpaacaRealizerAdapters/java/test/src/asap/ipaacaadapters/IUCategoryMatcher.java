/*******************************************************************************
 *******************************************************************************/
package asap.ipaacaadapters;

import ipaaca.AbstractIU;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

class IUCategoryMatcher<E extends AbstractIU> extends TypeSafeMatcher<E>
{
    private String expectedCategory;
    
    
    public IUCategoryMatcher(String expectedCategory)
    {
        this.expectedCategory = expectedCategory;            
    }

    @Override
    public void describeTo(Description description)
    {
        description.appendText("Expected LocalIU with category: ");
        description.appendValue(expectedCategory);                  
    }

    @Override
    protected boolean matchesSafely(E actual)
    {
        if (!expectedCategory.equals(actual.getCategory()))
        {
            return false;
        }
        return true;
    }
}