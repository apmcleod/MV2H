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
	 * A unique, ordered set of hierarchies of the musical score.
	 */
	private final SortedSet<Hierarchy> hierarchies;
	
	/**
	 * A unique, ordered set of the tatum times of the musical score.
	 */
	private final SortedSet<Tatum> tatums;
	
	/**
	 * Create a new empty meter object, defaulting to 4/4 time and 4 tatums per sub beat.
	 */
	public Meter() {
		tatums = new TreeSet<Tatum>();
		hierarchies = new TreeSet<Hierarchy>();
		hierarchies.add(new Hierarchy(4, 2, 4, 0));
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
	 * Add a hierarchy to this score.
	 * 
	 * @param hierarchy The hierarchy to add.
	 */
	public void addHierarchy(Hierarchy hierarchy) {
		if (hierarchies.contains(hierarchy)) {
			// Remove any hierarchy at the same time
			hierarchies.remove(hierarchy);
		}
		
		hierarchies.add(hierarchy);
	}
	
	/**
	 * Get the hierarchies of this score.
	 * 
	 * @return {@link #hierarchy}
	 */
	public List<Hierarchy> getHierarchies() {
		return new ArrayList<Hierarchy>(hierarchies);
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
		
		Iterator<Tatum> tatumIterator = getTatums().iterator();
		Iterator<Hierarchy> hierarchyIterator = getHierarchies().iterator();
		
		// Quick exit for no tatums or no hierarchies
		if (!tatumIterator.hasNext() || !hierarchyIterator.hasNext()) {
			return groupings;
		}
		
		// Set up tracking vars
		Tatum thisTatum = tatumIterator.next();
		Tatum nextTatum = tatumIterator.hasNext() ? tatumIterator.next() : null;
		
		Hierarchy thisHierarchy = hierarchyIterator.next();
		Hierarchy nextHierarchy = hierarchyIterator.hasNext() ? hierarchyIterator.next() : null;
		
		int tatumsPerSubBeat = thisHierarchy.tatumsPerSubBeat;
		int tatumsPerBeat = tatumsPerSubBeat * thisHierarchy.subBeatsPerBeat;
		int tatumsPerBar = tatumsPerBeat * thisHierarchy.beatsPerBar;
		
		int tatumNum = thisHierarchy.anacrusisLengthTatums == 0 ? 0 : tatumsPerBar - thisHierarchy.anacrusisLengthTatums;
		
		int subBeatStart = tatumNum % tatumsPerSubBeat == 0 ? thisTatum.time : -1;
		int beatStart = tatumNum % tatumsPerBeat == 0 ? thisTatum.time : -1;
		int barStart = tatumNum % tatumsPerBar == 0 ? thisTatum.time : -1;
		
		while (nextTatum != null) {
			// Go to next tatum
			thisTatum = nextTatum;
			nextTatum = tatumIterator.hasNext() ? tatumIterator.next() : null;
			tatumNum++;
			
			if (nextHierarchy != null && nextHierarchy.time <= thisTatum.time) {
				// Go to next hierarchy
				thisHierarchy = nextHierarchy;
				nextHierarchy = hierarchyIterator.hasNext() ? hierarchyIterator.next() : null;
				
				tatumsPerSubBeat = thisHierarchy.tatumsPerSubBeat;
				tatumsPerBeat = tatumsPerSubBeat * thisHierarchy.subBeatsPerBeat;
				tatumsPerBar = tatumsPerBeat * thisHierarchy.beatsPerBar;
				
				tatumNum = thisHierarchy.anacrusisLengthTatums == 0 ? 0 : tatumsPerBar - thisHierarchy.anacrusisLengthTatums;
			}
			
			// Check for grouping starts/ends
			
			// Sub beat
			if (tatumNum % tatumsPerSubBeat == 0) {
				if (subBeatStart != -1) {
					// Already started
					groupings.add(new Grouping(subBeatStart, thisTatum.time));
				}
				
				subBeatStart = thisTatum.time;
			}
			
			// Beat
			if (tatumNum % tatumsPerBeat == 0) {
				if (beatStart != -1) {
					// Already started
					groupings.add(new Grouping(beatStart, thisTatum.time));
				}
				
				beatStart = thisTatum.time;
			}
			
			// Bar
			if (tatumNum % tatumsPerBar == 0) {
				if (barStart != -1) {
					// Already started
					groupings.add(new Grouping(barStart, thisTatum.time));
				}
				
				barStart = thisTatum.time;
			}
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
		return "Meters " + hierarchies + " Tatums " + tatums;
	}
}
