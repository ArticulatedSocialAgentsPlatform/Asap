/*******************************************************************************
 *******************************************************************************/
package asap.livemocapengine.binding;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

interface StubInterface{}

/**
 * Unit tests for the NameTypeBinding
 * @author welberge
 *
 */
public class NameTypeBindingTest
{
    NameTypeBinding ntb = new NameTypeBinding();
    
    @Test
    public void testGetAndPutOne()
    {
        class Stub{}
        Stub stub = new Stub();
        ntb.put("stub", Stub.class, stub);
        assertEquals(stub, ntb.get("stub", Stub.class));
    }
    
    @Test
    public void testGetAndPutTwoWithSameName()
    {
        class Stub1{}
        class Stub2{}
        Stub1 stub1 = new Stub1();
        Stub2 stub2 = new Stub2();
        ntb.put("stub", Stub1.class, stub1);
        ntb.put("stub", Stub2.class, stub2);        
        assertEquals(stub1, ntb.get("stub", Stub1.class));
        assertEquals(stub2, ntb.get("stub", Stub2.class));
    }
    
    @Test
    public void testGetAndPutWithInterface()
    {
        class Stub implements StubInterface{}
        Stub stub = new Stub();
        ntb.put("stub", StubInterface.class, stub);
        assertEquals(stub, ntb.get("stub", StubInterface.class));
    }
    
    @Test
    public void testGetAndPutWithName()
    {
        class Stub implements StubInterface{}
        Stub stub = new Stub();
        System.out.println(StubInterface.class.getName());
        ntb.put("stub", "asap.livemocapengine.binding.StubInterface", stub);
        assertEquals(stub, ntb.get("stub", StubInterface.class));
    }
}
