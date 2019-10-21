package mv2h.tools;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import mv2h.objects.Note;
import mv2h.objects.harmony.Key;
import mv2h.objects.meter.Hierarchy;
import mv2h.objects.meter.Tatum;

/**
 * The <code>MusicXMLConverter</code> class is used to convert a given output from the MusicXMLParser
 * into a format that can be read by the MV2H package (standard out).
 * 
 * @author Andrew McLeod
 */
public class MusicXmlConverter extends Converter {
	/**
	 * The notes present in the XML piece.
	 */
	private List<Note> notes = new ArrayList<Note>();
	
	/**
	 * The hierarchies of this piece.
	 */
	private List<Hierarchy> hierarchies = new ArrayList<Hierarchy>();
	
	/**
	 * The tick of the starting time of each hierarchy.
	 */
	private List<Integer> hierarchyTicks = new ArrayList<Integer>();
	
	/**
	 * A list of the key signatures of this piece.
	 */
	private List<Key> keys = new ArrayList<Key>();
	
	/**
	 * A list of the notes for which there hasn't yet been an offset.
	 */
	private List<Note> unfinishedNotes = new ArrayList<Note>();
	
	/**
	 * The last tick of the piece.
	 */
	private int lastTick = 0;
	
	/**
	 * The first tick of the piece.
	 */
	private int firstTick = Integer.MAX_VALUE;
	
	/**
	 * The bar of the previous line. This is kept updated until the anacrusis is handled.
	 * <br>
	 * @see #handleAnacrusis(int, int, int, int)
	 */
	private int previousBar = -1;
	
	/**
	 * The number of ticks per quarter note. 1 tick is 1 tatum. Defaults to 4.
	 */
	private int ticksPerQuarterNote = 4;

	/**
	 * Create a new MusicXMLConverter object by parsing the input from the MusicXMLParser.
	 * <br>
	 * This method contains the main program logic, and printing is handled by
	 * {@link #toString()}.
	 * 
	 * @param stream The MusicXMLParser output to convert.
	 */
	public MusicXmlConverter(InputStream stream) {
		Scanner in = new Scanner(stream);
		int lineNum = 0;
		boolean anacrusisHandled = false;
		
		while (in.hasNextLine()) {
			lineNum++;
			String line = in.nextLine();
			
			// Skip comment lines
			if (line.startsWith("//")) {
				continue;
			}
			
			String[] attributes = line.split("\t");
			if (attributes.length < 5) {
				// Error if fewer than 5 columns
				System.err.println("WARNING: Line type not found. Skipping line " + lineNum + ": " + line);
				continue;
			}
			
			int tick = Integer.parseInt(attributes[0]);
			int voice = Integer.parseInt(attributes[4]);
			
			lastTick = Math.max(tick, lastTick);
			firstTick = Math.min(tick, firstTick);
			
			// Switch for different types of lines
			switch (attributes[5]) {
				// Attributes is the base line type describing time signature, tempo, etc.
				case "attributes":
					ticksPerQuarterNote = Integer.parseInt(attributes[6]);
					
					// Time signature
					int tsNumerator = Integer.parseInt(attributes[9]);
					int tsDenominator = Integer.parseInt(attributes[10]);
					
					int beatsPerBar = tsNumerator;
					int subBeatsPerBeat = 2;
					
					int subBeatsPerQuarterNote = tsDenominator / 2;
					
					// Check for compound meter
					if (beatsPerBar % 3 == 0 && beatsPerBar > 3) {
						beatsPerBar /= 3;
						subBeatsPerBeat = 3;
						
						subBeatsPerQuarterNote = tsDenominator / 4;
					}
					
					int tatumsPerSubBeat = ticksPerQuarterNote / subBeatsPerQuarterNote;
					
					// Add the new time signature (if it is new)
					Hierarchy mostRecent = hierarchies.isEmpty() ? null : hierarchies.get(hierarchies.size() - 1);
					if (mostRecent == null || mostRecent.beatsPerBar != beatsPerBar ||
							mostRecent.subBeatsPerBeat != subBeatsPerBeat || mostRecent.tatumsPerSubBeat != tatumsPerSubBeat) {
						hierarchyTicks.add(tick);
						hierarchies.add(new Hierarchy(beatsPerBar, subBeatsPerBeat, tatumsPerSubBeat, 0, getTimeFromTick(tick)));
					}
					
					// Key signature
					int keyFifths = Integer.parseInt(attributes[7]);
					String keyMode = attributes[8];
					
					int tonic = ((7 * keyFifths) + 144) % 12;
					boolean mode = keyMode.equalsIgnoreCase("Major");
					
					// Add the new key (if it is new)
					Key mostRecentKey = keys.isEmpty() ? null : keys.get(keys.size() - 1);
					if (mostRecentKey == null || mostRecentKey.tonic != tonic || mostRecentKey.isMajor != mode) {
						keys.add(new Key(tonic, mode, getTimeFromTick(tick)));
					}
					
					break;
					
				case "rest":
					// Handle anacrusis
					if (!anacrusisHandled) {
						anacrusisHandled = handleAnacrusis(Integer.parseInt(attributes[1]), tick);
					}
					break;
					
				case "chord":
					// There are notes here
					
					// Handle anacrusis
					if (!anacrusisHandled) {
						anacrusisHandled = handleAnacrusis(Integer.parseInt(attributes[1]), tick);
					}
					
					int duration = Integer.parseInt(attributes[6]);
					lastTick = Math.max(tick + duration, lastTick);
					
					int tieInfo = Integer.parseInt(attributes[7]);
					int numNotes = Integer.parseInt(attributes[8]);
					
					// Get all of the pitches
					int[] pitches = new int[numNotes];
					for (int i = 0; i < numNotes; i++) {
						try {
							pitches[i] = getPitchFromString(attributes[9 + i]);
						} catch (IOException e) {
							System.err.println("WARNING: " + e.getMessage() + " Skipping line " + lineNum + ": " + line);
							continue;
						}
					}
					
					// Handle each pitch
					for (int pitch : pitches) {
						switch (tieInfo) {
							// No tie
							case 0:
								notes.add(new Note(pitch, getTimeFromTick(tick), getTimeFromTick(tick), getTimeFromTick(tick + duration), voice));
								break;
								
							// Tie out
							case 1:
								unfinishedNotes.add(new Note(pitch, getTimeFromTick(tick), getTimeFromTick(tick), getTimeFromTick(tick + duration), voice));
								break;
								
							// Tie in
							case 2:
								try {
									Note matchedNote = findAndRemoveUnfinishedNote(pitch, getTimeFromTick(tick), voice);
									notes.add(new Note(pitch, matchedNote.onsetTime, matchedNote.valueOnsetTime, getTimeFromTick(tick), voice));
								} catch (IOException e) {
									System.err.println("WARNING: " + e.getMessage() + " Adding tied note as new note " + lineNum + ": " + line);
									notes.add(new Note(pitch, getTimeFromTick(tick), getTimeFromTick(tick), getTimeFromTick(tick + duration), voice));
								}
								break;
								
							// Tie in and out
							case 3:
								try {
									Note matchedNote = findAndRemoveUnfinishedNote(pitch, getTimeFromTick(tick), voice);
									unfinishedNotes.add(new Note(pitch, matchedNote.onsetTime, matchedNote.valueOnsetTime, getTimeFromTick(tick + duration), voice));
								} catch (IOException e) {
									System.err.println("WARNING: " + e.getMessage() + " Skipping note on line " + lineNum + ": " + line);
									unfinishedNotes.add(new Note(pitch, getTimeFromTick(tick), getTimeFromTick(tick), getTimeFromTick(tick + duration), voice));
								}
								break;
								
							// ???
							default:
								System.err.println("WARNING: Unknown tie type " + tieInfo + ". Skipping line " + lineNum + ": " + line);
								break;
						}
					}
					break;
					
				case "tremolo-m":
					duration = Integer.parseInt(attributes[6]);
					lastTick = Math.max(tick + duration, lastTick);
					
					numNotes = Integer.parseInt(attributes[8]);
					
					pitches = new int[numNotes];
					for (int i = 0; i < numNotes; i++) {
						try {
							pitches[i] = getPitchFromString(attributes[9 + i]);
						} catch (IOException e) {
							System.err.println("WARNING: " + e.getMessage() + " Skipping line " + lineNum + ": " + line);
							continue;
						}
					}
					
					for (int pitch : pitches) {
						// TODO: Decide how many notes to add here. i.e., default to eighth notes for now?
						int ticksPerTremolo = ticksPerQuarterNote / 2;
						int numTremolos = duration / ticksPerTremolo;
						
						
						for (int i = 0; i < numTremolos; i++) {
							notes.add(new Note(pitch, getTimeFromTick(tick + ticksPerTremolo * i), getTimeFromTick(tick + ticksPerTremolo * i), getTimeFromTick(tick + ticksPerTremolo * (i + 1)), voice));
						}
					}
					break;
					
				default:
					System.err.println("WARNING: Unrecognized line type. Skipping line " + lineNum + ": " + line);
					continue;
			}
		}
		
		in.close();
		
		// Check for any unfinished notes (because of ties out).
		for (Note note : unfinishedNotes) {
			System.err.println("WARNING: Tie never ended for note " + note + ". Adding note as untied.");
			notes.add(note);
		}
	}
	
	/**
	 * Handle any anacrusis, if possible. First, detect if at least 1 bar has finished. If it has,
	 * check how long the previous bar was, and set the first anacrusis according to that.
	 * 
	 * @param bar The bar number of the current line. This will be compared to {@link #previousBar}
	 * to check if a bar has just finished.
	 * @param tick The tick of the current line.
	 * @return True if the anacrusis has now been handled. False otherwise.
	 */
	private boolean handleAnacrusis(int bar, int tick) {
		if (previousBar == -1) {
			// This is the first bar we've seen
			previousBar = bar;
			
		} else if (previousBar != bar) {
			// Ready to handle the anacrusis
			
			// Add a default 4/4 at time 0 if no hierarchy has been seen yet
			if (hierarchies.isEmpty()) {
				hierarchies.add(new Hierarchy(4, 2, ticksPerQuarterNote / 2, 0, 0));
			}
			
			if (hierarchies.size() != 1) {
				System.err.println("Warning: More than 1 time signature seen in the first bar.");
			}
			
			// Duplicate mostRecent, but with correct anacrusis (tick % tatumsPerBar)
			Hierarchy mostRecent = hierarchies.get(hierarchies.size() - 1);
			int tatumsPerBar = mostRecent.beatsPerBar * mostRecent.subBeatsPerBeat * mostRecent.tatumsPerSubBeat;
			hierarchies.set(hierarchies.size() - 1, new Hierarchy(mostRecent.beatsPerBar, mostRecent.subBeatsPerBeat,
					mostRecent.tatumsPerSubBeat, tick % tatumsPerBar, mostRecent.time));
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Find a tied out note that matches a new tied in note, return it, and remove it from
	 * {@link #unfinishedNotes}.
	 * 
	 * @param pitch The pitch of the tie.
	 * @param valueOnsetTime The onset time of the tied in note.
	 * @param voice The voice of the tied in note.
	 * 
	 * @return The note from {@link #unfinishedNotes} that matches the pitch onset time and voice.
	 * @throws IOException If no matching note is found.
	 */
	private Note findAndRemoveUnfinishedNote(int pitch, int valueOnsetTime, int voice) throws IOException {
		Iterator<Note> noteIterator = unfinishedNotes.iterator();
		while (noteIterator.hasNext()) {
			Note note = noteIterator.next();
			
			if (note.pitch == pitch && note.valueOffsetTime == valueOnsetTime && note.voice == voice) {
				noteIterator.remove();
				return note;
			}
		}
		
		throw new IOException("Tied note not found at pitch=" + pitch + " offset=" + valueOnsetTime + " voice=" + voice + ".");
	}
	
	/**
	 * Convert from XML tick to time, using {@link #MS_PER_BEAT}.
	 * 
	 * @param tick The XML tick.
	 * @return The time, in milliseconds.
	 */
	private int getTimeFromTick(int tick) {
		if (hierarchies.isEmpty()) {
			return tick;
		}
		
		int i;
		for (i = 0; i < hierarchies.size() - 1; i++) {
			if (hierarchyTicks.get(i + 1) > tick) {
				break;
			}
		}
		
		Hierarchy hierarchy = hierarchies.get(i);
		int hierarchyTick = hierarchyTicks.get(i);
		
		return hierarchy.time + (int) Math.round(((double) tick - hierarchyTick) / hierarchy.tatumsPerSubBeat / hierarchy.subBeatsPerBeat * MS_PER_BEAT);
	}
	
	/**
	 * Create and return a list of tatums based on the parsed {@link #hierarchy}, {@link #firstTick}, and
	 * {@link #lastTick}.
	 * 
	 * @return A list of the parsed tatums.
	 */
	private List<Tatum> getTatums() {
		List<Tatum> tatums = new ArrayList<Tatum>(lastTick - firstTick);
		
		for (int tick = firstTick; tick < lastTick; tick++) {
			tatums.add(new Tatum(getTimeFromTick(tick)));
		}
		
		return tatums;
	}
	
	/**
	 * Return a String version of the parsed musical score into our mv2h format.
	 * 
	 * @return The parsed musical score.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for (Note note : notes) {
			sb.append(note).append('\n');
		}
		
		for (Tatum tatum : getTatums()) {
			sb.append(tatum).append('\n');
		}
		
		for (Key key : keys) {
			sb.append(key).append('\n');
		}
		
		for (Hierarchy hierarchy : hierarchies) {
			sb.append(hierarchy).append('\n');
		}
		
		return sb.toString();
	}
	
	/**
	 * Get the pitch number of a note given its String.
	 * 
	 * @param pitchString A pitch String, like C##4, or Ab1, or G7.
	 * @return The number of the given pitch, with A4 = 440Hz = 69.
	 * 
	 * @throws IOException If a parse error occurs.
	 */
	private static int getPitchFromString(String pitchString) throws IOException {
		char pitchChar = pitchString.charAt(0);
		int pitch;
		switch (pitchChar) {
			case 'C':
				pitch = 0;
				break;
				
			case 'D':
				pitch = 2;
				break;
				
			case 'E':
				pitch = 4;
				break;
				
			case 'F':
				pitch = 5;
				break;
				
			case 'G':
				pitch = 7;
				break;
				
			case 'A':
				pitch = 9;
				break;
				
			case 'B':
				pitch = 11;
				break;
				
			default:
				throw new IOException("Pith " + pitchChar + " not recognized.");
		}
		
		int accidental = pitchString.length() - pitchString.replace("#", "").length();
		accidental -= pitchString.length() - pitchString.replace("b", "").length();
		
		int octave = Integer.parseInt(pitchString.substring(1).replace("#", "").replace("b", ""));						
		
		return (octave + 1) * 12 + pitch + accidental;
	}
}
