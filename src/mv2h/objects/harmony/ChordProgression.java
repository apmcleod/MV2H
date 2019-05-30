package mv2h.objects.harmony;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A <code>ChordProgression</code> represents the sequence of {@link Chord}s present in a musical score.
 * 
 * @author Andrew McLeod
 */
public class ChordProgression {
	/**
	 * An ordered, unique sequence of chords, ordered by increasing start time.
	 */
	public final SortedSet<Chord> chords;
	
	/**
	 * Create a new empty chord progression.
	 */
	public ChordProgression() {
		chords = new TreeSet<Chord>();
	}
	
	/**
	 * Add the given chord to this progression.
	 * 
	 * @param chord The chord to add.
	 */
	public void addChord(Chord chord) {
		chords.add(chord);
	}
	
	/**
	 * Get an ordered list of the chords in this progression.
	 * 
	 * @return {@link #chords}
	 */
	public List<Chord> getChords() {
		return new ArrayList<Chord>(chords);
	}
	
	/**
	 * Get the score of this transcribed chord progression given the ground truth one and some end time
	 * (in milliseconds).
	 * 
	 * @param groundTruth The ground truth chord progression.
	 * @param lastTime The end time of the ground truth chord progression, in milliseconds.
	 * @return The score of the transcribed chord progression, or NaN if the progression is empty.
	 */
	public double getScore(ChordProgression groundTruth, int lastTime) {
		if (groundTruth.chords.isEmpty()) {
			return Double.NaN;
		}
		
		List<Chord> transcriptionChords = getChords();
		List<Chord> groundTruthChords = groundTruth.getChords();
		
		double totalDuration = lastTime - groundTruthChords.get(0).time;
		int correctDuration = 0;
		
		for (int transcriptionIndex = 0; transcriptionIndex < transcriptionChords.size(); transcriptionIndex++) {
			// Go through each transcribed chord
			
			Chord transcriptionChord = transcriptionChords.get(transcriptionIndex);
			int nextTranscriptionChordTime = transcriptionIndex == transcriptionChords.size() - 1 ?
					lastTime : Math.min(lastTime, transcriptionChords.get(transcriptionIndex + 1).time);
			
			for (int groundTruthIndex = 0; groundTruthIndex < groundTruthChords.size(); groundTruthIndex++) {
				// Go through each ground truth chord
				
				Chord groundTruthChord = groundTruthChords.get(groundTruthIndex);
				int nextGroundTruthChordTime = groundTruthIndex == groundTruthChords.size() - 1 ?
						lastTime : Math.min(lastTime, groundTruthChords.get(groundTruthIndex + 1).time);
				
				// Find the overlap
				int overlapBeginning = Math.max(transcriptionChord.time, groundTruthChord.time);
				int overlapEnding = Math.min(nextTranscriptionChordTime, nextGroundTruthChordTime);
				
				// Check for valid overlap
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
	
	@Override
	public String toString() {
		return "Progression " + chords.toString();
	}
}
