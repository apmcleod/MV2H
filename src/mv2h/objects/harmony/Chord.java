package mv2h.objects.harmony;

import java.io.IOException;

/**
 * A <code>Chord</code> object represents a single chord at a given time. They are ordered by increasing
 * time in a {@link ChordProgression}. Each chord is stored as a String, to allow for different vocabularies.
 * 
 * @author Andrew McLeod
 */
public class Chord implements Comparable<Chord> {
	/**
	 * The start time of this chord, in milliseconds.
	 */
	public final int time;
	
	/**
	 * The String representation of this chord.
	 */
	public final String chord;
	
	/**
	 * Create a new chord.
	 * 
	 * @param chord {@link #chord}
	 * @param time {@link #time}
	 */
	public Chord(String chord, int time) {
		this.time = time;
		this.chord = chord;
	}
	
	/**
	 * Decide whether this chord matches the given one.
	 * 
	 * @param chord The chord to check for a match.
	 * @return True if the chords are equal (disregarding time). False otherwise.
	 */
	public boolean matches(Chord chord) {
		return this.chord.equals(chord.chord);
	}
	
	@Override
	public String toString() {
		return "Chord " + time + " " + chord;
	}

	@Override
	public int compareTo(Chord o) {
		int result = Integer.compare(time, o.time);
		if (result != 0) {
			return result;
		}
		
		result = chord.compareTo(o.chord);
		if (result != 0) {
			return result;
		}
		
		return 0;
	}

	/**
	 * Parse a chord from the given string. The string should be of the form
	 * <code>Chord time chord</code>.
	 * 
	 * @param line The string to parse.
	 * @return The parsed chord.
	 * 
	 * @throws IOException If the String has invalid format.
	 */
	public static Chord parseChord(String line) throws IOException {
		String[] chordSplit = line.split(" ");
		
		if (chordSplit.length != 3) {
			throw new IOException("Error parsing Chord: " + line);
		}
		
		int time;
		try {
			time = Integer.parseInt(chordSplit[1]);
		} catch (NumberFormatException e) {
			throw new IOException("Error parsing Chord. Time not recognised: " + line);
		}
		
		String chord = chordSplit[2];
		
		return new Chord(chord, time);
	}
}
