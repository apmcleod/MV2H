package mv2h.objects.meter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import mv2h.Main;

/**
 * A <code>Meter</code> object defines the time signature and metrical structure of a musical score.
 * 
 * @author Andrew McLeod
 */
public class Meter {
	/**
	 * The time signature (metrical structure) of the score.
	 */
	private Hierarchy hierarchy;
	
	/**
	 * A unique, ordered set of the tatum times of the musical score.
	 */
	private final SortedSet<Tatum> tatums;
	
	/**
	 * Create a new empty meter object, defaulting to 4/4 time and 4 tatums per sub beat.
	 */
	public Meter() {
		tatums = new TreeSet<Tatum>();
		hierarchy = new Hierarchy(4, 2, 4, 0);
	}
	
	/**
	 * Add a tatum to {@link #tatums}.
	 * 
	 * @param tatum The new tatum to add.
	 */
	public void addTatum(Tatum tatum) {
		tatums.add(tatum);
	}
	
	/**
	 * Set the hierarchy of this score.
	 * 
	 * @param hierarchy {@link #hierarchy}
	 */
	public void setHierarchy(Hierarchy hierarchy) {
		this.hierarchy = hierarchy;
	}
	
	/**
	 * Get the hierarchy of this score.
	 * 
	 * @return {@link #hierarchy}
	 */
	public Hierarchy getHierarchy() {
		return hierarchy;
	}
	
	/**
	 * Get a List of the tatums present in this score.
	 * 
	 * @return A List of {@link #tatums}
	 */
	public List<Tatum> getTatums() {
		return new ArrayList<Tatum>(tatums);
	}
	
	/**
	 * Get all of the groupings of this score's metrical structure.
	 * 
	 * @return A grouping for each bar, beat, and sub beat in this score.
	 */
	private List<Grouping> getGroupings() {
		List<Grouping> groupings = new ArrayList<Grouping>();
		List<Tatum> tatumList = getTatums();
		
		// Sub beats
		int length = hierarchy.tatumsPerSubBeat;
		int start = hierarchy.anacrusisLengthTatums % length;
		for (int tatumIndex = start; tatumIndex + length < tatumList.size(); tatumIndex += length) {
			groupings.add(new Grouping(tatumList.get(tatumIndex).time, tatumList.get(tatumIndex + length).time));
		}
		
		// Beats
		length = hierarchy.tatumsPerSubBeat * hierarchy.subBeatsPerBeat;
		start = hierarchy.anacrusisLengthTatums % length;
		for (int tatumIndex = start; tatumIndex + length < tatumList.size(); tatumIndex += length) {
			groupings.add(new Grouping(tatumList.get(tatumIndex).time, tatumList.get(tatumIndex + length).time));
		}
		
		// Bars
		length = hierarchy.tatumsPerSubBeat * hierarchy.subBeatsPerBeat * hierarchy.beatsPerBar;
		start = hierarchy.anacrusisLengthTatums % length;
		for (int tatumIndex = start; tatumIndex + length < tatumList.size(); tatumIndex += length) {
			groupings.add(new Grouping(tatumList.get(tatumIndex).time, tatumList.get(tatumIndex + length).time));
		}
		
		return groupings;
	}
	
	/**
	 * Get the metrical F1 of this score, given the ground truth.
	 * 
	 * @param groundTruth The ground truth meter.
	 * 
	 * @return The metrical F1.
	 */
	public double getF1(Meter groundTruth) {
		List<Grouping> transcriptionGroupings = getGroupings();
		List<Grouping> groundTruthGroupings = groundTruth.getGroupings();
		
		int truePositives = 0;
		for (Grouping transcriptionGrouping : transcriptionGroupings) {
			
			Iterator<Grouping> groundTruthGroupingIterator = groundTruthGroupings.iterator();
			while (groundTruthGroupingIterator.hasNext()) {
				Grouping groundTruthGrouping = groundTruthGroupingIterator.next();
				
				if (transcriptionGrouping.matches(groundTruthGrouping)) {
					// Match found
					groundTruthGroupingIterator.remove();
					truePositives++;
					break;
				}
			}
		}
		int falsePositives = transcriptionGroupings.size() - truePositives;
		int falseNegatives = groundTruthGroupings.size();
		
		return Main.getF1(truePositives, falsePositives, falseNegatives);
	}
	
	@Override
	public String toString() {
		return "Meter " + hierarchy + " Tatums " + tatums;
	}
}
