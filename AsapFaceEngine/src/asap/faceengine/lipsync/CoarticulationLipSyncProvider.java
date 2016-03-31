package asap.faceengine.lipsync;

import hmi.faceanimation.FaceController;
import hmi.tts.Phoneme;
import hmi.tts.TTSTiming;
import hmi.tts.Visime;
import hmi.tts.WordDescription;
import hmi.tts.util.PhonemeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import saiba.bml.core.Behaviour;
import asap.faceengine.faceunit.TimedFaceUnit;
import asap.faceengine.viseme.MorphVisemeBinding;
import asap.faceengine.viseme.MorphVisemeDescription;
import asap.realizer.feedback.NullFeedbackManager;
import asap.realizer.lipsync.LipSynchProvider;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.OffsetPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.PlanManager;
import asap.realizer.planunit.TimedPlanUnit;

/**
 * Moves the lips using dominance functions and some coarticulation rules
 * 
 * @author mklemens
 */
public class CoarticulationLipSyncProvider implements LipSynchProvider
{
    private final MorphVisemeBinding visimeBinding;
    private final FaceController faceController;
    private final PlanManager<TimedFaceUnit> facePlanManager;
    private final PegBoard pegBoard;
    private final Map<String, DominanceParameters> dominanceParameters;
    private final Map<String, String> phonemeClass;
    private final Map<String, Double> phonemeMagnitudes;

    // Displays information about the phonemes, their properties and the dropping rules if set to 'true'
    private static boolean ACTIVATEOUTPUTS = true;
    // Gives information about the rules applied to each phoneme (if 'true')
    private static boolean SHOWAPPLIEDRULES = false;

    // If set to 'false' all of the rules implemented for this model will be disabled
    private static boolean RULESACTIVATED = true;

    // Calculates the difference between two sound-classes
    private SoundClassDifferenceCalculator differenceCalculator;

    // The parameter for the dominance functions
    private double startOffsetMultiplicator;
    private double endOffsetMultiplicator;
    private double magnitude;
    private double stretchLeft;
    private double stretchRight;
    private double rateLeft;
    private double rateRight;
    private double peak;

    public CoarticulationLipSyncProvider(Map<String, DominanceParameters> dominanceParameters, Map<String, String> phonemeClass,
            Map<String, Double> phonemeMagnitudes, MorphVisemeBinding visBinding, FaceController fc,
            PlanManager<TimedFaceUnit> facePlanManager, PegBoard pb)
    {
        this.dominanceParameters = dominanceParameters;
        this.phonemeClass = phonemeClass;
        this.phonemeMagnitudes = phonemeMagnitudes;
        visimeBinding = visBinding;
        faceController = fc;
        pegBoard = pb;
        this.facePlanManager = facePlanManager;

        differenceCalculator = new SoundClassDifferenceCalculator();
    }

    @Override
    public void addLipSyncMovement(BMLBlockPeg bbPeg, Behaviour beh, TimedPlanUnit speechUnit, TTSTiming timing)
    {
        // -------------------- DEFINE LOTS OF VARIABLES --------------------

        // A list of all visemes of the current sequence/sentence
        List<Visime> visemes = timing.getVisimes();
        // A list of all words of the current sequence
        List<WordDescription> wordDescriptions = timing.getWordDescriptions();
        // A list of all phonemes of the current sequence
        List<Phoneme> phonemesOfSequence = new ArrayList<>();

        // Those lists are used for checking the position of the phonemes inside a word; contains numbers of the first and last phonemes in all words
        List<Integer> numbersOfFirstPhonemes = new ArrayList<>();
        List<Integer> numbersOfLastPhonemes = new ArrayList<>();
        // Set to true if the current phoneme is at the beginning or end of a word respectively
        boolean isFirst;
        boolean isLast;
        // Set to true if one of the conditions for dropping phonemes is met
        boolean isDropped;

        // The neighbors of the current phoneme and their sound-classes as Strings
        String predecessor;
        String successor;
        String predecessorSoundClass;
        String successorSoundClass;

        // Just as the booleans above but for the neighbors
        boolean isPredecessorFirst;
        boolean isPredecessorLast;
        boolean isSuccessorFirst;
        boolean isSuccessorLast;

        // Another set of Strings for the second-order neighbors; the external neighbors of the neighbors
        String predecessorSecondOrder;
        String successorSecondOrder;
        String predecessorSecondOrderSoundClass;
        String successorSecondOrderSoundClass;

        // -------------------- //DEFINE LOTS OF VARIABLES// --------------------

        // -------------------- CREATE A SEQUENCE OF ALL PHONEMES AND LISTS WITH PHONEMES AT THE BEGINNING OR END OF A WORD --------------------

        if (ACTIVATEOUTPUTS)
        {
            System.out.println("");
            System.out.println("--- Creating sequence of phonemes ---");
            System.out.println("");
            System.out.println("Found " + wordDescriptions.size() + " word(s) in this sequence");
            System.out.println("");
        }

        // A increasing number for the overall amount of phonemes in the current sequence
        int currentPhonemeAmount = wordDescriptions.get(0).getPhonemes().size();
        phonemesOfSequence = new ArrayList<>(wordDescriptions.get(0).getPhonemes());
        numbersOfFirstPhonemes.add(0);
        numbersOfLastPhonemes.add(currentPhonemeAmount - 1);

        // Create the sequence of all phonemes as a list and fill the lists with the numbers of the first and last phonemes in the words of the sequence
        for (WordDescription wd : wordDescriptions)
        {

            if (ACTIVATEOUTPUTS) System.out.print("--- Added " + wd.getPhonemes().size() + " new phonemes ---   ");

            for (Phoneme p : wd.getPhonemes())
            {
                if (ACTIVATEOUTPUTS) System.out.print(PhonemeUtil.phonemeIntToString(p.getNumber()));
                if (p != wd.getPhonemes().get(wd.getPhonemes().size() - 1)) if (ACTIVATEOUTPUTS) System.out.print(", ");
            }

            if (wd != wordDescriptions.get(0))
            {
                phonemesOfSequence.addAll(wd.getPhonemes());

                numbersOfFirstPhonemes.add(currentPhonemeAmount);

                currentPhonemeAmount += wd.getPhonemes().size();

                numbersOfLastPhonemes.add(currentPhonemeAmount - 1);

            }
            if (ACTIVATEOUTPUTS) System.out.println("");
        }

        if (ACTIVATEOUTPUTS)
        {
            System.out.println("");
            System.out.println("--- The sequence consists of " + currentPhonemeAmount + " phonemes ---");
            System.out.println("");
        }

        if (ACTIVATEOUTPUTS) System.out.println("--- Those are the first phonemes ---");
        for (Integer f : numbersOfFirstPhonemes)
            if (ACTIVATEOUTPUTS) System.out.print(PhonemeUtil.phonemeIntToString(phonemesOfSequence.get(f).getNumber()) + "(" + f + ") ");
        if (ACTIVATEOUTPUTS) System.out.println("");

        if (ACTIVATEOUTPUTS) System.out.println("--- Those are the last phonemes ---");
        for (Integer l : numbersOfLastPhonemes)
            if (ACTIVATEOUTPUTS) System.out.print(PhonemeUtil.phonemeIntToString(phonemesOfSequence.get(l).getNumber()) + "(" + l + ") ");
        if (ACTIVATEOUTPUTS)
        {
            System.out.println("");
            System.out.println("");
            System.out.println("--- Done creating the sequence ---");
            System.out.println("");
            if (RULESACTIVATED)
            {
                System.out.println("--- Dropping ---");
                System.out.println("");
            }
        }

        if (!RULESACTIVATED) System.out.println("----- REMINDER: The coarticulation rules are currently deactivated!");

        // -------------------- //CREATE A SEQUENCE OF ALL PHONEMES AND LISTS WITH PHONEMES AT THE BEGINNING OR END OF A WORD// --------------------

        boolean positionEffects;
        boolean rule11;
        boolean rule21;
        boolean rule31;
        boolean rule51;

        // Parameter to define the offset of the individual visemes in the sequence
        double startOffset = 0.0;

        // Note that the number of phonemes and visemes is equal
        for (int currentVisemeNumber = 0; currentVisemeNumber < visemes.size(); currentVisemeNumber++)
        {
            isDropped = false;
            positionEffects = false;
            rule11 = false;
            rule21 = false;
            rule31 = false;
            rule51 = false;

            // nr contains the number of the viseme assigned to the current phoneme
            int nr = visemes.get(currentVisemeNumber).getNumber(); // number of the viseme; to address the new visemes we have to choose a number from 0 to 10
            if (nr < 0 || nr > 10) nr = 0;

            MorphVisemeDescription desc = visimeBinding.getgetMorphTargetForViseme(nr);

            // The DominanceFU is used for modelling the visual representation of the phoneme
            DominanceFU fu = new DominanceFU();
            fu.setTargets(desc.getMorphNames());
            fu = fu.copy(faceController, null, null);

            // The current phoneme represented as a String
            String currentPhoneme = PhonemeUtil.phonemeIntToString(phonemesOfSequence.get(currentVisemeNumber).getNumber());
            // To get the parameter from the HashMap we need the sound-class of the phoneme
            String soundclass = phonemeClass.get(currentPhoneme);

            // -------------------- DEFINE THE PREDECESSORS AND SUCCESSORS OF FIRST AND SECOND ORDER AND THEIR SOUND-CLASSES --------------------

            // Initialize the first order neighbors

            predecessor = null;
            successor = null;
            predecessorSoundClass = null;
            successorSoundClass = null;

            if (currentVisemeNumber != 0)
            {
                predecessor = PhonemeUtil.phonemeIntToString(phonemesOfSequence.get(currentVisemeNumber - 1).getNumber());
                predecessorSoundClass = phonemeClass.get(predecessor);
            }
            if (currentVisemeNumber != phonemesOfSequence.size() - 1)
            {
                successor = PhonemeUtil.phonemeIntToString(phonemesOfSequence.get(currentVisemeNumber + 1).getNumber());
                successorSoundClass = phonemeClass.get(successor);
            }

            // Initialize the second order neighbors

            predecessorSecondOrder = null;
            successorSecondOrder = null;
            predecessorSecondOrderSoundClass = null;
            successorSecondOrderSoundClass = null;

            if (currentVisemeNumber > 1)
            {
                predecessorSecondOrder = PhonemeUtil.phonemeIntToString(phonemesOfSequence.get(currentVisemeNumber - 2).getNumber());
                predecessorSecondOrderSoundClass = phonemeClass.get(predecessorSecondOrder);
            }
            if (currentVisemeNumber < phonemesOfSequence.size() - 2)
            {
                successorSecondOrder = PhonemeUtil.phonemeIntToString(phonemesOfSequence.get(currentVisemeNumber + 2).getNumber());
                successorSecondOrderSoundClass = phonemeClass.get(successorSecondOrder);
            }

            // -------------------- //DEFINE THE PREDECESSORS AND SUCCESSORS OF FIRST AND SECOND ORDER AND THEIR SOUND-CLASSES// --------------------

            // Load default parameters for the dominance function of the current phoneme
            loadDefaultParametersForPhoneme(currentPhoneme, soundclass);

            // -------------------- CHECK IF THE IMPORTANT PHONEMES ARE THE FIRST IN A WORD --------------------

            // Check if the current phoneme is at the beginning or end of a word
            isFirst = false;
            isLast = false;

            if (numbersOfFirstPhonemes.contains(currentVisemeNumber)) isFirst = true;
            if (numbersOfLastPhonemes.contains(currentVisemeNumber)) isLast = true;

            // Now do the same for the surrounding phonemes to enable blending independent from the 'dropped-phoneme-loop'
            isPredecessorFirst = false;
            isPredecessorLast = false;
            isSuccessorFirst = false;
            isSuccessorLast = false;

            if (predecessor != null)
            {
                if (numbersOfFirstPhonemes.contains(currentVisemeNumber - 1)) isPredecessorFirst = true;
                if (numbersOfLastPhonemes.contains(currentVisemeNumber - 1)) isPredecessorLast = true;
            }

            if (successor != null)
            {
                if (numbersOfFirstPhonemes.contains(currentVisemeNumber + 1)) isSuccessorFirst = true;
                if (numbersOfLastPhonemes.contains(currentVisemeNumber + 1)) isSuccessorLast = true;
            }

            // -------------------- //CHECK IF THE IMPORTANT PHONEMES ARE THE FIRST IN A WORD// --------------------

            if (RULESACTIVATED)
            {

                // -------------------- APPLY RULES FOR DROPPING PHONEMES (Includes coarticulation-rules 2, 3 and 6) --------------------

                // All of these effects (DROPS) can only occur if the phoneme is not at the beginning of a word, not an important sound-class or b or p
                if (!isFirst && isNotImportantSoundClass(soundclass) && isNotBorP(currentPhoneme))
                {

                    if (ACTIVATEOUTPUTS) System.out
                            .print("Candidate: " + currentPhoneme + " - " + currentVisemeNumber + " - " + soundclass);

                    // ---------- RULE 2: DROP STOPS OR PHONEMES VERY SIMILAR TO THEIR PREDECESSOR AT THE END OF A WORD ----------

                    // Dropped if too similar at the end OR if stop at the end
                    if (predecessor != null) if (soundclass.equals("stop") && isLast
                            || differenceCalculator.getDifference(soundclass, predecessorSoundClass) <= 1 && isLast)
                    {
                        isDropped = true;
                        positionEffects = true;
                    }

                    // Modify the blending to fill the "gap"
                    if (successor != null) if (!isSuccessorFirst && isNotImportantSoundClass(successorSoundClass) && isNotBorP(successor)) // make sure it wont be dropped
                    if (successorSoundClass.equals("stop") && isSuccessorLast
                            || differenceCalculator.getDifference(soundclass, successorSoundClass) <= 1 && isSuccessorLast) endOffsetMultiplicator += 1.0; // ^ are they
                                                                                                                                                           // different enough
                                                                                                                                                           // or is it a stop?
                                                                                                                                                           // ^

                    // ---------- RULE 6: DROP A PHONEME IF ITS NEIGHBORS ARE VERY DIFFERENT FROM EACH OTHER

                    // Should the current phoneme be dropped?
                    if (predecessor != null && successor != null) if (differenceCalculator.getDifference(predecessorSoundClass,
                            successorSoundClass) > 4)
                    {
                        isDropped = true;
                        rule11 = true;
                    }

                    // Close a possible gap with blending
                    if (successorSecondOrder != null) if (!isSuccessorFirst && isNotImportantSoundClass(successorSoundClass)
                            && isNotBorP(successor)) // make sure it wont be dropped
                    if (differenceCalculator.getDifference(soundclass, successorSecondOrderSoundClass) > 4) // are they different enough?
                    endOffsetMultiplicator += 1.0;
                    if (predecessorSecondOrder != null) if (!isPredecessorFirst && isNotImportantSoundClass(predecessorSoundClass)
                            && isNotBorP(predecessor)) // make sure it wont be dropped
                    if (differenceCalculator.getDifference(soundclass, predecessorSecondOrderSoundClass) > 4) // are they different enough?
                    startOffsetMultiplicator += 1.0;

                    // ---------- RULE 3: DROP NASALE PHONEMES

                    if (soundclass.equals("nasal"))
                    {
                        isDropped = true;
                        rule21 = true;
                    }

                    // Close a possible gap with blending
                    if (successor != null) if (!isSuccessorFirst && isNotImportantSoundClass(successorSoundClass) && isNotBorP(successor)) if (successorSoundClass
                            .equals("nasal")) endOffsetMultiplicator += 1.0;
                    if (predecessor != null) if (!isPredecessorFirst && isNotImportantSoundClass(predecessorSoundClass)
                            && isNotBorP(predecessor)) if (predecessorSoundClass.equals("nasal")) startOffsetMultiplicator += 1.0;

                    if (isDropped)
                    {
                        if (ACTIVATEOUTPUTS) System.out.println(" - DROPPED");
                    }
                    else
                    {
                        if (ACTIVATEOUTPUTS) System.out.println(" - NOT DROPPED");
                    }
                }

                // If one of the conditions above resulted in dropping the current phoneme its magnitude is simply set to 0
                if (isDropped) magnitude = 0.0;

                // -------------------- //APPLY RULES FOR DROPPING PHONEMES// --------------------

                // -------------------- APPLY SOME MORE SIMPLE AND SPECIFIC RULES (Includes coarticulation-rules 2 and 3) --------------------

                // ---------- RULE 4: FOR THE STOPS 'P' AND 'B' WE HAVE TO CLOSE THE MOUTH FOR A SHORT MOMENT
                if (successor != null) if (successorSoundClass.equals("stop") && !isNotBorP(successor))
                {
                    endOffsetMultiplicator = 1.0;
                    rule31 = true;
                }
                if (predecessor != null) if (predecessorSoundClass.equals("stop") && !isNotBorP(predecessor))
                {
                    startOffsetMultiplicator = 0.0;
                    rule31 = true;
                }

                // ---------- RULE 1: REALISTIC MOUTHMOVEMENT BY REDUCTION OF INTENSE FEATURES
                magnitude = 0.8 * magnitude;

                // ---------- RULE 5: 'hh' HAS THE SAME VISEME AS ITS SUCCESSOR / PREDECESSOR
                // The phoneme "hh" has no own visual representation and instead prolongs the viseme of its successor (or predecessor if last phoneme of a word)
                if (currentPhoneme.equals("hh"))
                {
                    magnitude = 0.0;
                    rule51 = true;
                }

                if (predecessor != null) if (predecessor.equals("hh") && !isPredecessorLast) startOffsetMultiplicator += 1.0;
                if (successor != null) if (successor.equals("hh") && !isSuccessorFirst && isSuccessorLast) endOffsetMultiplicator += 1.0;

                // Finally there are some specific rules for a few phonemes that are implemented here; b, f, m, l need longer active-phases
                if (currentPhoneme.equals("b"))
                {
                    stretchLeft = stretchRight = 0.3;
                    startOffsetMultiplicator += 0.3;
                    endOffsetMultiplicator += 0.3;
                }
                if (currentPhoneme.equals("f"))
                {
                    stretchLeft = stretchRight = 0.2;
                    startOffsetMultiplicator += 0.3;
                    endOffsetMultiplicator += 0.3;
                }
                if (currentPhoneme.equals("m"))
                {
                    stretchLeft = 0.3;
                    stretchRight = 0.1;
                    startOffsetMultiplicator += 0.2;
                    endOffsetMultiplicator += 0.2;
                }
                if (currentPhoneme.equals("l"))
                {
                    stretchLeft = stretchRight = 0.2;
                    rateLeft = -0.15;
                    startOffsetMultiplicator += 0.3;
                    endOffsetMultiplicator += 0.3;
                }

                // -------------------- //APPLY SOME MORE SIMPLE AND SPECIFIC RULES// --------------------

                // Displays the applied rules (except for the reduction of the magnitude because this rule is always active)
                if (SHOWAPPLIEDRULES && (positionEffects || rule11 || rule21 || rule31 || rule51))
                {
                    System.out.println("");
                    System.out.println("The following rules have been applied to the phoneme " + currentPhoneme + " ("
                            + currentVisemeNumber + ")");
                    if (positionEffects) System.out.println("----- Positioneffects");
                    if (rule11) System.out.println("----- Different neighbors caused the phoneme to drop");
                    if (rule21) System.out.println("----- Drop nasale phonemes");
                    if (rule31) System.out.println("----- Close the mouth for stops");
                    if (rule51) System.out.println("----- 'hh' adapts to its neighbor");
                    System.out.println("");
                }
            }

            // -------------------- PASS PARAMETERS AND SET THE AMOUNT OF BLENDING --------------------

            // Now set all the parameters for the dominance function; nr: corresponding number of the viseme
            fu.setFunctionParameters(magnitude, stretchLeft, stretchRight, rateLeft, rateRight, peak, currentVisemeNumber, nr);

            TimedFaceUnit tfu = fu.createTFU(NullFeedbackManager.getInstance(), bbPeg, speechUnit.getBMLId(), speechUnit.getId(), pegBoard);

            // To reduce mouth movement after speech has ended
            if (currentVisemeNumber == visemes.size() - 1) endOffsetMultiplicator = 1.0;

            // Set the start- and end-time of the individual visemes for blending effects
            // -0.07 is used for the average 2 frames a viseme should start before the acoustic signal
            TimePeg startPeg = new OffsetPeg(speechUnit.getTimePeg("start"), startOffset
                    - (double) (visemes.get(currentVisemeNumber).getDuration() / 1000d) * startOffsetMultiplicator - 0.07);
            TimePeg endPeg = new OffsetPeg(speechUnit.getTimePeg("start"), startOffset
                    + (double) (visemes.get(currentVisemeNumber).getDuration() / 1000d) * endOffsetMultiplicator - 0.07);
            // System.out.println("Start, end: "+startPeg.getGlobalValue()+" "+endPeg.getGlobalValue());
            tfu.resolveFaceKeyPositions();
            tfu.setSubUnit(true);
            tfu.setTimePeg("start", startPeg);
            tfu.setTimePeg("end", endPeg);
            facePlanManager.addPlanUnit(tfu);

            // Calculate time offset for the next viseme
            startOffset += (double) visemes.get(currentVisemeNumber).getDuration() / 1000d;

            // -------------------- //PASS PARAMETERS AND SET THE AMOUNT OF BLENDING// --------------------
        }

    }

    /** Returns true if the given sound-class s is considered not important. */
    private boolean isNotImportantSoundClass(String s)
    {
        return (s.equals("nasal") || s.equals("fricative") || s.equals("stop"));
    }

    /** Returns true if the given phoneme is not 'b' or 'p'. */
    private boolean isNotBorP(String p)
    {
        return !(p.equals("b") || p.equals("p"));
    }

    /** Get the parameters for the current phoneme from the HashMap of the parameterLoader. */
    private void loadDefaultParametersForPhoneme(String currentPhoneme, String soundclass)
    {

        // Those two parameters define the amount of blending of a viseme with the previous and following visemes
        startOffsetMultiplicator = dominanceParameters.get(soundclass).getStartOffsetMultiplicator();
        endOffsetMultiplicator = dominanceParameters.get(soundclass).getEndOffsetMultiplicator();

        magnitude = dominanceParameters.get(soundclass).getMagnitude()
                * phonemeMagnitudes.get(currentPhoneme);
        stretchLeft = dominanceParameters.get(soundclass).getStretchLeft();
        stretchRight = dominanceParameters.get(soundclass).getStretchRight();
        rateLeft = dominanceParameters.get(soundclass).getRateLeft();
        rateRight = dominanceParameters.get(soundclass).getRateRight();
        peak = dominanceParameters.get(soundclass).getPeak();
    }
}
