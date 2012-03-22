package asap.faceengine.facebinding;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.List;

import hmi.bml.core.FaceBehaviour;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.xml.XMLTokenizer;

import org.junit.Before;

import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.planunit.ParameterException;
import hmi.faceanimation.FaceController;
import hmi.faceanimation.converters.EmotionConverter;
import hmi.faceanimation.converters.FACSConverter;

import org.junit.Test;

import asap.faceengine.faceunit.TimedFaceUnit;

/**
 * Unit testcases for the facebinding
 * @author hvanwelbergen
 *
 */
public class FaceBindingTest
{
    private FaceController mockFaceController = mock(FaceController.class);
    private FeedbackManager mockFeedbackManager = mock(FeedbackManager.class);
    private FACSConverter mockFacsConverter = mock(FACSConverter.class);
    private EmotionConverter mockEmotionConverter = mock(EmotionConverter.class);
    private FaceBinding faceBinding;
    
    @Before
    public void setup()
    {
        String binding = "<facebinding>"+
        "<FaceUnitSpec type=\"face\">"+
            "<constraints>"+
                "<constraint name=\"type\" value=\"LEXICALIZED\"/>"+
                "<constraint name=\"lexeme\" value=\"smile\"/>"+
            "</constraints>"+
            "<parametermap>"+
                "<parameter src=\"amount\" dst=\"intensity\"/>"+
            "</parametermap>"+
            "<parameterdefaults>"+
                "<parameterdefault name=\"angle\" value=\"315\"/>"+
                "<parameterdefault name=\"activation\" value=\"1\"/>"+
            "</parameterdefaults>"+
            "<FaceUnit type=\"Plutchik\"/>"+
        "</FaceUnitSpec>"+
        "<FaceUnitSpec type=\"face\">"+
            "<constraints>"+
            "<constraint name=\"type\" value=\"LEXICALIZED\"/>"+
            "<constraint name=\"lexeme\" value=\"frown\"/>"+
            "</constraints>"+
            "<parametermap>"+
                "<parameter src=\"amount\" dst=\"intensity\"/>"+
            "</parametermap>"+
            "<parameterdefaults>"+
                "<parameterdefault name=\"intensity\" value=\"1\"/>"+
                "<parameterdefault name=\"targetname\" value=\"bodymorpher1\"/>"+                         
            "</parameterdefaults>"+
            "<FaceUnit type=\"Morph\"/>"+
        "</FaceUnitSpec>"+  
        "</facebinding>";
        faceBinding = new FaceBinding();
        faceBinding.readXML(binding);
    }
    
    private FaceBehaviour createFaceBehaviour(String bmlId, String bml) throws IOException
    {
        return new FaceBehaviour(bmlId,new XMLTokenizer(bml));
    }
    @Test
    public void testReadXML() throws IOException, ParameterException
    {
        FaceBehaviour fbeh = createFaceBehaviour("bml1","<face amount=\"3\" id=\"face1\" type=\"LEXICALIZED\" lexeme=\"smile\"/>");
        
        BMLBlockPeg bbPeg = new BMLBlockPeg("bml1",0.3);
        List<TimedFaceUnit> fus = faceBinding.getFaceUnit(mockFeedbackManager,bbPeg, fbeh, mockFaceController,mockFacsConverter,mockEmotionConverter);
        assertEquals(1,fus.size());
        assertEquals(fus.get(0).getBMLId(),"bml1");
        assertEquals(fus.get(0).getId(),"face1");        
        assertEquals(Double.parseDouble(fus.get(0).getFaceUnit().getParameterValue("angle")),315,0.0001);
        assertEquals(Double.parseDouble(fus.get(0).getFaceUnit().getParameterValue("activation")),1,0.0001);
        assertEquals(Double.parseDouble(fus.get(0).getFaceUnit().getParameterValue("intensity")),3,0.0001);
    }
}
