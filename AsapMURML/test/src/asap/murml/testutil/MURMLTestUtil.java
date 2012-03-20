package asap.murml.testutil;

import asap.murml.JointValue;

/**
 * Test Utilities for AsapMURML
 * @author hvanwelbergen
 *
 */
public final class MURMLTestUtil
{
    public static JointValue createJointValue(String id, float ... dofs)
    {
        return new JointValue(id,dofs);
    }
}
