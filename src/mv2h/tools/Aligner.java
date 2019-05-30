package mv2h.tools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import mv2h.Main;
import mv2h.objects.Music;
import mv2h.objects.Note;

/**
 * The <code>Aligner</code> class is used to align to musical scores ({@link mv2h.opbjects.Music} objects).
 * All of its methods are static, and it uses a heuristic-based dynamic time warp to get a number of
 * candidate alignments {@link #getPossibleAlignments(Music, Music)}, and can be used to convert
 * the times of a transcription based on one of those alignments
 * {@link #convertTime(int, Music, Music, List)}. 
 * 
 * @author Andrew McLeod
 */
public class Aligner {
	
	/**
	 * Get all possible alignments of the given ground truth and transcription.
	 * 
	 * @param gt The ground truth.
	 * @param m The transcription.
	 * 
	 * @return A Set of all possible alignments of the transcription to the ground truth.
	 * An alignment is a list containing, for each transcribed note list, the index of the ground
	 * truth note list to which it is aligned, or -1 if it was not aligned with any ground truth note.
	 */
	public static Set<List<Integer>> getPossibleAlignments(Music gt, Music m) {
		double[][] distances = getAlignmentMatrix(gt.getNoteLists(), m.getNoteLists());
		return getPossibleAlignmentsFromMatrix(distances.length - 1, distances[0].length - 1, distances);
	}
	
	/**
	 * A recursive function to get all of the possible alignments which lead to
	 * the optimal distance from the distance matrix returned by the heuristic-based DTW in
	 * {@link #getAlignmentMatrix(List, List)}, up to matrix indices i, j.
	 * 
	 * @param i The first index, representing the transcribed note index.
	 * @param j The second index, representing the ground truth note index.
	 * @param distances The full distances matrix from {@link #getAlignmentMatrix(List, List)}.
	 * 
	 * @return A Set of all possible alignments given the distance matrix, up to notes i, j.
	 * An alignment is a list containing, for each transcribed note list, the index of the ground
	 * truth note list to which it is aligned, or -1 if it was not aligned with any ground truth note.
	 */
	private static Set<List<Integer>> getPossibleAlignmentsFromMatrix(int i, int j, double[][] distances) {
		Set<List<Integer>> alignments = new HashSet<List<Integer>>();
		
		// Base case. we are at the beginning and nothing else needs to be aligned.
		if (i == 1 && j == 1) {
			alignments.add(new ArrayList<Integer>());
			return alignments;
		}
		
		double min = Math.min(Math.min(
				distances[i - 1][j],
				distances[i][j - 1]),
				distances[i - 1][j - 1]);
		
		// Note that we could perform multiple of these if blocks.
		
		// This transcription note was aligned with nothing in the ground truth. Add -1.
		if (distances[i - 1][j] == min) {
			for (List<Integer> list : getPossibleAlignmentsFromMatrix(i - 1, j, distances)) {
				list.add(-1);
				alignments.add(list);
			}
		}
		
		// This ground truth note was aligned with nothing in the transcription. Skip it.
		if (distances[i][j - 1] == min) {
			for (List<Integer> list : getPossibleAlignmentsFromMatrix(i, j - 1, distances)) {
				alignments.add(list);
			}
		}
		
		// The current transcription and ground truth notes were aligned. Add the current ground
		// truth index to the alignment list.
		if (distances[i - 1][j - 1] == min) {
			for (List<Integer> list : getPossibleAlignmentsFromMatrix(i - 1, j - 1, distances)) {
				list.add(j - 2); // j - 2, because (j-1) is aligned, and the distance matrix starts from 1.
				alignments.add(list);
			}
		}
		
		return alignments;
	}
	
	/**
	 * Get the Dynamic Time Warping distance matrix from the note lists.
	 * <br>
	 * During calculation, we add an additional 0.01 penalty to any aligned note whose previous
	 * notes (in both ground truth and transcription) were not aligned. This is used to prefer
	 * alignments which align many consecutive notes.
	 * 
	 * @param gtNotes The ground truth note lists, split by onset time.
	 * @param mNotes The transcribed note lists, split by onset time.
	 * 
	 * @return The DTW distance matrix.
	 */
	private static double[][] getAlignmentMatrix(List<List<Note>> gtNotes, List<List<Note>> mNotes) {
		double[][] distances = new double[gtNotes.size() + 1][mNotes.size() + 1];
		
		for (int i = 1; i < distances.length; i++) {
			distances[i][0] = Double.POSITIVE_INFINITY;
		}
		
		for (int j = 1; j < distances[0].length; j++) {
			distances[0][j] = Double.POSITIVE_INFINITY;
		}
		
		for (int j = 1; j < distances[0].length; j++) {
			for (int i = 1; i < distances.length; i++) {
				double distance = getDistance(gtNotes.get(i - 1), mNotes.get(j - 1));
				
				double min = Math.min(Math.min(
						distances[i - 1][j],
						distances[i][j - 1]),
						distances[i - 1][j - 1]);
				distances[i][j] = min + distance;
				
				// Add a 0.01 penalty if this was not a 1:1 alignment from the previous note in both
				// the transcription and the ground truth.
				// Used to prefer alignments which align many consecutive notes.
				if (distances[i - 1][j - 1] != min) {
					distances[i][j] += 0.01;
				}
			}
		}
		
		return distances;
	}
	
	/**
	 * Get the distance between a given ground truth note set and a possible transcription note set.
	 * 
	 * @param gtNotes The ground truth notes.
	 * @param mNotes The possible transcription notes.
	 * @return The alignment score. 1 - its F-measure.
	 */
	private static double getDistance(List<Note> gtNotes, List<Note> mNotes) {
		int truePositives = 0;
		List<Note> gtNotesCopy = new ArrayList<Note>(gtNotes);
		
		for (Note mNote : mNotes) {
			Iterator<Note> gtIterator = gtNotesCopy.iterator();
			
			while (gtIterator.hasNext()) {
				Note gtNote = gtIterator.next();
				
				if (mNote.pitch == gtNote.pitch) {
					truePositives++;
					gtIterator.remove();
					break;
				}
			}
		}
		
		int falsePositives = mNotes.size() - truePositives;
		int falseNegatives = gtNotesCopy.size();
		
		return 1.0 - Main.getF1(truePositives, falsePositives, falseNegatives);
	}

	/**
	 * Convert a time to a new time given some alignment.
	 * 
	 * @param time The time we want to convert.
	 * @param gt The ground truth music, to help with alignment.
	 * @param m The transcribed music, where the time comes from.
	 * @param alignment The alignment.
	 * An alignment is a list containing, for each transcribed note list, the index of the ground
	 * truth note list to which it is aligned, or -1 if it was not aligned with any ground truth note.
	 * 
	 * @return A time converted from transcription scale to ground truth scale.
	 */
	public static int convertTime(int time, Music gt, Music m, List<Integer> alignment) {
		double mIndex = -1;
		List<List<Note>> mNotes = m.getNoteLists();
		
		// Find the correct transcription anchor index to start with
		for (int i = 0; i < mNotes.size(); i++) {
			
			// Time matches an anchor exactly
			if (mNotes.get(i).get(0).valueOnsetTime == time) {
				mIndex = i;
				break;
			}
			
			// This anchor is past the time
			if (mNotes.get(i).get(0).valueOnsetTime > time) {
				mIndex = i - 0.5;
				break;
			}
		}
		
		List<List<Note>> gtNotes = gt.getNoteLists();
		int gtPreviousAnchor = -1;
		int gtPreviousPreviousAnchor = -1;
		int gtNextAnchor = gtNotes.size();
		int gtNextNextAnchor = gtNotes.size();
		
		// Go through the alignments
		for (int i = 0; i < alignment.size(); i++) {
			if (alignment.get(i) != -1) {
				// There was an alignment here
				
				if (alignment.get(i) == mIndex) {
					// This is the correct time, exactly on the index
					return gtNotes.get(i).get(0).valueOnsetTime;
				}
				
				if (alignment.get(i) < mIndex) {
					// The time is past this anchor
					gtPreviousPreviousAnchor = gtPreviousAnchor;
					gtPreviousAnchor = i;
					
				} else {
					// We are past the time
					if (gtNextAnchor == gtNotes.size()) {
						// This is the first anchor for which we are past the time
						gtNextAnchor = i;
						
					} else {
						// This is the 2nd anchor for which we are past the time
						gtNextNextAnchor = i;
						break;
					}
				}
			}
		}
		
		if (gtPreviousAnchor == -1 && gtNextAnchor == gtNotes.size()) {
			// Nothing was aligned
			return time;
		}
		
		if (gtPreviousAnchor == -1 ) {
			// Time is before the first anchor. Use the rate from the first anchor.
			if (gtNextNextAnchor != gtNotes.size()) {
				return convertTime(time, gtNextAnchor, gtNextNextAnchor, gtNotes, mNotes, alignment);
			}
			
			// Only 1 anchor. Just linear shift.
			return time - mNotes.get(alignment.get(gtNextAnchor)).get(0).valueOnsetTime + gtNotes.get(gtNextAnchor).get(0).valueOnsetTime;
			
		} else if (gtNextAnchor == gtNotes.size()) {
			// Time is after the last anchor. Use the previous rate.
			if (gtPreviousPreviousAnchor != -1) {
				return convertTime(time, gtPreviousPreviousAnchor, gtPreviousAnchor, gtNotes, mNotes, alignment);
			}
			
			// Only 1 anchor. Just linear shift.
			return time - mNotes.get(alignment.get(gtPreviousAnchor)).get(0).valueOnsetTime + gtNotes.get(gtPreviousAnchor).get(0).valueOnsetTime;
			
		} else {
			// Time is between anchor points.
			return convertTime(time, gtPreviousAnchor, gtNextAnchor, gtNotes, mNotes, alignment);
		}
	}
	
	/**
	 * Convert the given time from transcription scale to ground truth scale using the rate between the
	 * given anchor points.
	 * 
	 * @param time The time to convert.
	 * @param gtPreviousAnchor The first anchor point.
	 * @param gtNextAnchor The second anchor point.
	 * @param gtNotes The ground truth note lists.
	 * @param mNotes The transcription note lists.
	 * @param alignment The alignment.
	 * An alignment is a list containing, for each transcribed note list, the index of the ground
	 * truth note list to which it is aligned, or -1 if it was not aligned with any ground truth note.
	 * 
	 * @return The converted time.
	 */
	private static int convertTime(int time, int gtPreviousAnchor, int gtNextAnchor, List<List<Note>> gtNotes, List<List<Note>> mNotes, List<Integer> alignment) {
		int gtPreviousTime = gtNotes.get(gtPreviousAnchor).get(0).valueOnsetTime;
		int gtNextTime = gtNotes.get(gtNextAnchor).get(0).valueOnsetTime;
		int mPreviousTime = mNotes.get(alignment.get(gtPreviousAnchor)).get(0).valueOnsetTime;
		int mNextTime = mNotes.get(alignment.get(gtNextAnchor)).get(0).valueOnsetTime;
		
		double rate = ((double) (gtNextTime - gtPreviousTime)) / (mNextTime - mPreviousTime);
		
		return (int) Math.round(rate * (time - mPreviousTime) + gtPreviousTime);
	}
}