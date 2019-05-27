package mv2h.objects.meter;

import mv2h.Main;

public class Grouping {
	private final int startTime;
	private final int endTime;
	
	public Grouping(int start, int end) {
		startTime = start;
		endTime = end;
	}
	
	public boolean matches(Grouping grouping) {
		return Math.abs(grouping.startTime - startTime) <= Main.GROUPING_EPSILON &&
				Math.abs(grouping.endTime - endTime) <= Main.GROUPING_EPSILON;
	}
	
	public String toString() {
		return "Grouping " + startTime + "-" + endTime;
	}
}
