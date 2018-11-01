package mv2h.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class ChordProgression {
	public final SortedSet<Chord> chords;
	
	public ChordProgression() {
		chords = new TreeSet<Chord>();
	}
	
	public void addChord(Chord chord) {
		chords.add(chord);
	}
	
	public List<Chord> getChords() {
		return new ArrayList<Chord>(chords);
	}
	
	public double getScore(ChordProgression groundTruth, int lastTime) {
		if (groundTruth.chords.isEmpty()) {
			return Double.NaN;
		}
		
		List<Chord> transcriptionChords = getChords();
		List<Chord> groundTruthChords = groundTruth.getChords();
		
		double totalDuration = lastTime - groundTruthChords.get(0).time;
		int correctDuration = 0;
		
		for (int transcriptionIndex = 0; transcriptionIndex < transcriptionChords.size(); transcriptionIndex++) {
			Chord transcriptionChord = transcriptionChords.get(transcriptionIndex);
			int nextTranscriptionChordTime = transcriptionIndex == transcriptionChords.size() - 1 ?
					lastTime : Math.min(lastTime, transcriptionChords.get(transcriptionIndex + 1).time);
			
			for (int groundTruthIndex = 0; groundTruthIndex < groundTruthChords.size(); groundTruthIndex++) {
				Chord groundTruthChord = groundTruthChords.get(groundTruthIndex);
				int nextGroundTruthChordTime = groundTruthIndex == groundTruthChords.size() - 1 ?
						lastTime : Math.min(lastTime, groundTruthChords.get(groundTruthIndex + 1).time);
				
				int overlapBeginning = Math.max(transcriptionChord.time, groundTruthChord.time);
				int overlapEnding = Math.min(nextTranscriptionChordTime, nextGroundTruthChordTime);
				
				if (overlapEnding > overlapBeginning) {
					int overlapLength = overlapEnding - overlapBeginning;
					
					if (transcriptionChord.matches(groundTruthChord)) {
						correctDuration += overlapLength;
					}
				}
			}
		}
		
		return correctDuration / totalDuration;
	}
	
	public String toString() {
		return "Progression " + chords.toString();
	}
}
