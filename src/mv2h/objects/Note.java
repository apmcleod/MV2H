package mv2h.objects;

import java.io.IOException;

public class Note implements Comparable<Note> {
	private static final int ONSET_DELTA = 50;
	private static final int DURATION_DELTA = 100;
	
	public final int pitch;
	public final int onsetTime;
	public final int valueOnsetTime;
	public final int valueOffsetTime;
	public final int voice;
	
	public Note(int pitch, int onsetTime, int valueOnsetTime, int valueOffsetTime, int voice) {
		this.pitch = pitch;
		this.onsetTime = onsetTime;
		this.valueOnsetTime = valueOnsetTime;
		this.valueOffsetTime = valueOffsetTime;
		this.voice = voice;
	}
	
	public boolean matches(Note note) {
		return pitch == note.pitch && Math.abs(onsetTime - note.onsetTime) <= ONSET_DELTA;
	}
	
	public double getValueScore(Note groundTruth) {
		double transcriptionDuration = valueOffsetTime - valueOnsetTime;
		double groundTruthDuration = groundTruth.valueOffsetTime - groundTruth.valueOnsetTime;
		
		double diff = Math.abs(transcriptionDuration - groundTruthDuration);
		
		if (diff <= DURATION_DELTA) {
			return 1.0;
		}
		
		return Math.max(0.0, 1.0 - diff / groundTruthDuration);
	}
	
	public String toString() {
		return "Note " + pitch + " " + onsetTime + " " + valueOnsetTime + " " + valueOffsetTime + " " + voice;
	}

	@Override
	public int compareTo(Note o) {
		int result = Integer.compare(onsetTime, o.onsetTime);
		if (result != 0) {
			return result;
		}
		
		result = Integer.compare(voice, o.voice);
		if (result != 0) {
			return result;
		}
		
		result = Integer.compare(pitch, o.pitch);
		if (result != 0) {
			return result;
		}
		
		result = Integer.compare(valueOnsetTime, o.valueOnsetTime);
		if (result != 0) {
			return result;
		}
		
		result = Integer.compare(valueOffsetTime, o.valueOffsetTime);
		if (result != 0) {
			return result;
		}
		
		return 0;
	}

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
