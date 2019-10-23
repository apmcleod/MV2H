package mv2h.tools;

import java.io.File;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;

import mv2h.objects.harmony.Key;
import mv2h.tools.midi.MidiEventParser;
import mv2h.tools.midi.NoteEventParser;
import mv2h.tools.midi.TimeTracker;

/**
 * The <code>MidiConverter</code> class is used to convert a given MIDI file
 * into a format that can be read by the MV2H package (using the toString() method).
 * 
 * @author Andrew McLeod
 */
public class MidiConverter extends Converter {
	
	/**
	 * This converter's time tracker.
	 */
	private final TimeTracker tt;
	
	/**
	 * This converter's note event parser.
	 */
	private final NoteEventParser nep;

	/**
	 * Create a new MidiConverter object by parsing the given MIDI file.
	 * <br>
	 * This method contains the main program logic, and printing is handled by
	 * {@link #toString()}.
	 * 
	 * @param file The MIDI file to convert.
	 * @param anacrusis The anacrusis (pick-up) length, in sub beats.
	 * @param useChannel True to use channels as ground truth voices. False to use tracks.
	 * @throws InvalidMidiDataException 
	 */
	public MidiConverter(File file, int anacrusis, boolean useChannel) throws IOException, InvalidMidiDataException {
		tt = new TimeTracker();
		tt.setAnacrusis(anacrusis);
		nep = new NoteEventParser(tt);
		MidiEventParser mep = new MidiEventParser(file, nep, tt, useChannel);
		mep.run();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		// Add notes
		sb.append(nep.toString());
		
		// Add key signatures
		for (Key key : tt.getAllKeySignatures()) {
			sb.append(key.toString()).append('\n');
		}
		
		// Add hierarchies and tatums
		sb.append(tt.getMeter().toString());
		
		return sb.toString();
	}
}
