package mv2h.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A <code>Voice</code> represents a list of unique {@link Note}s, sorted by increasing onset time.
 * 
 * @author Andrew McLeod
 */
public class Voice {
	/**
	 * An ordered, unique set of notes.
	 */
	public final SortedSet<Note> notes;
	
	/**
	 * Create a new, empty voice.
	 */
	public Voice() {
		notes = new TreeSet<Note>();
	}
	
	/**
	 * Add a note to the voice.
	 * 
	 * @param note The note to add.
	 */
	public void addNote(Note note) {
		notes.add(note);
	}
	
	/**
	 * Get an ordered List of the notes in this voice.
	 * 
	 * @return {@link #notes}
	 */
	public List<Note> getNotes() {
		return new ArrayList<Note>(notes);
	}
	
	@Override
	public String toString() {
		return "Voice " + notes.toString();
	}
}
