package mv2h.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * A <code>NoteCluster</code> represents a set of Notes which share a value onset time.
 * This is used for homophonic voice separation evaluation.
 * 
 * @author Andrew McLeod
 */
public class NoteCluster implements Comparable<NoteCluster> {

	/**
	 * The value onset time of the notes in this cluster.
	 */
	public final int onsetTime;
	
	/**
	 * The value offset time of the notes in this cluster.
	 */
	public final int offsetTime;
	
	/**
	 * A List of the notes in this cluster.
	 */
	public final List<Note> notes;
	
	/**
	 * A List of the NoteClusters that follow this cluster.
	 */
	public final List<NoteCluster> nextClusters;
	
	/**
	 * Create a new NoteCluster at the given time.
	 * 
	 * @param onsetTime {@link #onsetTime}
	 * @param offsetTime {@link #offsetTime}
	 */
	public NoteCluster(int onsetTime, int offsetTime) {
		this.onsetTime = onsetTime;
		this.offsetTime = offsetTime;
		
		notes = new ArrayList<Note>();
		nextClusters = new ArrayList<NoteCluster>();
	}
	
	/**
	 * Add a note to this cluster.
	 * 
	 * @param note The note to add to this cluster.
	 */
	public void addNote(Note note) {
		notes.add(note);
	}
	
	/**
	 * Add a NoteCluster as one that directly follows this one.
	 * 
	 * @param cluster The cluster which follows this one.
	 */
	public void addNextCluster(NoteCluster cluster) {
		nextClusters.add(cluster);
	}
	
	public String getKeyString() {
		StringBuilder sb = new StringBuilder("");
		
		// Onset time, padded with 0 until MAX_INT
		int desiredLength = Integer.toString(Integer.MAX_VALUE).length();
		for (int i = 0; i < desiredLength - Integer.toString(onsetTime).length(); i++) {
			sb.append('0');
		}
		sb.append(Integer.toString(onsetTime)).append('_');
		
		// Offset time, padded with 0 until MAX_INT
		for (int i = 0; i < desiredLength - Integer.toString(offsetTime).length(); i++) {
			sb.append('0');
		}
		sb.append(Integer.toString(offsetTime));
		
		return sb.toString();
	}

	@Override
	public int compareTo(NoteCluster o) {
		int result = Integer.compare(onsetTime, o.onsetTime);
		if (result != 0) {
			return result;
		}
		
		return Integer.compare(offsetTime, o.offsetTime);
	}
}
