package mv2h.tools.midi;

/**
 * A <code>Tempo</code> object tracks the speed of the music data. That is, the rate at which quarter notes occur.
 * 
 * @author Andrew McLeod - 11 Feb, 2015
 */
public class Tempo {

	/**
	 * The number of microseconds which pass per quarter note in the song.
	 */
	private final int microSecondsPerQuarter;
	
	/**
	 * Create a default tempo - 120 QPM
	 */
	public Tempo() {
		microSecondsPerQuarter = 500000;
	}
	
	/**
	 * Create a new Tempo from the given data array.
	 * 
	 * @param data
	 * @see #calculateMicroSecondsPerQuarter(byte[])
	 */
	public Tempo(byte[] data) {
		microSecondsPerQuarter = calculateMicroSecondsPerQuarter(data);
	}

	/**
	 * Gets the number of milliseconds per quarter note at this tempo.
	 * 
	 * @return {@link #microSecondsPerQuarter} converted to milliseconds.
	 */
	public double getMillisPerQuarter() {
		return microSecondsPerQuarter / 1000.0;
	}
	
	/**
     * Calculate the number of microseconds per quarter note based on the given data array. It is really just an int,
     * where the highest byte is data[0], and the lowest byte is data[3].
     * 
     * @param data The Midi byte array of the tempo data for microseconds/quarter. It is actually just represented as an int,
     * but it is grabbed from the file as a byte array, so we need this conversion.
     * @return The number of microseconds per quarter note of the given data
     */
    public static int calculateMicroSecondsPerQuarter(byte[] data) {
    	int tpq = 0;
		
		for (int j = 0; j < data.length; j++) {
			int byteNumber = data.length - 1 - j;
			
			// Grab the lowest byte of the casted int, and shift it into the proper int byte location
			tpq += (0x000000ff & data[j]) << (8 * byteNumber);
		}
		
		return tpq;
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Tempo)) {
			return false;
		}
		
		Tempo t = (Tempo) other;
		
		return getMillisPerQuarter() == t.getMillisPerQuarter();
	}
}
