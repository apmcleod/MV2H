package mv2h.objects.meter;

import java.io.IOException;

/**
 * A <code>Tatum</code> represents a single point in time in a musical score. They are ordered by
 * increasing {@link #time}. See {@link Meter}.
 * 
 * @author Andrew McLeod
 */
public class Tatum implements Comparable<Tatum> {
	/**
	 * The time of this tatum in milliseconds.
	 */
	public final int time;

	/**
	 * Create a new tatum at the given time.
	 * 
	 * @param time {@link #time}
	 */
	public Tatum(int time) {
		this.time = time;
	}
	
	@Override
	public String toString() {
		return "Tatum " + time;
	}

	@Override
	public int compareTo(Tatum o) {
		int result = Integer.compare(time, o.time);
		if (result != 0) {
			return result;
		}
		
		return 0;
	}

	/**
	 * Parse a tatum from the given string. It should be of the form <code>Tatum time</code>.
	 * 
	 * @param line The string from which to parse a tatum.
	 * @return The parsed Tatum.
	 * 
	 * @throws IOException If the given string was malformed.
	 */
	public static Tatum parseTatum(String line) throws IOException {
		String[] tatumSplit = line.split(" ");
		
		if (tatumSplit.length != 2) {
			throw new IOException("Error parsing Tatum: " + line);
		}
		
		int time;
		try {
			time = Integer.parseInt(tatumSplit[1]);
		} catch (NumberFormatException e) {
			throw new IOException("Error parsing Tatum. Time not recognised: " + line);
		}
		
		return new Tatum(time);
	}
}
