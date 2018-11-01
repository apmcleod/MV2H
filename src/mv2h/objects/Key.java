package mv2h.objects;

import java.io.IOException;

public class Key implements Comparable<Key> {
	public final int tonic;
	public final boolean isMajor;
	public final int time;
	
	public Key(int tonic, boolean isMajor, int startTime) {
		this.tonic = tonic;
		this.isMajor = isMajor;
		this.time = startTime;
	}
	
	public Key(int tonic, boolean isMajor) {
		this.tonic = tonic;
		this.isMajor = isMajor;
		time = 0;
	}
	
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
		int result = Integer.compare(time, o.time);
		if (result != 0) {
			return result;
		}
		
		result = Integer.compare(tonic, o.tonic);
		if (result != 0) {
			return result;
		}
		
		return Boolean.compare(isMajor, o.isMajor);
	}
	
	public String toString() {
		return "Key " + tonic + " " + (isMajor ? "Maj" : "min") + " " + time;
	}

	public static Key parseKey(String line) throws IOException {
		String[] keySplit = line.split(" ");
		
		if (keySplit.length < 3) {
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
