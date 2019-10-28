package mv2h.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * A <code>Voice</code> represents a list of unique {@link Note}s, sorted by increasing value onset time.
 * 
 * @author Andrew McLeod
 */
public class Voice {
	/**
	 * An ordered, unique set of notes.
	 */
	public final SortedSet<Note> notes;
	
	/**
	 * A mapping of onset_offset times to NoteClusters.
	 */
	public final SortedMap<String, NoteCluster> noteClusters;
	
	/**
	 * Create a new, empty voice.
	 */
	public Voice() {
		notes = new TreeSet<Note>();
		noteClusters = new TreeMap<String, NoteCluster>();
	}
	
	/**
	 * Create the connections graph for the note clusters in this voice.
	 * <br>
	 * Each note cluster is connected to:
	 * <ol>
	 *   <li>Every note cluster which begins at this one's offset time.
	 *   <li>All note clusters at the earliest time after this one's offset time, if no
	 *       connections were added from rule (1).
	 * </ol>
	 */
	public void createConnections() {
		for (Map.Entry<String, NoteCluster> base : noteClusters.entrySet()) {
			String baseKey = base.getKey();
			NoteCluster baseCluster = base.getValue();
			
			for (Map.Entry<String, NoteCluster> next : noteClusters.tailMap(baseKey).entrySet()) {
				String nextKey = next.getKey();
				NoteCluster nextCluster = next.getValue();
				
				// Skip first entry (equal)
				if (nextKey.equals(baseKey)) {
					continue;
				}
				
				// Falls under case 1 in javadoc
				if (nextCluster.onsetTime == baseCluster.offsetTime) {
					baseCluster.addNextCluster(nextCluster);
					
				// We are passed all case (1)s from javadoc
				} else if (nextCluster.onsetTime > baseCluster.offsetTime) {
					
					// Falls under case 2 in javadoc
					if (baseCluster.nextClusters.isEmpty() || baseCluster.nextClusters.get(0).onsetTime == nextCluster.onsetTime) {
						baseCluster.addNextCluster(nextCluster);
						
					// We are passed all case (2)s from javadoc
					} else {
						// Break to next base
						break;
					}
				}
			}
		}
	}
	
	/**
	 * Add a note to the voice.
	 * 
	 * @param note The note to add.
	 */
	public void addNote(Note note) {
		notes.add(note);
		
		String key = (new NoteCluster(note.valueOnsetTime, note.valueOffsetTime)).getKeyString();
		NoteCluster cluster = noteClusters.get(key);
		if (cluster == null) {
			cluster = new NoteCluster(note.valueOnsetTime, note.valueOffsetTime);
			noteClusters.put(key, cluster);
		}
		cluster.addNote(note);
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
