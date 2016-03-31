/*******************************************************************************
 *******************************************************************************/
package asap.tts.ipaaca;

import hmi.tts.AbstractTTSGenerator;
import hmi.tts.Bookmark;
import hmi.tts.Phoneme;
import hmi.tts.TTSException;
import hmi.tts.TimingInfo;
import hmi.tts.Visime;
import hmi.tts.WordDescription;
import hmi.tts.util.BMLTextUtil;
import hmi.tts.util.NullPhonemeToVisemeMapping;
import hmi.tts.util.PhonemeToVisemeMapping;
import hmi.tts.util.PhonemeUtil;
import ipaaca.AbstractIU;
import ipaaca.HandlerFunctor;
import ipaaca.IUEventHandler;
import ipaaca.IUEventType;
import ipaaca.Initializer;
import ipaaca.InputBuffer;
import ipaaca.LocalMessageIU;
import ipaaca.OutputBuffer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

/**
 * Sends speech commands through to an ipaaca TTS agent
 * @author hvanwelbergen
 * 
 */
public class IpaacaTTSGenerator extends AbstractTTSGenerator
{
    private String voice = "";
    private String name = "";
    private static final String MARYTTSREQUEST_CATEGORY = "maryttsrequest"; // request to Mary TTS client
    private static final String MARYTTSREPLY_CATEGORY = "maryttsreply"; // answer from Mary TTS client
    private static final String CHARACTERNAME_KEY = "name";
    private static final String CHARACTERVOICE_KEY = "voice";
    private static final String TYPE_KEY = "type";
    private static final String SPEECHTEXT_KEY = "text";
    private static final String FILENAME_KEY = "file";
    private static final String STATE_KEY = "state";
    private static final String PHONEMES_KEY = "phonems";
    private static final String MARKS_KEY = "marks";
    private static final String IGNOREXML_KEY = "ignore_xml";

    private static final String EXECUTE_TYPE = "tts.execute";
    private static final String PLAN_TYPE = "tts.plan";
    private static final String STATE_DONE = "done";

    private final InputBuffer inBuffer;
    private final OutputBuffer outBuffer;
    private final PhonemeToVisemeMapping visemeMapping;
    private final VisualProsodyAnalyzer vpAnalyzer;

    private ConcurrentMap<String, BlockingQueue<ImmutableMap<String, String>>> replyQueues = new ConcurrentHashMap<>();

    static
    {
        Initializer.initializeIpaacaRsb();
    }

    public IpaacaTTSGenerator()
    {
        this(new NullPhonemeToVisemeMapping(), new NullProsodyAnalyzer());
    }

    public IpaacaTTSGenerator(VisualProsodyAnalyzer vpAnalyzer)
    {
        this(new NullPhonemeToVisemeMapping(), vpAnalyzer);
    }

    public IpaacaTTSGenerator(PhonemeToVisemeMapping visemeMapping)
    {
        this(visemeMapping, new NullProsodyAnalyzer());
    }

    public IpaacaTTSGenerator(PhonemeToVisemeMapping visemeMapping, VisualProsodyAnalyzer vpAnalyzer)
    {
        this.visemeMapping = visemeMapping;
        this.vpAnalyzer = vpAnalyzer;
        inBuffer = new InputBuffer("IpaacaTTSGeneratorIn", ImmutableSet.of(MARYTTSREPLY_CATEGORY));
        inBuffer.registerHandler(new IUEventHandler(new HandlerFunctor()
        {
            @Override
            public void handle(AbstractIU iu, IUEventType type, boolean local)
            {
                BlockingQueue<ImmutableMap<String, String>> queue = replyQueues.get(iu.getPayload().get(FILENAME_KEY));
                queue.add(ImmutableMap.copyOf(iu.getPayload()));
            }
        }, EnumSet.of(IUEventType.ADDED, IUEventType.UPDATED, IUEventType.MESSAGE), ImmutableSet.of(MARYTTSREPLY_CATEGORY)));
        outBuffer = new OutputBuffer("IpaacaTTSGeneratorOut");
    }

    public void close()
    {
        inBuffer.close();
        outBuffer.close();
    }

    private ImmutableMap<String, String> requestMessage(String executionType, String fileName, String speechText)
    {
        LocalMessageIU speakMessage = new LocalMessageIU();
        speakMessage.setCategory(MARYTTSREQUEST_CATEGORY);
        speakMessage.getPayload().put(CHARACTERNAME_KEY, name);
        speakMessage.getPayload().put(CHARACTERVOICE_KEY, voice);
        speakMessage.getPayload().put(TYPE_KEY, executionType);
        speakMessage.getPayload().put(FILENAME_KEY, fileName);
        speakMessage.getPayload().put(IGNOREXML_KEY, "false");
        if (speechText != null && !speechText.isEmpty())
        {
            speakMessage.getPayload().put(SPEECHTEXT_KEY, speechText);
        }
        outBuffer.add(speakMessage);

        BlockingQueue<ImmutableMap<String, String>> queue = new LinkedBlockingQueue<>();
        replyQueues.put(fileName, queue);
        ImmutableMap<String, String> payload;
        while (true)
        {
            try
            {
                payload = queue.take();
                if (payload.get(STATE_KEY).equals(STATE_DONE))
                {
                    break;
                }
            }
            catch (InterruptedException e)
            {
                Thread.interrupted();
            }
        }
        return payload;
    }

    private String getUniqueFilename()
    {
        return UUID.randomUUID().toString();
    }

    private TimingInfo plan(String text) throws IOException
    {
        return plan(text, getUniqueFilename());
    }

    private TimingInfo plan(String ttsText, String fileName) throws IOException
    {
        ImmutableMap<String, String> payload = requestMessage(PLAN_TYPE, fileName, ttsText);
        if (payload.containsKey(PHONEMES_KEY))
        {
            return createTimingInfo(payload.get(PHONEMES_KEY), payload.get(MARKS_KEY), fileName);
        }
        else
        {
            return createTimingInfo("", "", fileName);
        }
    }

    private void execute(String filename)
    {
        requestMessage(EXECUTE_TYPE, filename, null);
    }

    private WordDescription createWordDescription(String wordPh, String word, int offset)
    {
        List<Phoneme> phonemes = new ArrayList<>();
        List<Visime> visemes = new ArrayList<>();
        String phStr[] = wordPh.split("\\]\\[");

        int prevEnd = offset;
        for (String ph : phStr)
        {
            ph = ph.replaceAll("\\[", "");
            ph = ph.replaceAll("\\]", "");
            String phCont[] = ph.split("\\)\\(");
            String phoneme = phCont[0].replaceAll("\\(", "");
            int start = (int) (Double.parseDouble(phCont[1]) * 1000);
            int end = (int) (Double.parseDouble(phCont[2].replaceAll("\\)", "")) * 1000);
            // hack to put phoneme string into a number
            int phonemeNr = PhonemeUtil.phonemeStringToInt(phoneme);

            // add pause
            if (prevEnd < start)
            {
                int duration = start - prevEnd;
                phonemes.add(new Phoneme(0, duration, false));
                visemes.add(new Visime(0, duration, false));
            }
            int duration = end - start;
            prevEnd = end;
            phonemes.add(new Phoneme(phonemeNr, duration, false));
            visemes.add(new Visime(visemeMapping.getVisemeForPhoneme(phonemeNr), duration, false));
        }
        return new WordDescription(word, phonemes, visemes);
    }

    private List<Bookmark> createBookmarks(String marks, List<WordDescription> wd)
    {
        System.out.println(marks);
        List<Bookmark> bmList = new ArrayList<Bookmark>();
        String seperatedMarks = marks.replaceAll("\\]\\[", ",");
        seperatedMarks = seperatedMarks.replaceAll("><", ",");
        seperatedMarks = seperatedMarks.replaceAll("\\]<", ",");
        seperatedMarks = seperatedMarks.replaceAll(">\\[", ",");
        seperatedMarks = seperatedMarks.replaceAll(">|<|\\[|\\]", "");
        String split[] = seperatedMarks.split(",");

        int wordnr = 0;
        int offset = 0;
        for (int i = 0; i < split.length; i++)
        {
            if (split[i].startsWith("("))
            {
                String bm[] = split[i].split("\\)\\(");
                String name = bm[0].replaceAll("\\(", "");
                WordDescription word = null;
                if (wordnr < wd.size())
                {
                    word = wd.get(wordnr);
                }
                bmList.add(new Bookmark(name, word, offset));
            }
            else
            {
                WordDescription word = wd.get(wordnr);
                offset += word.getDuration();
                wordnr++;
            }
        }
        return bmList;
    }

    private TimingInfo createTimingInfo(String phonemes, String marks, String filename) throws IOException
    {
        List<WordDescription> wd = createWordDescriptions(phonemes, marks);
        List<Visime> vis = createVisemes(wd);

        return new TimingInfo(wd, createBookmarks(marks, wd), vis, vpAnalyzer.analyze(filename));
    }

    private List<Visime> createVisemes(List<WordDescription> wd)
    {
        List<Visime> vis = new ArrayList<>();
        for (WordDescription w : wd)
        {
            vis.addAll(w.getVisimes());
        }
        return vis;
    }

    private List<WordDescription> createWordDescriptions(String phonemes, String marks)
    {
        List<WordDescription> wd = new ArrayList<>();
        String phString = phonemes.split("#")[1].trim();

        String words[] = Iterables.toArray(Splitter.on(";").omitEmptyStrings().split(phString), String.class);

        String wordsOnly = marks.replaceAll("\\[[^\\]]+\\]", "");
        wordsOnly = wordsOnly.replaceAll("><", ",");
        wordsOnly = wordsOnly.replaceAll("<", "");
        wordsOnly = wordsOnly.replaceAll(">", "");
        String wordsInContent[] = Iterables.toArray(Splitter.on(",").omitEmptyStrings().split(wordsOnly), String.class);

        if (wordsInContent.length == 0)
        {
            wordsInContent = new String[words.length];
        }

        int i = 0;
        int offset = 0;
        for (String word : words)
        {
            WordDescription w = createWordDescription(word, wordsInContent[i], offset);
            offset += w.getDuration();
            wd.add(w);
            i++;
        }
        return wd;
    }

    @Override
    public TimingInfo speak(String text) throws TTSException
    {
        String file = getUniqueFilename();
        TimingInfo info = speakToFile(text, file);
        execute(file);
        return info;
    }

    @Override
    public TimingInfo speakBML(String text) throws TTSException
    {
        String file = getUniqueFilename();
        TimingInfo info = speakBMLToFile(text, file);
        execute(file);
        return info;
    }

    @Override
    public TimingInfo speakToFile(String text, String filename) throws TTSException
    {
        try
        {
            return plan(text, filename);
        }
        catch (IOException e)
        {
            throw new TTSException("IOException in planning of " + text, e);
        }
    }

    @Override
    public TimingInfo speakBMLToFile(String text, String filename) throws TTSException
    {
        try
        {
            return plan(BMLTextUtil.BMLToSSML(text), filename);
        }
        catch (IOException e)
        {
            throw new TTSException("IOException in speakBMLToFile of " + text + " filename " + filename, e);
        }
    }

    @Override
    public TimingInfo getTiming(String text) throws TTSException
    {
        try
        {
            return plan(text);
        }
        catch (IOException e)
        {
            throw new TTSException("IOException in planning of " + text, e);
        }
    }

    @Override
    public void setVoice(String speaker)
    {
        voice = speaker;
    }

    @Override
    public String getVoice()
    {
        return voice;
    }

    @Override
    public TimingInfo getBMLTiming(String text) throws TTSException
    {
        try
        {
            return plan(BMLTextUtil.BMLToSSML(text), BMLTextUtil.stripSyncs(text));
        }
        catch (IOException e)
        {
            throw new TTSException("IOException in planning of " + text, e);
        }
    }

    @Override
    public String[] getVoices()
    {
        return new String[] {};
    }

}
