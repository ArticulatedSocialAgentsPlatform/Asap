package asap.faceengine.viseme;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import asap.faceengine.faceunit.TimedFaceUnit;
import asap.faceengine.viseme.MorphVisemeBinding;
import asap.faceengine.viseme.VisemeBinding;
import asap.faceengine.viseme.VisemeToMorphMapping;

import hmi.bml.core.SpeechBehaviour;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.planunit.ParameterException;
import hmi.faceanimation.FaceController;
import hmi.xml.XMLTokenizer;

/**
 * Unit testcases for MorphVisemeBinding
 * @author welberge
 */
public class MorphVisemeBindingTest
{
    private FaceController mockFaceController = mock(FaceController.class);
    private SpeechBehaviour speechBehaviour;
    private BMLBlockPeg bbPeg = new BMLBlockPeg("bb",0);
    
    @Before
    public void setup() throws IOException
    {
        speechBehaviour = new SpeechBehaviour("bml1",new XMLTokenizer("<speech id=\"s1\"><text>Hello World</text></speech>"));        
    }
    
    public VisemeBinding getVisemeBinding()
    {
        VisemeToMorphMapping mapping = new VisemeToMorphMapping();
        return new MorphVisemeBinding(mapping);
    }    
    
    @Test
    public void getNonExistingViseme() throws ParameterException
    {
        TimedFaceUnit tfu = getVisemeBinding().getVisemeUnit(bbPeg, speechBehaviour, 999, mockFaceController);
        assertEquals("",tfu.getParameterValue("targetname"));
    }
}
