package mv2h.tools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import mv2h.Main;
import mv2h.objects.Music;
import mv2h.objects.Note;

public class Aligner {
	public static Set<List<Integer>> getPossibleAlignments(Music gt, Music m) {
		double[][] distances = getAlignmentMatrix(gt.getNoteLists(), m.getNoteLists());
		return getPossibleAlignmentsFromMatrix(distances.length - 1, distances[0].length - 1, distances);
	}
	
	private static Set<List<Integer>> getPossibleAlignmentsFromMatrix(int i, int j, double[][] distances) {
		Set<List<Integer>> alignments = new HashSet<List<Integer>>();
		
		if (i == 1 && j == 1) {
			alignments.add(new ArrayList<Integer>());
			return alignments;
		}
		
		double min = Math.min(Math.min(
				distances[i - 1][j],
				distances[i][j - 1]),
				distances[i - 1][j - 1]);
		
		if (distances[i - 1][j] == min) {
			for (List<Integer> list : getPossibleAlignmentsFromMatrix(i - 1, j, distances)) {
				list.add(-1);
				alignments.add(list);
			}
		}
		
		if (distances[i][j - 1] == min) {
			for (List<Integer> list : getPossibleAlignmentsFromMatrix(i, j - 1, distances)) {
				//list.add(-1);
				alignments.add(list);
			}
		}
		
		if (distances[i - 1][j - 1] == min) {
			for (List<Integer> list : getPossibleAlignmentsFromMatrix(i - 1, j - 1, distances)) {
				list.add(j - 2);
				alignments.add(list);
			}
		}
		
		return alignments;
	}
	
	/**
	 * Get the Dynamic Time Warping distance matrix from the note lists.
	 * 
	 * @param gtNotes The ground truth note lists.
	 * @param mNotes The transcribed note lists.
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
				
				if (distances[i - 1][j - 1] != min) {
					distances[i][j] += 0.01;
				}
			}
		}
		
		return distances;
	}
	
	/**
	 * Get the distance between a given ground truth note set and a possible transcription.
	 * 
	 * @param gtNotes The ground truth notes.
	 * @param mNotes The possible transcription notes.
	 * @return The alignment score. Currently 1 - its F-measure.
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
	 * 
	 * @return A mapping of the time from transcription scale to ground truth scale.
	 */
	public static int convertTime(int time, Music gt, Music m, List<Integer> alignment) {
		double mIndex = -1;
		List<List<Note>> mNotes = m.getNoteLists();
		for (int i = 0; i < mNotes.size(); i++) {
			if (mNotes.get(i).get(0).valueOnsetTime == time) {
				mIndex = i;
				break;
			}
			
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
		
		for (int i = 0; i < alignment.size(); i++) {
			if (alignment.get(i) != -1) {
				if (alignment.get(i) == mIndex) {
					return gtNotes.get(i).get(0).valueOnsetTime;
				}
				
				if (alignment.get(i) < mIndex) {
					gtPreviousPreviousAnchor = gtPreviousAnchor;
					gtPreviousAnchor = i;
				} else {
					if (gtNextAnchor == gtNotes.size()) {
						gtNextAnchor = i;
					} else {
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
			// Time is before the first anchor. Use the following rate.
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
	
	private static int convertTime(int time, int gtPreviousAnchor, int gtNextAnchor, List<List<Note>> gtNotes, List<List<Note>> mNotes, List<Integer> alignment) {
		int gtPreviousTime = gtNotes.get(gtPreviousAnchor).get(0).valueOnsetTime;
		int gtNextTime = gtNotes.get(gtNextAnchor).get(0).valueOnsetTime;
		int mPreviousTime = mNotes.get(alignment.get(gtPreviousAnchor)).get(0).valueOnsetTime;
		int mNextTime = mNotes.get(alignment.get(gtNextAnchor)).get(0).valueOnsetTime;
		
		return convertTime(time, gtPreviousTime, gtNextTime, mPreviousTime, mNextTime);
	}
	
	private static int convertTime(int time, int gtPreviousTime, int gtNextTime, int mPreviousTime, int mNextTime) {
		double rate = ((double) (gtNextTime - gtPreviousTime)) / (mNextTime - mPreviousTime);
		
		return (int) Math.round(rate * (time - mPreviousTime) + gtPreviousTime);
	}
}