package mv2h.tools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

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
	 * An alignment is a list containing, for each ground truth note list, the index of the transcription
	 * note list to which it is aligned, or -1 if it was not aligned with any transcription note.
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
	 * An alignment is a list containing, for each ground truth note list, the index of the transcription
	 * note list to which it is aligned, or -1 if it was not aligned with any transcription note.
	 */
	private static Set<List<Integer>> getPossibleAlignmentsFromMatrix(int i, int j, double[][] distances) {
		Set<List<Integer>> alignments = new HashSet<List<Integer>>();

		// Base case. we are at the beginning and nothing else needs to be aligned.
		if (i == 0 && j == 0) {
			alignments.add(new ArrayList<Integer>());
			return alignments;
		}

		double min = Math.min(Math.min(
				i > 0 ? distances[i - 1][j] : Double.POSITIVE_INFINITY,
				j > 0 ? distances[i][j - 1] : Double.POSITIVE_INFINITY),
				i > 0 && j > 0 ? distances[i - 1][j - 1] : Double.POSITIVE_INFINITY);

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
				list.add(j - 1); // j - 2, because (j-1) is aligned, and the distance matrix starts from 1.
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
		List<Map<Integer, Integer>> gtNoteMaps = getNotePitchMaps(gtNotes);
		List<Map<Integer, Integer>> mNoteMaps = getNotePitchMaps(mNotes);

		double[][] distances = new double[gtNotes.size() + 1][mNotes.size() + 1];

		for (int i = 1; i < distances.length; i++) {
			distances[i][0] = Double.POSITIVE_INFINITY;
		}

		for (int j = 1; j < distances[0].length; j++) {
			distances[0][j] = Double.POSITIVE_INFINITY;
		}

		boolean useProgressBar = distances[0].length >= 100;

		for (int j = 1; j < distances[0].length; j++) {
			for (int i = 1; i < distances.length; i++) {
				double distance = getDistance(gtNoteMaps.get(i - 1), mNoteMaps.get(j - 1));

				double min = Math.min(Math.min(
						distances[i - 1][j] + 0.6,
						distances[i][j - 1] + 0.6),
						distances[i - 1][j - 1] + distance);
				distances[i][j] = min;
			}
		}

		return distances;
	}

	/**
	 * Convert note lists into note pitch maps, which map each pitch of a note to the
	 * number of notes in that note list at that pitch.
	 *
	 * @param noteLists A list of the note lists of a piece of music.
	 * @return A list of pitch maps for that piece of music.
	 */
	private static List<Map<Integer, Integer>> getNotePitchMaps(List<List<Note>> noteLists) {
		List<Map<Integer, Integer>> notePitchMaps = new ArrayList<Map<Integer, Integer>>(noteLists.size());

		for (List<Note> notesList : noteLists) {
			Map<Integer, Integer> pitchMap = new HashMap<Integer, Integer>(notesList.size());
			notePitchMaps.add(pitchMap);

			for (Note note : notesList) {
				if (pitchMap.containsKey(note.pitch)) {
					pitchMap.put(note.pitch, pitchMap.get(note.pitch) + 1);
				} else {
					pitchMap.put(note.pitch, 1);
				}
			}
		}

		return notePitchMaps;
	}

	/**
	 * Get the distance between a given ground truth note set and a possible transcription note set.
	 *
	 * @param gtNoteMap The pitch map of a ground truth note set.
	 * @param mNoteMap The pitch map of a possible transcription note set.
	 * @return The alignment score. 1 - its F-measure.
	 */
	private static double getDistance(Map<Integer, Integer> gtNoteMap, Map<Integer, Integer> mNoteMap) {
		int truePositives = 0;
		int falsePositives = 0;

		for (Entry<Integer, Integer> entry : mNoteMap.entrySet()) {
			Integer pitch = entry.getKey();
			int count = entry.getValue();

			if (gtNoteMap.containsKey(pitch)) {
				int gtCount = gtNoteMap.get(pitch);

				truePositives += Math.min(count, gtCount);
				if (count > gtCount) {
					falsePositives += count - gtCount;
				}

			} else {
				falsePositives += count;
			}
		}

		if (truePositives == 0) {
			return 1.0;
		}

		int gtNoteCount = 0;
		for (int count : gtNoteMap.values()) {
			gtNoteCount += count;
		}

		int falseNegatives = gtNoteCount - truePositives;

		return 1.0 - Main.getF1(truePositives, falsePositives, falseNegatives);
	}

	/**
	 * Convert a time to a new time given some alignment.
	 *
	 * @param time The time we want to convert.
	 * @param gt The ground truth music, to help with alignment.
	 * @param transcription The transcribed music, where the time comes from.
	 * @param alignment The alignment.
	 * An alignment is a list containing, for each ground truth note list, the index of the transcription
	 * note list to which it is aligned, or -1 if it was not aligned with any transcription note.
	 *
	 * @return A time converted from transcription scale to ground truth scale.
	 */
	public static int convertTime(int time, Music gt, Music transcription, List<Integer> alignment) {
		double transcriptionIndex = -1;
		List<List<Note>> transcriptionNotes = transcription.getNoteLists();

		// Find the correct transcription anchor index to start with
		for (int i = 0; i < transcriptionNotes.size(); i++) {

			// Time matches an anchor exactly
			if (transcriptionNotes.get(i).get(0).valueOnsetTime == time) {
				transcriptionIndex = i;
				break;
			}

			// This anchor is past the time
			if (transcriptionNotes.get(i).get(0).valueOnsetTime > time) {
				transcriptionIndex = i - 0.5;
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

				if (alignment.get(i) == transcriptionIndex) {
					// This is the correct time, exactly on the index
					return gtNotes.get(i).get(0).valueOnsetTime;
				}

				if (alignment.get(i) < transcriptionIndex) {
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
				return convertTime(time, gtNextAnchor, gtNextNextAnchor, gtNotes, transcriptionNotes, alignment);
			}

			// Only 1 anchor. Just linear shift.
			return time - transcriptionNotes.get(alignment.get(gtNextAnchor)).get(0).valueOnsetTime + gtNotes.get(gtNextAnchor).get(0).valueOnsetTime;

		} else if (gtNextAnchor == gtNotes.size()) {
			// Time is after the last anchor. Use the previous rate.
			if (gtPreviousPreviousAnchor != -1) {
				return convertTime(time, gtPreviousPreviousAnchor, gtPreviousAnchor, gtNotes, transcriptionNotes, alignment);
			}

			// Only 1 anchor. Just linear shift.
			return time - transcriptionNotes.get(alignment.get(gtPreviousAnchor)).get(0).valueOnsetTime + gtNotes.get(gtPreviousAnchor).get(0).valueOnsetTime;

		} else {
			// Time is between anchor points.
			return convertTime(time, gtPreviousAnchor, gtNextAnchor, gtNotes, transcriptionNotes, alignment);
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
	 * An alignment is a list containing, for each ground truth note list, the index of the transcription
	 * note list to which it is aligned, or -1 if it was not aligned with any transcription note.
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