package mv2h.objects.meter;

import java.io.IOException;

public class Hierarchy {
	public final int beatsPerBar;
	public final int subBeatsPerBeat;
	public final int tatumsPerSubBeat;
	public final int anacrusisLengthTatums;
	
	public Hierarchy(int beatsPerBar, int subBeatsPerBeat, int tatumsPerSubBeat, int anacrusisLengthTatums) {
		this.beatsPerBar = beatsPerBar;
		this.subBeatsPerBeat = subBeatsPerBeat;
		this.tatumsPerSubBeat = tatumsPerSubBeat;
		this.anacrusisLengthTatums = anacrusisLengthTatums;
	}
	
	public String toString() {
		return "Hierarchy " + beatsPerBar + "," + subBeatsPerBeat + " " + tatumsPerSubBeat + " a=" + anacrusisLengthTatums;
	}

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
