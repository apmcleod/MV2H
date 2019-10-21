package mv2h.tools.midi;

import java.io.File;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import mv2h.objects.Note;

/**
 * A <code>MidiEventParser</code> handles the interfacing between this program and MIDI files.
 * It can read in MIDI events from a file with {@link #run()}.
 * 
 * @author Andrew McLeod
 */
public class MidiEventParser {
	/**
	 * The mask for reading the channel number from a MidiMessage.
	 */
	public static final int CHANNEL_MASK = 0x0f;
	
	/**
	 * The mask for reading the message type from a MidiMessage.
	 */
	public static final int MESSAGE_MASK = 0xf0;
	
	/**
	 * The constant which midi uses for tempo change events.
	 */
	public static final int TEMPO = 0x51;
	
	/**
	 * The constant which midi uses for time signature change events.
	 */
	public static final int TIME_SIGNATURE = 0x58;
	
	/**
	 * The constant which midi uses for key signature change events.
	 */
	public static final int KEY_SIGNATURE = 0x59;
	
	/**
	 * The TimeTracker which will handle timing information for this song.
	 */
	private TimeTracker timeTracker;
	
	/**
	 * The NoteTracker which will keep track of the notes for this song.
	 */
	private final NoteEventParser noteEventParser;
	
	/**
	 * The song we are parsing.
	 */
	private final Sequence song;
	
	/**
	 * The first note time.
	 */
	private int firstNoteTime;
	
	/**
	 * True if we want to use the input data's channel as gold standard voices. False to use track instead.
	 */
	private boolean useChannel;
    
	/**
	 * Creates a new MidiEventParser
	 * 
	 * @param midiFile The MIDI file we will parse.
	 * @param noteEventParser The NoteEventParser to pass events to when we run this parser.
	 * @param useChannel True if we want to use the input data's channel as gold standard voices.
	 * False to use track instead.
	 * @throws IOException If an I/O error occurred when reading the given file. 
	 * @throws InvalidMidiDataException If the given file was is not in a valid MIDI format.
	 */
    public MidiEventParser(File midiFile, NoteEventParser noteEventParser, TimeTracker timeTracker, boolean useChannel)
    		throws InvalidMidiDataException, IOException{
    	song = MidiSystem.getSequence(midiFile);
    	
    	this.noteEventParser = noteEventParser;
    	this.timeTracker = timeTracker;
    	
    	timeTracker.setPPQ(song.getResolution());
    	
    	this.useChannel = useChannel;
    	
    	firstNoteTime = Integer.MAX_VALUE;
    }
	
    /**
     * Parses the events from the loaded MIDI file through to the NoteTracker.
     * @throws InvalidMidiDataException If a note off event doesn't match any previously seen note on.
     */
    public void run() throws InvalidMidiDataException {
    	long lastTick = 0;
    	
    	Track[] tracks = song.getTracks();
    	int nextEventIndex[] = new int[tracks.length];
    	long nextEventTick[] = new long[nextEventIndex.length];
    	
    	// Initialize tracking arrays
    	for (int trackNum = 0; trackNum < tracks.length; trackNum++) {
    		if (tracks[trackNum].size() == 0) {
    			nextEventIndex[trackNum] = -1;
    			nextEventTick[trackNum] = -1;
    			
    		} else {
    			nextEventTick[trackNum] = tracks[trackNum].get(0).getTick();
    		}
    	}
    	
    	while (moreEventsRemain(nextEventIndex)) {
	    	int trackNum = getNextTrackNum(nextEventTick);
	        MidiEvent event = tracks[trackNum].get(nextEventIndex[trackNum]);
	        
	        nextEventIndex[trackNum]++;
	        if (nextEventIndex[trackNum] == tracks[trackNum].size()) {
	        	nextEventIndex[trackNum] = -1;
	        	nextEventTick[trackNum] = -1;
	        	
	        } else {
	        	nextEventTick[trackNum] = tracks[trackNum].get(nextEventIndex[trackNum]).getTick();
	        }
	        
	        MidiMessage message = event.getMessage();
	        ShortMessage sm;
	        int status = message.getStatus();
	        
	        int key, velocity;
	                
	        lastTick = Math.max(lastTick, event.getTick());
	                
	        if (status == MetaMessage.META) {
	           	MetaMessage mm = (MetaMessage) message;
	             	
	           	switch (mm.getType()) {
	           		case TEMPO:
	           			// Tempo change
	           			timeTracker.addTempoChange(event, mm);
	           			break;
	                		
	           		case TIME_SIGNATURE:
	           			// Time signature change
	           			timeTracker.addTimeSignatureChange(event, mm);
	           			break;
	                			
	           		case KEY_SIGNATURE:
	           			// Key signature
	           			timeTracker.addKeyChange(event, mm);
	           			break;
	               			
	           		default:
	           			break;
	           	}
	                	
	        } else {
	           	int channel = status & CHANNEL_MASK;
	                	
	           	int correctVoice = useChannel ? channel : trackNum;
	                	
		        switch (status & MESSAGE_MASK) {
			                	
		           	case ShortMessage.NOTE_ON:
		           		sm = (ShortMessage) message;
		                		
		           		key = sm.getData1();
		                velocity = sm.getData2();
		                       
		                if (velocity != 0) {
		                   	Note note = noteEventParser.noteOn(key, velocity, event.getTick(), correctVoice);
		                   	firstNoteTime = Integer.min(firstNoteTime, note.onsetTime);
		                   	break;
		                }
		                        
		                // Fallthrough on velocity == 0 --> this is a NOTE_OFF
		           	case ShortMessage.NOTE_OFF:
		           		sm = (ShortMessage) message;
		                		
		           		key = sm.getData1();
		                		
		                noteEventParser.noteOff(key, event.getTick(), correctVoice);
		                break;
		                        
		            default:
		               	break;
		        }
		    }
        }
        
        timeTracker.setLastTick(lastTick);
    }
    
    /**
     * Return true if some notes remain. That is, if some track still
     * has events left. That is, if any index contains a value other than
     * -1.
     * 
     * @param nextEventIndex The indices of the next event for each track.
     * -1 i none remains.
     * 
     * @return True if some track has an event remaining. False otherwise.
     */
    private boolean moreEventsRemain(int[] nextEventIndex) {
		for (int index : nextEventIndex) {
			if (index != -1) {
				return true;
			}
		}
		
		return false;
	}

    /**
     * Get the index of the track with the next event.
     * 
     * @param nextEventTick The ticks for the next event on each of the tracks.
     * @return The index of the minimum value that isn't -1.
     */
	private int getNextTrackNum(long[] nextEventTick) {
		int minIndex = -1;
		
		for (int i = 0; i < nextEventTick.length; i++) {
			if (nextEventTick[i] != -1) {
				if (minIndex == -1 || nextEventTick[i] < nextEventTick[minIndex]) {
					minIndex = i;
				}
			}
		}
		
		return minIndex;
	}
}
