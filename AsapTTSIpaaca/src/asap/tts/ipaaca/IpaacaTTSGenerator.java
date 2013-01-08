package asap.tts.ipaaca;

import hmi.tts.AbstractTTSGenerator;
import hmi.tts.Bookmark;
import hmi.tts.Phoneme;
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
import ipaaca.InputBuffer;
import ipaaca.LocalMessageIU;
import ipaaca.OutputBuffer;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

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

    private static final String EXECUTE_TYPE = "tts.execute";
    private static final String PLAN_TYPE = "tts.plan";
    private static final String STATE_DONE = "done";

    private final InputBuffer inBuffer;
    private final OutputBuffer outBuffer;
    private final PhonemeToVisemeMapping visemeMapping;

    private ConcurrentMap<String, BlockingQueue<ImmutableMap<String, String>>> replyQueues = new ConcurrentHashMap<>();

    public IpaacaTTSGenerator()
    {
        this(new NullPhonemeToVisemeMapping());
    }

    public IpaacaTTSGenerator(PhonemeToVisemeMapping visemeMapping)
    {
        this.visemeMapping = visemeMapping;
        inBuffer = new InputBuffer("IpaacaTTSGeneratorIn", ImmutableSet.of(MARYTTSREPLY_CATEGORY));
        inBuffer.registerHandler(new IUEventHandler(new HandlerFunctor()
        {

            @Override
            public void handle(AbstractIU iu, IUEventType type, boolean local)
            {
                System.out.println(iu.getPayload());
                BlockingQueue<ImmutableMap<String, String>> queue = replyQueues.get(iu.getPayload().get(FILENAME_KEY));
                queue.add(ImmutableMap.copyOf(iu.getPayload()));
            }
        }, EnumSet.of(IUEventType.ADDED, IUEventType.UPDATED), ImmutableSet.of(MARYTTSREPLY_CATEGORY)));
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

    private TimingInfo plan(String text)
    {
        return plan(text, getUniqueFilename());
    }

    private TimingInfo plan(String text, String fileName)
    {
        return plan(text, text, fileName);
    }

    private TimingInfo plan(String ttsText, String content, String fileName)
    {
        ImmutableMap<String, String> payload = requestMessage(PLAN_TYPE, fileName, ttsText);
        if (payload.containsKey(PHONEMES_KEY))
        {
            return createTimingInfo(payload.get(PHONEMES_KEY), content);
        }
        else
        {
            return createTimingInfo("", content);
        }
    }

    private void execute(String filename)
    {
        requestMessage(EXECUTE_TYPE, filename, null);
    }

    private WordDescription createWordDescription(String wordPh, String word)
    {
        List<Phoneme> phonemes = new ArrayList<>();
        List<Visime> visemes = new ArrayList<>();
        String phStr[] = wordPh.split("\\]\\[");
        for (String ph : phStr)
        {
            ph = ph.replaceAll("\\[", "");
            ph = ph.replaceAll("\\]", "");
            String phCont[] = ph.split("\\)\\(");
            String phoneme = phCont[0].replaceAll("\\(", "");
            double start = Double.parseDouble(phCont[1]);
            double end = Double.parseDouble(phCont[2].replaceAll("\\)", ""));
            // hack to put phoneme string into a number
            int phonemeNr = PhonemeUtil.phonemeStringToInt(phoneme);

            int duration = (int) ((end - start) * 1000);
            phonemes.add(new Phoneme(phonemeNr, duration, false));
            visemes.add(new Visime(visemeMapping.getVisemeForPhoneme(phonemeNr), duration, false));
        }
        return new WordDescription(word, phonemes, visemes);
    }

    private TimingInfo createTimingInfo(String phonemes, String content)
    {
        List<WordDescription> wd = new ArrayList<>();
        List<Bookmark> bms = new ArrayList<>();
        List<Visime> vis = new ArrayList<>();
        System.out.println("createTimingInfo from " + phonemes);

        String phString = phonemes.replaceAll("\\[\\(0\\)\\(0\\)\\(0\\)\\]\\#", "");
        String words[] = phString.split(";");
        String wordsInContent[] = content.split("\\s+");

        int i = 0;
        for (String word : words)
        {
            WordDescription w = createWordDescription(word, wordsInContent[i]);
            wd.add(w);
            vis.addAll(w.getVisimes());
            i++;
        }
        return new TimingInfo(wd, bms, vis);
    }

    @Override
    public TimingInfo speak(String text)
    {
        String file = getUniqueFilename();        
        TimingInfo info = speakToFile(text, file);
        execute(file);
        return info;
    }

    @Override
    public TimingInfo speakBML(String text)
    {
        String file = getUniqueFilename();
        TimingInfo info = speakBMLToFile(text, file);
        execute(file);
        return info;
    }

    @Override
    public TimingInfo speakToFile(String text, String filename)
    {
        return plan(text, filename);        
    }

    @Override
    public TimingInfo speakBMLToFile(String text, String filename)
    {
        return plan(BMLTextUtil.BMLToSSML(text), BMLTextUtil.stripSyncs(text), filename);
    }

    @Override
    public TimingInfo getTiming(String text)
    {
        return plan(text);
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
    public TimingInfo getBMLTiming(String text)
    {
        return plan(BMLTextUtil.BMLToSSML(text), BMLTextUtil.stripSyncs(text));
    }

    @Override
    public String[] getVoices()
    {
        return new String[] {};
    }

}
