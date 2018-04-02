package mv2h.objects;

import java.io.IOException;

public class Key {
	public final int tonic;
	public final boolean isMajor;
	
	public Key(int tonic, boolean isMajor) {
		this.tonic = tonic;
		this.isMajor = isMajor;
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
	
	public String toString() {
		return "Key " + tonic + " " + (isMajor ? "Maj" : "min");
	}

	public static Key parseKey(String line) throws IOException {
		String[] keySplit = line.split(" ");
		
		if (keySplit.length != 3) {
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
		
		return new Key(tonic, isMajor);
	}
}
