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
     * A List of previous AlignmentNodes in the backwards-linked list.
     */
    public final List<AlignmentNode> prevList;

    /**
     * The value to add to the alignment list for this node. Use {@link #NO_ALIGNMENT}
     * for no aligned transcription note.
     */
    public final int value;

    /**
     * How many alignment lists pass through this node.
     */
    public final int count;

    /**
     * Create a new AlignmentNode.
     *
     * @param prevList {@link #prevList}
     * @param value {@link #value}
     */
	public AlignmentNode(List<AlignmentNode> prevList, int value) {
		this.prevList = prevList;
        this.value = value;

        int count = 0;
        for (AlignmentNode prev : this.prevList) {
            count += prev.count;
        }
        this.count = Math.max(count, 1);
	}

    /**
     * Generate an alignment list from the node.
     *
     * @param index The index of the alignment node to return (since multiple lists pass
     * through this node).
     *
     * @return An alignment for this node.
     * An alignment is a list containing, for each ground truth note, the index of the transcription
	 * note to which it is aligned, or -1 if it was not aligned with any transcription note.
     */
	public List<Integer> getAlignment(int index) {
        List<Integer> alignment = null;

        if (prevList.isEmpty()) {
            // Base case
            alignment = new ArrayList<Integer>();

        } else {
            // Find the correct previous node based on the index
            for (AlignmentNode prev : prevList) {
                if (index < prev.count) {
                    // Previous node found. Get prev list.
                    alignment = prev.getAlignment(index);
                    break;
                }

                // Previous node not yet found. Decrememnt index and find the previous list.
                index -= prev.count;
            }
        }

        alignment.add(value);

		return alignment;
	}
}
