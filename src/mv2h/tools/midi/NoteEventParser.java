package mv2h.tools.midi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;

import mv2h.objects.Note;

/**
 * A <code>NoteEventParser</code> parses Note On and Note Off events and generates a List of
 * the notes present in any given song. 
 * 
 * @author Andrew McLeod - 11 Feb, 2015
 */
public class NoteEventParser {
	/**
	 * A list of the Notes which have not yet been closed.
	 */
	private LinkedList<Note> activeNotes;
	
	/**
	 * A list of Notes which have already been closed.
	 */
	private List<Note> completedNotes;
	
	/**
	 * The TimeTracker for this NoteEventParser.
	 */
	protected TimeTracker timeTracker;
	
	/**
	 * Creates a new NoteEventParser with the given TimeTracker.
	 * 
	 * @param timeTracker
	 */
	public NoteEventParser(TimeTracker timeTracker) {
		activeNotes = new LinkedList<Note>();
		completedNotes = new ArrayList<Note>();
		
		this.timeTracker = timeTracker;
	}
	
	/**
     * Process a Note On event.
     * 
     * @param key The key pressed. This will be a number between 1 and 88 for piano.
     * @param velocity The velocity of the press. This is a value between 1 and 127 inclusive.
     * @param tick The midi tick location of this event.
     * @param channel The midi channel this note came from.
	 * @return The MidiNote we just created.
     */
	public Note noteOn(int key, int velocity, long tick, int channel) {
		long time = Math.round(timeTracker.getTimeAtTick(tick));
		
		Note note = new Note(key, (int) time, (int) time, -1, channel);
		
		activeNotes.add(note);
		
		return note;
	}

	/**
     * Process a Note Off event.
     * 
     * @param key The midi key which has been turned off. This value is between 39 and 127, inclusive, for piano.
     * @param tick The midi tick location of this event.
     * @param channel The midi channel this note came from.
     * @throws InvalidMidiDataException If a note off event doesn't match any previously seen note on events.
     */
	public void noteOff(int key, long tick, int channel) throws InvalidMidiDataException {
		long time = Math.round(timeTracker.getTimeAtTick(tick));
		Iterator<Note> iterator = activeNotes.iterator();
		
		while (iterator.hasNext()) {
			Note note = iterator.next();
			
			if (note.pitch == key && note.voice == channel) {
				iterator.remove();
				completedNotes.add(new Note(note.pitch, note.onsetTime, note.valueOnsetTime, (int) time, note.voice));
				return;
			}
		}
		
		// Note off event didn't match any active notes.
		throw new InvalidMidiDataException("Note off event doesn't match any note on: " +
				"pitch=" + key + ", tick=" + tick + " voice=" + channel);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Note note : completedNotes) {
			sb.append(note.toString()).append('\n');
		}
		return sb.toString();
	}
}
