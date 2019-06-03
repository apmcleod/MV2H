package mv2h.objects.harmony;

import java.io.IOException;

/**
 * A <code>Key</code> object represents a musical key with a tonic, either major or minor mode,
 * and a start time. They are naturally ordered by increasing time.
 * <br>
 * See {@link KeyProgression}.
 * 
 * @author Andrew McLeod
 */
public class Key implements Comparable<Key> {
	/**
	 * The tonic of this key.
	 */
	public final int tonic;
	
	/**
	 * True if this key is major. False for minor.
	 */
	public final boolean isMajor;
	
	/**
	 * The starting time of this key, in milliseconds.
	 */
	public final int time;
	
	/**
	 * Create a new Key.
	 * 
	 * @param tonic {@link #tonic}
	 * @param isMajor {@link #isMajor}
	 * @param startTime {@link #time}
	 */
	public Key(int tonic, boolean isMajor, int startTime) {
		this.tonic = tonic;
		this.isMajor = isMajor;
		this.time = startTime;
	}
	
	/**
	 * Create a new Key at time 0.
	 * 
	 * @param tonic {@link #tonic}
	 * @param isMajor {@link #isMajor}
	 */
	public Key(int tonic, boolean isMajor) {
		this(tonic, isMajor, 0);
	}
	
	/**
	 * Get the score of a transcribed key given some ground truth.
	 * 
	 * @param groundTruth The ground truth key.
	 * 
	 * @return The score of the transcribed key. 1 for a perfect match, 0.5 for correct
	 * mode but tonic off by a perfect 5th, 0.3 for relative major or minor(CM, am), 0.2 for
	 * parallel major or minor (CM, cm), and 0 otherwise.
	 */
	public double getScore(Key groundTruth) {
		// Correct
		if (tonic == groundTruth.tonic && isMajor == groundTruth.isMajor) {
			return 1.0;
		}
		
		// Perfect fifth higher
		if (isMajor == groundTruth.isMajor && tonic == (groundTruth.tonic + 7) % 12) {
			return 0.5;
		}
		
		// Perfect fifth lower
		if (isMajor == groundTruth.isMajor && tonic == (groundTruth.tonic + 5) % 12) {
			return 0.5;
		}
		
		// Relative major
		if (isMajor && !groundTruth.isMajor && tonic == (groundTruth.tonic + 3) % 12) {
			return 0.3;
		}
		
		// Relative minor
		if (!isMajor && groundTruth.isMajor && groundTruth.tonic == (tonic + 3) % 12) {
			return 0.3;
		}
		
		// Parallel major/minor
		if (isMajor != groundTruth.isMajor && tonic == groundTruth.tonic) {
			return 0.2;
		}
		
		return 0.0;
	}
	
	@Override
	public int compareTo(Key o) {
		return Integer.compare(time, o.time);
	}
	
	@Override
	public String toString() {
		return "Key " + tonic + " " + (isMajor ? "Maj" : "min") + " " + time;
	}

	/**
	 * Parse a Key from a given string. The string should be of the form
	 * <code>Key tonic maj/min [time]</code>.
	 * 
	 * @param line The string to parse.
	 * @return The parsed key.
	 * 
	 * @throws IOException If the string was of an invalid format.
	 */
	public static Key parseKey(String line) throws IOException {
		String[] keySplit = line.split(" ");
		
		if (keySplit.length < 3 || keySplit.length > 4) {
			throw new IOException("Error parsing Key: " + line);
		}
		
		int tonic;
		try {
			tonic = Integer.parseInt(keySplit[1]);
		} catch (NumberFormatException e) {
			throw new IOException("Error parsing Key. Tonic not recognised: " + line);
		}
		
		boolean isMajor;
		if (keySplit[2].equalsIgnoreCase("maj")) {
			isMajor = true;
		} else if (keySplit[2].equalsIgnoreCase("min")) {
			isMajor = false;
		} else {
			throw new IOException("Error parsing Key. Major/minor not recognised: " + line);
		}
		
		int time = 0;
		if (keySplit.length >= 4) {
			try {
				time = Integer.parseInt(keySplit[3]);
			} catch (NumberFormatException e) {
				throw new IOException("Error parsing Key. Start time not recognised: " + line);
			}
		}
		
		return new Key(tonic, isMajor, time);
	}
}
