package mv2h.objects.harmony;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A <code>KeyProgression</code> is the sequences of {@link Key}s of a musical score.
 * 
 * @author Andrew McLeod
 */
public class KeyProgression {
	/**
	 * A set of musical keys, ordered by time.
	 */
	public final SortedSet<Key> keys;
	
	/**
	 * Create a new empty key progression.
	 */
	public KeyProgression() {
		keys = new TreeSet<Key>();
	}
	
	/**
	 * Add the given key to this progression.
	 * 
	 * @param key The key to add.
	 */
	public void addKey(Key key) {
		keys.add(key);
	}
	
	/**
	 * Get an ordered list of the keys in this progression.
	 * 
	 * @return {@link #keys}
	 */
	public List<Key> getKeys() {
		return new ArrayList<Key>(keys);
	}
	
	/**
	 * Get the score of this transcription given some ground truth and an ending time (in milliseconds).
	 * 
	 * @param groundTruth The ground truth key progression.
	 * @param lastTime The last time of the ground truth musical score.
	 * 
	 * @return The key score of this transcription, or NaN if this progression is empty.
	 */
	public double getScore(KeyProgression groundTruth, int lastTime) {
		if (groundTruth.keys.isEmpty()) {
			return Double.NaN;
		}
		
		List<Key> transcriptionKeys = getKeys();
		List<Key> groundTruthKeys = groundTruth.getKeys();
		
		double totalDuration = lastTime - groundTruthKeys.get(0).time;
		// A map of the score of an overlap to the duration for which that score occurs
		Map<Double, Integer> scoreMap = new HashMap<Double, Integer>();
		
		for (int transcriptionIndex = 0; transcriptionIndex < transcriptionKeys.size(); transcriptionIndex++) {
			// Go through each transcribed key
			
			Key transcriptionKey = transcriptionKeys.get(transcriptionIndex);
			int nextTranscriptionKeyTime = transcriptionIndex == transcriptionKeys.size() - 1 ?
					lastTime : Math.min(lastTime, transcriptionKeys.get(transcriptionIndex + 1).time);
			
			for (int groundTruthIndex = 0; groundTruthIndex < groundTruthKeys.size(); groundTruthIndex++) {
				// Go through each ground truth key
				
				Key groundTruthKey = groundTruthKeys.get(groundTruthIndex);
				int nextGroundTruthKeyTime = groundTruthIndex == groundTruthKeys.size() - 1 ?
						lastTime : Math.min(lastTime, groundTruthKeys.get(groundTruthIndex + 1).time);
				
				// Get overlap times
				int overlapBeginning = Math.max(transcriptionKey.time, groundTruthKey.time);
				int overlapEnding = Math.min(nextTranscriptionKeyTime, nextGroundTruthKeyTime);
				
				// Check for valid overlap
				if (overlapEnding > overlapBeginning) {
					int overlapLength = overlapEnding - overlapBeginning;
					
					double score = transcriptionKey.getScore(groundTruthKey);
					
					// ADd score to map
					if (!scoreMap.containsKey(score)) {
						scoreMap.put(score, overlapLength);
					} else {
						scoreMap.put(score, scoreMap.get(score) + overlapLength);
					}
				}
			}
		}
		
		// Reweight and normalize the scores
		double weightedCorrectDuration = 0.0;
		for (Double score : scoreMap.keySet()) {
			weightedCorrectDuration += score * scoreMap.get(score);
		}
		
		return weightedCorrectDuration / totalDuration;
	}
	
	@Override
	public String toString() {
		return "Progression " + keys.toString();
	}
}
