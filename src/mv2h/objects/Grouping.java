package mv2h.objects;

public class Grouping {
	private static final int GROUPING_EPSILON = 70;
	
	private final int startTime;
	private final int endTime;
	
	public Grouping(int start, int end) {
		startTime = start;
		endTime = end;
	}
	
	public boolean matches(Grouping grouping) {
		return Math.abs(grouping.startTime - startTime) <= GROUPING_EPSILON &&
				Math.abs(grouping.endTime - endTime) <= GROUPING_EPSILON;
	}
	
	public String toString() {
		return "Grouping " + startTime + "-" + endTime;
	}
}
