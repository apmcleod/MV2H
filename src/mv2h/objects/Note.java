package mv2h.objects;

import java.io.IOException;

import mv2h.Main;

/**
 * A <code>Note</code> represents a musical note with pitch, onset and offset time, and a specific
 * voice. Notes are ordered by increasing onset time, followed by pitch, voice, and offset time.
 * 
 * @author Andrew McLeod
 */
public class Note implements Comparable<Note> {
	/**
	 * The pitch of this note.
	 */
	public final int pitch;
	
	/**
	 * The onset time of this note, in milliseconds.
	 */
	public final int onsetTime;
	
	/**
	 * The time of the tatum at which this note's onset is quantized to in the score.
	 */
	public final int valueOnsetTime;
	
	/**
	 * The time of the tatum at which this note's offset is quantized to, according to its note value.
	 */
	public final int valueOffsetTime;
	
	/**
	 * The voice to which this note is assigned.
	 */
	public final int voice;
	
	/**
	 * Create a new note.
	 * 
	 * @param pitch {@link #pitch}
	 * @param onsetTime {@link #onsetTime}
	 * @param valueOnsetTime {@link #valueOnsetTime}
	 * @param valueOffsetTime {@link #valueOffsetTime}
	 * @param voice {@link #voice}
	 */
	public Note(int pitch, int onsetTime, int valueOnsetTime, int valueOffsetTime, int voice) {
		this.pitch = pitch;
		this.onsetTime = onsetTime;
		this.valueOnsetTime = valueOnsetTime;
		this.valueOffsetTime = valueOffsetTime;
		this.voice = voice;
	}
	
	/**
	 * Decide whether this note matches a given note. That is, whether their pitches are equal and their
	 * onset times are within {@link Main#ONSET_DELTA} milliseconds.
	 * 
	 * @param note The note we are checking for a match.
	 * @return True if the notes match. False otherwise.
	 */
	public boolean matches(Note note) {
		return pitch == note.pitch && Math.abs(onsetTime - note.onsetTime) <= Main.ONSET_DELTA;
	}
	
	/**
	 * Get the value score of this note compared to the given ground truth. That is, 1 minus the
	 * difference in quantized duration, as a proportion of the ground truth note's quantized
	 * duration, or 0 if that value is negative.
	 * 
	 * @param groundTruth The ground truth note.
	 * @return The value score of this note.
	 */
	public double getValueScore(Note groundTruth) {
		double transcriptionDuration = valueOffsetTime - valueOnsetTime;
		double groundTruthDuration = groundTruth.valueOffsetTime - groundTruth.valueOnsetTime;
		
		double diff = Math.abs(transcriptionDuration - groundTruthDuration);
		
		if (diff <= Main.DURATION_DELTA) {
			return 1.0;
		}
		
		return Math.max(0.0, 1.0 - diff / groundTruthDuration);
	}
	
	@Override
	public String toString() {
		return "Note " + pitch + " " + onsetTime + " " + valueOnsetTime + " " + valueOffsetTime + " " + voice;
	}

	@Override
	public int compareTo(Note o) {
		int result = Integer.compare(valueOnsetTime, o.valueOnsetTime);
		if (result != 0) {
			return result;
		}
		
		result = Integer.compare(pitch, o.pitch);
		if (result != 0) {
			return result;
		}
		
		result = Integer.compare(onsetTime, o.onsetTime);
		if (result != 0) {
			return result;
		}
		
		result = Integer.compare(voice, o.voice);
		if (result != 0) {
			return result;
		}
		
		result = Integer.compare(valueOffsetTime, o.valueOffsetTime);
		if (result != 0) {
			return result;
		}
		
		return 0;
	}

	/**
	 * Parse a note from the given string. It should be in the form <code>Note pitch onsetTime
	 * valueOnsetTime valueOffsetTime voice</code>.
	 * 
	 * @param line The string to parse.
	 * @return The parsed note.
	 * 
	 * @throws IOException If the given string was malformed.
	 */
	public static Note parseNote(String line) throws IOException {
		String[] noteSplit = line.split(" ");
		
		if (noteSplit.length != 6) {
			throw new IOException("Error parsing Note. Note enough fields: " + line);
		}
		
		int pitch;
		try {
			pitch = Integer.parseInt(noteSplit[1]);
		} catch (NumberFormatException e) {
			throw new IOException("Error parsing Note. Pitch not recognised: " + line);
		}
		
		int onsetTime;
		try {
			onsetTime = Integer.parseInt(noteSplit[2]);
		} catch (NumberFormatException e) {
			throw new IOException("Error parsing Note. Onset time not recognised: " + line);
		}
		
		int valueOnsetTime;
		try {
			valueOnsetTime = Integer.parseInt(noteSplit[3]);
		} catch (NumberFormatException e) {
			throw new IOException("Error parsing Note. Value onset time not recognised: " + line);
		}
		
		int valueOffsetTime;
		try {
			valueOffsetTime = Integer.parseInt(noteSplit[4]);
		} catch (NumberFormatException e) {
			throw new IOException("Error parsing Note. Value offset time not recognised: " + line);
		}
		
		int voice;
		try {
			voice = Integer.parseInt(noteSplit[5]);
		} catch (NumberFormatException e) {
			throw new IOException("Error parsing Note. Voice not recognised: " + line);
		}
		
		return new Note(pitch, onsetTime, valueOnsetTime, valueOffsetTime, voice);
	}
}
