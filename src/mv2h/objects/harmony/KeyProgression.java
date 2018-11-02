package mv2h.objects.harmony;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class KeyProgression {
	public final SortedSet<Key> keys;
	
	public KeyProgression() {
		keys = new TreeSet<Key>();
	}
	
	public void addKey(Key key) {
		keys.add(key);
	}
	
	public List<Key> getKeys() {
		return new ArrayList<Key>(keys);
	}
	
	public double getScore(KeyProgression groundTruth, int lastTime) {
		if (groundTruth.keys.isEmpty()) {
			return Double.NaN;
		}
		
		List<Key> transcriptionKeys = getKeys();
		List<Key> groundTruthKeys = groundTruth.getKeys();
		
		double totalDuration = lastTime - groundTruthKeys.get(0).time;
		Map<Double, Integer> scoreMap = new HashMap<Double, Integer>();
		
		for (int transcriptionIndex = 0; transcriptionIndex < transcriptionKeys.size(); transcriptionIndex++) {
			Key transcriptionKey = transcriptionKeys.get(transcriptionIndex);
			int nextTranscriptionKeyTime = transcriptionIndex == transcriptionKeys.size() - 1 ?
					lastTime : Math.min(lastTime, transcriptionKeys.get(transcriptionIndex + 1).time);
			
			for (int groundTruthIndex = 0; groundTruthIndex < groundTruthKeys.size(); groundTruthIndex++) {
				Key groundTruthKey = groundTruthKeys.get(groundTruthIndex);
				int nextGroundTruthKeyTime = groundTruthIndex == groundTruthKeys.size() - 1 ?
						lastTime : Math.min(lastTime, groundTruthKeys.get(groundTruthIndex + 1).time);
				
				int overlapBeginning = Math.max(transcriptionKey.time, groundTruthKey.time);
				int overlapEnding = Math.min(nextTranscriptionKeyTime, nextGroundTruthKeyTime);
				
				if (overlapEnding > overlapBeginning) {
					int overlapLength = overlapEnding - overlapBeginning;
					
					double score = transcriptionKey.getScore(groundTruthKey);
					
					if (!scoreMap.containsKey(score)) {
						scoreMap.put(score, overlapLength);
					} else {
						scoreMap.put(score, scoreMap.get(score) + overlapLength);
					}
				}
			}
		}
		
		double weightedCorrectDuration = 0.0;
		for (Double score : scoreMap.keySet()) {
			weightedCorrectDuration += score * scoreMap.get(score);
		}
		
		return weightedCorrectDuration / totalDuration;
	}
	
	public String toString() {
		return "Progression " + keys.toString();
	}
}
