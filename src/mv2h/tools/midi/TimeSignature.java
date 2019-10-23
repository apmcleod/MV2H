package mv2h.tools.midi;

import mv2h.objects.meter.Hierarchy;

/**
 * A <code>TimeSignature</code> represents some MIDI data's beat structure (time signature).
 * Equality is based only on the numerator and denominator.
 * 
 * @author Andrew McLeod - 11 Feb, 2015
 */
public class TimeSignature {
	
	/**
	 * The numerator used to signify an irregular meter. It can be used with any denominator (4, for example).
	 */
	public static final int IRREGULAR_NUMERATOR = Integer.MAX_VALUE / 32;

	/**
	 * The numerator of the time signature.
	 */
	private final int numerator;
	
	/**
	 * The denominator of the time signature.
	 */
	private final int denominator;
	
	/**
	 * Create a new default TimeSignature (4/4 time)
	 */
	public TimeSignature() {
		this(new byte[] {4, 2, 24, 8});
	}
	
	/**
	 * Create a new TimeSignature from the given data array.
	 * 
	 * @param data Data array, parsed directly from midi.
	 */
	public TimeSignature(byte[] data) {
		numerator = data[0];
		denominator = (int) Math.pow(2, data[1]);
	}
	
	/**
	 * Create a new TimeSignature with the given numerator and denominator.
	 * Using this, {@link #metronomeTicksPerBeat} will be 24, and {@link #notes32PerQuarter}
	 * will be 8.
	 * 
	 * @param numerator {@link #numerator}
	 * @param denominator {@link #denominator}
	 */
	public TimeSignature(int numerator, int denominator) {
		this.numerator = numerator;
		this.denominator = denominator;
	}
	
	/**
	 * Get the numerator of this time signature.
	 * 
	 * @return {@link #numerator}
	 */
	public int getNumerator() {
		return numerator;
	}
	
	/**
	 * Get the denominator of this time signature.
	 * 
	 * @return {@link #denominator}
	 */
	public int getDenominator() {
		return denominator;
	}
	
	/**
	 * Get the number of sub beats per quarter note.
	 * 
	 * @return The number of sub beats per quarter note.
	 */
	public int getSubBeatsPerQuarter() {
		// Simple meter
		if (numerator <= 4 || numerator % 3 != 0) {
			return denominator / 2;
			
		// Compound meter
		} else {
			return denominator / 4;
		}
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof TimeSignature)) {
			return false;
		}
		
		TimeSignature ts = (TimeSignature) other;
		
		return getDenominator() == ts.getDenominator() && getNumerator() == ts.getNumerator();
	}

	/**
	 * Get the Hierarchy of this time signature.
	 * @param time The time of this hierarchy.
	 * @param anacrusisLengthSubBeats The length of this Hierarchy's anacrusis, in sub beats.
	 * 
	 * @return The Hierarchy of this time signature.
	 */
	public Hierarchy getHierarchy(int time, int anacrusisLengthSubBeats) {
		int beatsPerMeasure = numerator;
		int subBeatsPerBeat = 2;
		
		// Check for compound
		if (numerator > 3 && numerator % 3 == 0) {
			beatsPerMeasure = numerator / 3;
			subBeatsPerBeat = 3;	
		}
		
		Hierarchy hierarchy = new Hierarchy(beatsPerMeasure, subBeatsPerBeat, 1, anacrusisLengthSubBeats, time);
		
		return hierarchy;
	}
}