package asap.faceengine.facebinding;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.List;

import saiba.bml.core.FaceLexemeBehaviour;
import saiba.bml.core.ext.FaceFacsBehaviour;
import hmi.xml.XMLTokenizer;

import org.junit.Before;

import hmi.faceanimation.FaceController;
import hmi.faceanimation.converters.EmotionConverter;
import hmi.faceanimation.converters.FACSConverter;

import org.junit.Test;

import asap.faceengine.faceunit.TimedFaceUnit;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.ParameterException;

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
    private static final double PARAMETER_PRECISION = 0.0001;

    @Before
    public void setup()
    {
        String binding = "<facebinding>" + "<FaceUnitSpec type=\"faceLexeme\">" + "<constraints>"
                + "<constraint name=\"lexeme\" value=\"smile\"/>" + "</constraints>" + "<parametermap>"
                + "<parameter src=\"amount\" dst=\"intensity\"/>" + "</parametermap>" + "<parameterdefaults>"
                + "<parameterdefault name=\"angle\" value=\"315\"/>" + "<parameterdefault name=\"activation\" value=\"1\"/>"
                + "</parameterdefaults>" + "<FaceUnit type=\"Plutchik\"/>" + "</FaceUnitSpec>" + "<FaceUnitSpec type=\"face\">"
                + "<constraints>" + "<constraint name=\"type\" value=\"LEXICALIZED\"/>" + "<constraint name=\"lexeme\" value=\"frown\"/>"
                + "</constraints>" + "<parametermap>" + "<parameter src=\"amount\" dst=\"intensity\"/>" + "</parametermap>"
                + "<parameterdefaults>" + "<parameterdefault name=\"intensity\" value=\"1\"/>"
                + "<parameterdefault name=\"targetname\" value=\"bodymorpher1\"/>" + "</parameterdefaults>" + "<FaceUnit type=\"Morph\"/>"
                + "</FaceUnitSpec>" + "<FaceUnitSpec type=\"faceFacs\" namespace=\"http://www.bml-initiative.org/bml/coreextensions-1.0\">"
                + "<constraints>" + "</constraints>" + "<parametermap>" + "<parameter src=\"amount\" dst=\"intensity\"/>"
                + "<parameter src=\"au\" dst=\"au\"/>" + "<parameter src=\"side\" dst=\"side\"/>" + "</parametermap>"
                + "<FaceUnit type=\"AU\"/>" + "</FaceUnitSpec>" + "</facebinding>";
        faceBinding = new FaceBinding();
        faceBinding.readXML(binding);
    }

    private FaceLexemeBehaviour createFaceLexemeBehaviour(String bmlId, String bml) throws IOException
    {
        return new FaceLexemeBehaviour(bmlId, new XMLTokenizer(bml));
    }

    @Test
    public void testReadLexemeXML() throws IOException, ParameterException
    {
        FaceLexemeBehaviour fbeh = createFaceLexemeBehaviour("bml1", "<faceLexeme xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" "
                + "amount=\"3\" id=\"face1\" lexeme=\"smile\"/>");

        BMLBlockPeg bbPeg = new BMLBlockPeg("bml1", 0.3);
        List<TimedFaceUnit> fus = faceBinding.getFaceUnit(mockFeedbackManager, bbPeg, fbeh, mockFaceController, mockFacsConverter,
                mockEmotionConverter);
        assertEquals(1, fus.size());
        assertEquals(fus.get(0).getBMLId(), "bml1");
        assertEquals(fus.get(0).getId(), "face1");
        assertEquals(Double.parseDouble(fus.get(0).getFaceUnit().getParameterValue("angle")), 315, PARAMETER_PRECISION);
        assertEquals(Double.parseDouble(fus.get(0).getFaceUnit().getParameterValue("activation")), 1, PARAMETER_PRECISION);
        assertEquals(Double.parseDouble(fus.get(0).getFaceUnit().getParameterValue("intensity")), 3, PARAMETER_PRECISION);
    }

    @Test
    public void testReadFacsXML() throws IOException, ParameterException
    {
        String str = "<faceFacs id=\"face1\" xmlns=\"http://www.bml-initiative.org/bml/coreextensions-1.0\" au=\"1\" side=\"BOTH\" amount=\"1\"/>";
        FaceFacsBehaviour fbeh = new FaceFacsBehaviour("bml1", new XMLTokenizer(str));
        BMLBlockPeg bbPeg = new BMLBlockPeg("bml1", 0.3);
        List<TimedFaceUnit> fus = faceBinding.getFaceUnit(mockFeedbackManager, bbPeg, fbeh, mockFaceController, mockFacsConverter,
                mockEmotionConverter);

        assertEquals(1, fus.size());
        assertEquals(fus.get(0).getBMLId(), "bml1");
        assertEquals(fus.get(0).getId(), "face1");
        assertEquals(1, Double.parseDouble(fus.get(0).getParameterValue("au")), PARAMETER_PRECISION);
        assertEquals("BOTH", fus.get(0).getParameterValue("side"));
        assertEquals(1, Double.parseDouble(fus.get(0).getParameterValue("intensity")), PARAMETER_PRECISION);
    }
}
