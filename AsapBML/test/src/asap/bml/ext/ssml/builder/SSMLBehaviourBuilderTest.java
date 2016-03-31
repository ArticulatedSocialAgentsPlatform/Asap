package asap.bml.ext.ssml.builder;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import saiba.bml.BMLInfo;
import saiba.bml.core.Behaviour;
import saiba.bml.core.SpeechBehaviour;
import asap.bml.ext.bmlt.BMLTInfo;
import asap.bml.ext.ssml.SSMLBehaviour;

/**
 * Unit tests for SSMLBehaviourBuilder
 * @author hvanwelbergen
 *
 */
public class SSMLBehaviourBuilderTest
{
    @Before
    public void setup()
    {
        BMLTInfo.init();
        BMLInfo.supportedExtensions.add(SSMLBehaviour.class);
    }
    
    @Test
    public void test()
    {
        
        SSMLBehaviourBuilder bb = new SSMLBehaviourBuilder("bml1","beh1");
        bb.ssmlContent("hello world");
        Behaviour beh = bb.build();
        assertThat(beh, instanceOf(SpeechBehaviour.class));
        assertThat(beh.descBehaviour, instanceOf(SSMLBehaviour.class));
        SpeechBehaviour sb = (SpeechBehaviour)beh;
        SSMLBehaviour ssb = (SSMLBehaviour)beh.descBehaviour;
        assertEquals("hello world", sb.getContent());
        assertEquals("hello world", ssb.getContent());
    }
    
    @Test
    public void testWithMark()
    {
        SSMLBehaviourBuilder bb = new SSMLBehaviourBuilder("bml1","beh1");
        bb.ssmlContent("hello <mark name=\"s1\"/> world");
        Behaviour beh = bb.build();
        assertThat(beh, instanceOf(SpeechBehaviour.class));
        assertThat(beh.descBehaviour, instanceOf(SSMLBehaviour.class));
        SpeechBehaviour sb = (SpeechBehaviour)beh;
        SSMLBehaviour ssb = (SSMLBehaviour)beh.descBehaviour;
        assertEquals("hello <sync id=\"s1\" xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\"/>world", sb.getContent());
        assertEquals("hello <mark name=\"s1\"/> world", ssb.getContent());
    }
    
    @Test
    public void testWithMarkAndProsody()
    {
        SSMLBehaviourBuilder bb = new SSMLBehaviourBuilder("bml1","beh1");
        bb.ssmlContent("hello <mark name=\"s1\"/> <prosody rate=\"fast\"/> world");
        Behaviour beh = bb.build();
        assertThat(beh, instanceOf(SpeechBehaviour.class));
        assertThat(beh.descBehaviour, instanceOf(SSMLBehaviour.class));
        SpeechBehaviour sb = (SpeechBehaviour)beh;
        SSMLBehaviour ssb = (SSMLBehaviour)beh.descBehaviour;
        assertEquals("hello <sync id=\"s1\" xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\"/>world", sb.getContent());
        assertEquals("hello <mark name=\"s1\"/> <prosody rate=\"fast\"/> world", ssb.getContent());
    }
}
