package mv2h.tools.midi;

import java.util.ArrayList;
import java.util.List;

import mv2h.objects.harmony.Key;
import mv2h.objects.meter.Tatum;

/**
 * A <code>TimeTrackerNode</code> represents the state of a musical score at a given time. That is,
 * it represents a <TimeSig, Tempo, KeySig>
 * triple, and contains information about the times at which that triple is contiguously valid.
 * 
 * @author Andrew McLeod - 11 Feb, 2015
 */
public class TimeTrackerNode {
	/**
	 * The start tick for this TimeTrackerNode. That is, the tick at which this ones triple becomes
	 * valid.
	 */
	private long startTick = 0L;
	
	/**
	 * The start time for this TimeTrackerNode, measured in milliseconds. That is, the time at which
	 * this one's triple becomes valid.
	 */
	private double startTime = 0.0;
	
	/**
	 * The TimeSignature associated with this TimeTrackerNode.
	 */
	private TimeSignature timeSignature = null;
	
	/**
	 * The Tempo associated with this TimeTrackerNode.
	 */
	private Tempo tempo = null;
	
	/**
	 * The Key associated with this TimeTrackerNode.
	 */
	private Key keySignature = null;
	
	/**
	 * Whether the Time Signature in this Node is a dummy first node or not.
	 */
	private boolean isTimeSignatureDummy;
	
	/**
	 * Create a new dummy first TimeTrackerNode at tick and time 0.
	 */
	public TimeTrackerNode() {
		startTick = 0L;
		isTimeSignatureDummy = true;
		
		timeSignature = new TimeSignature();
		tempo = new Tempo();
		keySignature = new Key(0, true);
	}
	
	/**
	 * Create a new TimeTrackerNode with the given previous TimeTrackerNode at the given tick.
	 * 
	 * @param prev The previous TimeTrackerNode, cannot be null.
	 * @param ppq The pulses per quarter note of the song.
	 * @param tick The tick at which this new one becomes valid.
	 */
	public TimeTrackerNode(TimeTrackerNode prev, long tick, double ppq) {
		startTick = tick;
		isTimeSignatureDummy = prev.isTimeSignatureDummy;
		
		startTime = prev.getTimeAtTick(tick, ppq);
		timeSignature = prev.getTimeSignature();
		tempo = prev.getTempo();
		keySignature = prev.getKey();
	}
	
	/**
	 * Get the time at the given tick.
	 * 
	 * @param tick The tick at which we want the time.
	 * @param ppq The pulses per quarter note of the song.
	 * @return The time at the given tick, measured in milliseconds.
	 */
	public double getTimeAtTick(long tick, double ppq) {
		long tickOffset = tick - getStartTick();
		return (tickOffset * getTimePerTick(ppq)) + startTime;
	}
	
	/**
	 * Gets the amount of time, in milliseconds, that passes between each tick.
	 * 
	 * @param ppq The pulses per quarter note of the song.
	 * @return The length of a tick in milliseconds.
	 */
	public double getTimePerTick(double ppq) {
		return tempo.getMillisPerQuarter() / ppq;
	}
	
	/**
	 * Get the start tick of this node.
	 * 
	 * @return {@link #startTick}
	 */
	public long getStartTick() {
		return startTick;
	}
	
	/**
	 * Get the start time of this node.
	 * 
	 * @return {@link #startTime}
	 */
	public double getStartTime() {
		return startTime;
	}
	
	/**
	 * Set the Tempo of this node.
	 * 
	 * @param tempo
	 */
	public void setTempo(Tempo tempo) {
		this.tempo = tempo;
	}
	
	/**
	 * Set the TimeSignature of this node.
	 * 
	 * @param timeSignature {@link #timeSignature}
	 */
	public void setTimeSignature(TimeSignature timeSignature) {
		this.timeSignature = timeSignature;
		isTimeSignatureDummy = false;
	}
	
	/**
	 * Get if this node has a dummy time signature.
	 * 
	 * @return {@link #isTimeSignatureDummy}
	 */
	public boolean isTimeSignatureDummy() {
		return isTimeSignatureDummy;
	}
	
	/**
	 * Set if this node's time signature is a dummy node.
	 * 
	 * @param dummy {@link #isTimeSignatureDummy}
	 */
	public void setIsTimeSignatureDummy(boolean dummy) {
		isTimeSignatureDummy = dummy;	
	}
	
	/**
	 * Set the Key of this node.
	 * 
	 * @param keySignature
	 */
	public void setKey(Key keySignature) {
		this.keySignature = keySignature;
	}
	
	/**
	 * Get the TimeSignature of this node.
	 * 
	 * @return {@link #timeSignature}
	 */
	public TimeSignature getTimeSignature() {
		return timeSignature;
	}
	
	/**
	 * Get the Tempo of this node.
	 * 
	 * @return {@link #tempo}
	 */
	public Tempo getTempo() {
		return tempo;
	}
	
	/**
	 * Get the Key of this node.
	 * 
	 * @return {@link #keySignature}
	 */
	public Key getKey() {
		return keySignature;
	}
	
	/**
	 * Get the number of milliseconds per sub beat.
	 * 
	 * @return Milliseconds per sub beat.
	 */
	private double getMillisPerSubBeat() {
		return tempo.getMillisPerQuarter() / timeSignature.getSubBeatsPerQuarter();
	}
	
	/**
	 * Get Tatums representing the sub beats until the given time.
	 * 
	 * @param propFinished The proportion of a sub beat which has been completed before
	 * this node.
	 * @param time The time until which we should output tatums.
	 * @return A List of Tatums at the given times.
	 */
	public List<Tatum> getSubBeatsUntil(double propFinished, double time) {
		double millisPerSubBeat = getMillisPerSubBeat();
		
		// Correct start time for propFinished
		double currentTime = startTime - millisPerSubBeat * propFinished + millisPerSubBeat;
		
		List<Tatum> tatums = new ArrayList<Tatum>();
		
		while (currentTime <= time) {
			tatums.add(new Tatum((int) Math.round(currentTime)));
			currentTime += millisPerSubBeat;
		}
		
		return tatums;
	}
	
	/**
	 * Get the proportion of the last sub beat of this node which will be complete at the
	 * given time.
	 * 
	 * @param propFinished The proportion of a sub beat which has been completed before
	 * this node.
	 * @param time The time until which we should output tatums.
	 * @return The proportion of the last sub beat which will be completed at the given time.
	 */
	public double getPropFinished(double propFinished, double time) {
		double millisPerSubBeat = getMillisPerSubBeat();
		
		// Correct start time for propFinished
		double prevTime = startTime - millisPerSubBeat * propFinished;
		
		// Length of remainder time in sub beats
		propFinished = ((time - prevTime) % millisPerSubBeat) / millisPerSubBeat;
		return propFinished == 0 ? 1.0 : propFinished;
	}
}
