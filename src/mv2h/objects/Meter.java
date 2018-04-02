package mv2h.objects;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import mv2h.Main;

public class Meter {
	private Hierarchy hierarchy;
	private final SortedSet<Tatum> tatums;
	
	public Meter() {
		tatums = new TreeSet<Tatum>();
	}
	
	public void addTatum(Tatum tatum) {
		tatums.add(tatum);
	}
	
	public void setHierarchy(Hierarchy hierarchy) {
		this.hierarchy = hierarchy;
	}
	
	public Hierarchy getHierarchy() {
		return hierarchy;
	}
	
	public List<Tatum> getTatums() {
		return new ArrayList<Tatum>(tatums);
	}
	
	public List<Grouping> getGroupings() {
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
	
	public double getF1(Meter groundTruth) {
		List<Grouping> transcriptionGroupings = getGroupings();
		List<Grouping> groundTruthGroupings = groundTruth.getGroupings();
		
		int truePositives = 0;
		for (Grouping transcriptionGrouping : transcriptionGroupings) {
			
			Iterator<Grouping> groundTruthGroupingIterator = groundTruthGroupings.iterator();
			while (groundTruthGroupingIterator.hasNext()) {
				Grouping groundTruthGrouping = groundTruthGroupingIterator.next();
				
				if (transcriptionGrouping.matches(groundTruthGrouping)) {
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
	
	public String toString() {
		return "Meter " + hierarchy + " Tatums " + tatums;
	}
}
