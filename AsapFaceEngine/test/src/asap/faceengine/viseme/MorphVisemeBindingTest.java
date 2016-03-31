/*******************************************************************************
 *******************************************************************************/
package asap.faceengine.viseme;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import hmi.faceanimation.FaceController;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import saiba.bml.core.SpeechBehaviour;
import asap.faceengine.faceunit.TimedFaceUnit;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.ParameterException;

/**
 * Unit testcases for MorphVisemeBinding
 * @author welberge
 */
public class MorphVisemeBindingTest
{
    private FaceController mockFaceController = mock(FaceController.class);
    private SpeechBehaviour speechBehaviour;
    private BMLBlockPeg bbPeg = new BMLBlockPeg("bb", 0);
    private final PegBoard pegBoard = new PegBoard();

    @Before
    public void setup() throws IOException
    {
        speechBehaviour = new SpeechBehaviour("bml1", new XMLTokenizer("<speech xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" "
                + "id=\"s1\"><text>Hello World</text></speech>"));
    }

    public VisemeBinding getVisemeBinding()
    {
        VisemeToMorphMapping mapping = new VisemeToMorphMapping();
        return new MorphVisemeBinding(mapping);
    }

    @Test
    public void getNonExistingViseme() throws ParameterException
    {
        TimedFaceUnit tfu = getVisemeBinding().getVisemeUnit(bbPeg, speechBehaviour, 999, mockFaceController, pegBoard);
        assertEquals("", tfu.getParameterValue("targetname"));
    }
}
