package mv2h.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class Voice {
	public final SortedSet<Note> notes;
	
	public Voice() {
		notes = new TreeSet<Note>();
	}
	
	public void addNote(Note note) {
		notes.add(note);
	}
	
	public List<Note> getNotes() {
		return new ArrayList<Note>(notes);
	}
	
	public String toString() {
		return "Voice " + notes.toString();
	}
}
