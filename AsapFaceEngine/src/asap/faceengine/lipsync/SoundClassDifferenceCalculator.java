package asap.faceengine.lipsync;

import java.util.HashMap;
/**
 * This class sole purpose is to return the numeric difference between two sound-classes to define their disparity.
 * 
 * @author mklemens
 */
public class SoundClassDifferenceCalculator {
	
	// A HashMap containing the sound-classes and their difference-value
    private HashMap<String, Integer> differenceValues = new HashMap<String, Integer>();
	
	public SoundClassDifferenceCalculator() {
        differenceValues.put("vowel", 1);
        differenceValues.put("diphthong", 1);
        differenceValues.put("glide", 2);
        differenceValues.put("liquid", 2);
        differenceValues.put("nasal", 3);
        differenceValues.put("fricative", 4);
        differenceValues.put("stop", 5);
        differenceValues.put("affricate", 6);
	}

    // Calculates the numeric difference between two sound-classes
	public int getDifference(String current, String toCompare) {
		return Math.abs(differenceValues.get(current) - differenceValues.get(toCompare));
	}
}