package mv2h.objects.meter;

import java.io.IOException;

/**
 * A <code>Hierarchy</code> object represents the metrical structure of a single bar, and its anacrusis length.
 * See {@link Meter}. 
 * 
 * @author Andrew McLeod
 */
public class Hierarchy {
	/**
	 * The number of beats per bar in this metrical structure.
	 */
	public final int beatsPerBar;
	
	/**
	 * The number of sub beats per beat in this metrical structure.
	 */
	public final int subBeatsPerBeat;
	
	/**
	 * The number of tatums per sub beat in this metrical structure.
	 */
	public final int tatumsPerSubBeat;
	
	/**
	 * The length of the anacrusis of this metrical structure, in tatums.
	 */
	public final int anacrusisLengthTatums;
	
	/**
	 * Create a new hierarchy with the given fields.
	 * 
	 * @param beatsPerBar {@link #beatsPerBar}
	 * @param subBeatsPerBeat {@link #subBeatsPerBeat}
	 * @param tatumsPerSubBeat {@link #tatumsPerSubBeat}
	 * @param anacrusisLengthTatums {@link #anacrusisLengthTatums}
	 */
	public Hierarchy(int beatsPerBar, int subBeatsPerBeat, int tatumsPerSubBeat, int anacrusisLengthTatums) {
		this.beatsPerBar = beatsPerBar;
		this.subBeatsPerBeat = subBeatsPerBeat;
		this.tatumsPerSubBeat = tatumsPerSubBeat;
		this.anacrusisLengthTatums = anacrusisLengthTatums;
	}
	
	@Override
	public String toString() {
		return "Hierarchy " + beatsPerBar + "," + subBeatsPerBeat + " " + tatumsPerSubBeat + " a=" + anacrusisLengthTatums;
	}

	/**
	 * Parse a hierarchy definition from the given line.
	 * <br>
	 * It should be of the form
	 * <code>Hierarchy beatsPerBar,subBeatsPerBeat tatumsPerSubBeat a=anacrusisLengthTatums</code>.
	 * 
	 * @param line The hierarchy to parse.
	 * @return The parsed Hierarchy.
	 * 
	 * @throws IOException If the line was malformed.
	 */
	public static Hierarchy parseHierarchy(String line) throws IOException {
		String[] hierarchySplit = line.split(" ");
		
		if (hierarchySplit.length != 4) {
			throw new IOException("Error parsing hierarchy: " + line);
		}
		
		String[] meterSplit = hierarchySplit[1].split(",");
		
		if (meterSplit.length != 2) {
			throw new IOException("Error parsing hierarchy: " + line);
		}
		
		int beatsPerBar;
		try {
			beatsPerBar = Integer.parseInt(meterSplit[0]);
		} catch (NumberFormatException e) {
			throw new IOException("Error parsing Hierarchy. Beats per bar not recognised: " + line);
		}
		
		int subBeatsPerBeat;
		try {
			subBeatsPerBeat = Integer.parseInt(meterSplit[1]);
		} catch (NumberFormatException e) {
			throw new IOException("Error parsing Hierarchy. Sub beats per beat not recognised: " + line);
		}
		
		int tatumsPerSubBeat;
		try {
			tatumsPerSubBeat = Integer.parseInt(hierarchySplit[2]);
		} catch (NumberFormatException e) {
			throw new IOException("Error parsing Hierarchy. Tatums per sub beat not recognised: " + line);
		}
		
		if (!hierarchySplit[3].startsWith("a=")) {
			throw new IOException("Error parsing Hierarchy. Anacrusis length not recognised: " + line);
		}
		
		int anacrusisLengthTatums;
		try {
			anacrusisLengthTatums = Integer.parseInt(hierarchySplit[3].substring(2));
		} catch (NumberFormatException e) {
			throw new IOException("Error parsing Hierarchy. Anacrusis length not recognised: " + line);
		}
		
		return new Hierarchy(beatsPerBar, subBeatsPerBeat, tatumsPerSubBeat, anacrusisLengthTatums);
	}
}
