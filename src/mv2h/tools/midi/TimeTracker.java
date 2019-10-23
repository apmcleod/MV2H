package mv2h.tools.midi;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;

import mv2h.objects.harmony.Key;
import mv2h.objects.meter.Hierarchy;
import mv2h.objects.meter.Meter;
import mv2h.objects.meter.Tatum;

/**
 * A <code>TimeTracker</code> is able to interpret MIDI tempo, key, and time signature change events and keep track
 * of the song timing in seconds, instead of just using ticks as MIDI events do. It does this by using
 * a LinkedList of {@link TimeTrackerNode} objects.
 * 
 * @author Andrew McLeod - 23 October, 2014
 */
public class TimeTracker {
	/**
	 * Pulses (ticks) per Quarter note, as in the current Midi song's header.
	 */
	private double PPQ = 120.0;
	
	/**
	 * The LInkedList of TimeTrackerNodes of this TimeTracker, ordered by time.
	 */
	private final LinkedList<TimeTrackerNode> nodes;
	
	/**
	 * The number of sub beats which lie before the first full measure in this song.
	 */
	private int anacrusisLengthSubBeats;
	
	/**
	 * The last tick for any event in this song, initially 0.
	 */
	private long lastTick = 0;
    
    /**
	 * Create a new TimeTracker.
	 */
    public TimeTracker() {
    	this(-1);
    }
    
    /**
	 * Create a new TimeTracker with the given sub beat length.
	 * 
	 * @param subBeatLength {@link #subBeatLength}
	 */
    public TimeTracker(int subBeatLength) {
    	anacrusisLengthSubBeats = 0;
    	nodes = new LinkedList<TimeTrackerNode>();
    	nodes.add(new TimeTrackerNode(PPQ));
    }
    
    /**
     * A TimeSignature event was detected. Deal with it.
     * 
     * @param event The event.
     * @param mm The message from the event.
     */
    public void addTimeSignatureChange(MidiEvent event, MetaMessage mm) {
    	TimeSignature ts = new TimeSignature(mm.getData());
    	
    	if (nodes.getLast().getStartTick() > event.getTick()) {
    		return;
    	}
    	
    	if (nodes.getLast().getStartTick() == event.getTick()) {
    		// If we're at the same time as a prior time change, combine this with that node.
    		nodes.getLast().setTimeSignature(ts);
    		
    	} else if (!ts.equals(nodes.getLast().getTimeSignature())) {
    		// Some change has been made
    		nodes.add(new TimeTrackerNode(nodes.getLast(), event.getTick(), PPQ));
    		nodes.getLast().setTimeSignature(ts);
    	}
    	
    	nodes.getLast().setIsTimeSignatureDummy(false);
    }
    
    /**
     * A Tempo event was detected. Deal with it.
     * 
     * @param event The event.
     * @param mm The message from the event.
     */
    public void addTempoChange(MidiEvent event, MetaMessage mm) {
    	Tempo t = new Tempo(mm.getData());
    	
    	if (nodes.getLast().getStartTick() > event.getTick()) {
    		return;
    	}
    	
    	if (nodes.getLast().getStartTick() == event.getTick()) {
    		// If we're at the same time as a prior time change, combine this with that node.
    		nodes.getLast().setTempo(t);
    		
    	} else if (!t.equals(nodes.getLast().getTempo())) {
    		nodes.add(new TimeTrackerNode(nodes.getLast(), event.getTick(), PPQ));
    		nodes.getLast().setTempo(t);
    	}
    }
    
    /**
     * A Key event was detected. Deal with it.
     * 
     * @param event The event.
     * @param mm The message from the event.
     */
    public void addKeyChange(MidiEvent event, MetaMessage mm) {
    	int numSharps = mm.getData()[0];
		boolean major = mm.getData()[1] == 0;
		
		int keyNumber = (7 * numSharps + 100 * 12) % 12;
		Key ks = new Key(keyNumber, major);
    	
    	if (nodes.getLast().getStartTick() > event.getTick()) {
    		return;
    	}
    	
    	if (nodes.getLast().getStartTick() == event.getTick()) {
    		// If we're at the same time as a prior time change, combine this with that node.
    		nodes.getLast().setKey(ks);
    		
    	} else if (!ks.equals(nodes.getLast().getKey())) {
    		nodes.add(new TimeTrackerNode(nodes.getLast(), event.getTick(), PPQ));
    		nodes.getLast().setKey(ks);
    	}
	}
    
    /**
     * Returns the time in microseconds of a given tick number.
     * 
     * @param tick The tick number to calculate the time of
     * @return The time of the given tick number, measured in microseconds since the most recent epoch.
     */
    public double getTimeAtTick(long tick) {
    	return getNodeAtTick(tick).getTimeAtTick(tick, PPQ);
    }
    
    /**
     * Get the TimeTrackerNode which is valid at the given tick.
     * 
     * @param tick The tick.
     * @return The valid TimeTrackerNode.
     */
    private TimeTrackerNode getNodeAtTick(long tick) {
    	ListIterator<TimeTrackerNode> iterator = nodes.listIterator();
    	
    	TimeTrackerNode node = iterator.next();
    	while (iterator.hasNext()) {
    		node = iterator.next();
    		
    		if (node.getStartTick() > tick) {
    			iterator.previous();
    			return iterator.previous();
    		}
    	}

    	return node;
    }
    
    /**
     * Get a List of all of the key signatures of this TimeTracker.
     * 
     * @return A List of all of the key signatures of this TimeTracker.
     */
    public List<Key> getAllKeySignatures() {
		List<Key> keys = new ArrayList<Key>();
		
		for (TimeTrackerNode node : nodes) {
			Key key = node.getKey();
			
			// First key
			if (keys.isEmpty()) {
				keys.add(key);
				
			// Duplicate time
			} else if (keys.get(keys.size() - 1).time == key.time) {
				keys.set(keys.size() - 1, key);
				
			// Check if keys are equal
			} else {
				Key oldKey = keys.get(keys.size() - 1);
				if (key.isMajor != oldKey.isMajor || key.tonic != oldKey.tonic) {
					keys.add(key);
				}
			}
		}
		
		return keys;
	}
    
    /**
     * Get the Meter of this piece, according to this time tracker.
     * 
     * @return The meter of this piece.
     */
    public Meter getMeter() {
    	Meter meter = new Meter();
    	
    	// Add Hierarchies
    	Hierarchy current = null;
    	for (TimeTrackerNode node : nodes) {
    		current = node.getTimeSignature().getHierarchy((int) node.getStartTime(), node.getStartTime() == 0 ? anacrusisLengthSubBeats : 0);
    		
    		// First hierarchy
    		if (meter.getHierarchies().isEmpty()) {
    			meter.addHierarchy(current);
    			
    		// Same time -- overwrite
    		} else if (meter.getHierarchies().get(meter.getHierarchies().size() - 1).time == current.time) {
    			meter.addHierarchy(current);
    			
    		// New time -- add if changed
    		} else {
    			Hierarchy old = meter.getHierarchies().get(meter.getHierarchies().size() - 1);
    			if (old.beatsPerBar != current.beatsPerBar || old.subBeatsPerBeat != current.subBeatsPerBeat) {
    				meter.addHierarchy(current);
    			}
    		}
		}
    	
    	// Add Tatums (sub-beats)
    	double propFinished = 1.0;
    	for (int i = 1; i < nodes.size(); i++) {
    		TimeTrackerNode prevNode = nodes.get(i - 1);
    		TimeTrackerNode nextNode = nodes.get(i);
    		
    		for (Tatum tatum : prevNode.getSubBeatsUntil(propFinished, nextNode.getStartTime())) {
    			meter.addTatum(tatum);
    		}
    		propFinished = prevNode.getPropFinished(propFinished, nextNode.getStartTime());
    	}
    	
    	// Add last tatums
    	TimeTrackerNode lastNode = nodes.get(nodes.size() - 1);
    	for (Tatum tatum : lastNode.getSubBeatsUntil(propFinished, lastNode.getTimeAtTick(lastTick, PPQ))) {
    		meter.addTatum(tatum);
    	}
    	
    	return meter;
    }
    
    /**
     * Set the anacrusis length of this song to the given number of sub beats.
     * 
     * @param ticks The anacrusis length of this song, measured in sub beats.
     */
    public void setAnacrusis(int length) {
		anacrusisLengthSubBeats = length;
	}
    
    /**
     * Set the last tick for this song to the given value.
     * 
     * @param lastTick {@link #lastTick}
     */
    public void setLastTick(long lastTick) {
		this.lastTick = lastTick;
	}
    
    /**
     * Set the PPQ for this TimeTracker.
     * 
     * @param ppq {@link #PPQ}
     */
    public void setPPQ(double ppq) {
    	PPQ = ppq;
    }
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("[");
		
		ListIterator<TimeTrackerNode> iterator = nodes.listIterator();
		
		while (iterator.hasNext()) {
			sb.append(iterator.next().toString()).append(',');
		}
		
		sb.deleteCharAt(sb.length() - 1);
		sb.append(']');
		return sb.toString();
	}
}
