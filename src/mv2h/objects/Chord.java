package mv2h.objects;

import java.io.IOException;

public class Chord implements Comparable<Chord> {
	public final int time;
	public final String chord;
	
	public Chord(int time, String chord) {
		this.time = time;
		this.chord = chord;
	}
	
	public boolean matches(Chord chord) {
		return this.chord.equals(chord.chord);
	}
	
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
		
		return new Chord(time, chord);
	}
}
