package mv2h.tools;

import java.util.ArrayList;
import java.util.List;

/**
 * The <code>AlignmentNode</code> class is used to help when aligning musical scores
 * ({@link mv2h.opbjects.Music} objects). It implements a backwards-linked list of alignments,
 * and can be used to generate an alignment list at each node.
 *
 * @author Andrew McLeod
 */
public class AlignmentNode {
    /**
     * The previous AlignmentNode in the backwards-linked list.
     */
    public final AlignmentNode prev;

    /**
     * The value to add to the alignment list for this node. Use {@link #NO_ALIGNMENT}
     * for no aligned transcription note.
     */
    public final int value;

    /**
     * The value to use to designate no aligned transcription note.
     */
	public static final int NO_ALIGNMENT = -2;

    /**
     * Create a new AlignmentNode.
     *
     * @param prev {@link #prev}
     * @param value {@link #value}
     */
	public AlignmentNode(AlignmentNode prev, int value) {
		this.prev = prev;
		this.value = value;
	}

    /**
     * Generate the alignment list from the node.
     *
     * @return An alignment for this node.
     * An alignment is a list containing, for each ground truth note, the index of the transcription
	 * note to which it is aligned, or -1 if it was not aligned with any transcription note.
     */
	public List<Integer> getAlignment() {
		List<Integer> list = prev == null ? new ArrayList<Integer>() : prev.getAlignment();

		if (value != NO_ALIGNMENT) {
			list.add(value);
		}

		return list;
	}
}
