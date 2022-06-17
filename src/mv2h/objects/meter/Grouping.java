package mv2h.objects.meter;

import mv2h.Main;

/**
 * A <code>Grouping</code> object defines the start and end times of some level in the
 * metrical structure of a musical score (bar, beat, or sub beat). See {@link Meter}.
 *
 * @author Andrew McLeod
 */
public class Grouping {
	/**
	 * The start time of this grouping.
	 */
	public final int startTime;

	/**
	 * The end time of this grouping.
	 */
	public final int endTime;

	/**
	 * Create a new grouping with the given start and end times.
	 *
	 * @param start {@link #startTime}
	 * @param end {@link #endTime}
	 */
	public Grouping(int start, int end) {
		startTime = start;
		endTime = end;
	}

	/**
	 * Decide whether this grouping matches the given grouping, using the error threshold
	 * {@link Main#GROUPING_EPSILON}. If both its start and end times are within the epsilon
	 * of this one's start and end times, it is a match.
	 *
	 * @param grouping The grouping to check for a match.
	 *
	 * @return True if the groupings match. False otherwise.
	 */
	public boolean matches(Grouping grouping) {
		return Math.abs(grouping.startTime - startTime) <= Main.GROUPING_EPSILON &&
				Math.abs(grouping.endTime - endTime) <= Main.GROUPING_EPSILON;
	}

	@Override
	public String toString() {
		return "Grouping " + startTime + "-" + endTime;
	}
}
