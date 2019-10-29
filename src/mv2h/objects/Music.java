package mv2h.objects;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import mv2h.Main;
import mv2h.objects.harmony.Chord;
import mv2h.objects.harmony.ChordProgression;
import mv2h.objects.harmony.Key;
import mv2h.objects.harmony.KeyProgression;
import mv2h.objects.meter.Hierarchy;
import mv2h.objects.meter.Meter;
import mv2h.objects.meter.Tatum;
import mv2h.tools.Aligner;

/**
 * The <code>Music</code> class represents a musical score object. It contains fields which
 * store information about the score itself ({@link #notes}, {@link #voices}, {@link #meter},
 * {@link #keyProgression}, and {@link #chordProgression}), as well as methods to evaluate
 * ({@link #evaluateTranscription(Music)}) and align another Music object with this one
 * ({@link #align(Music, List)}).
 * <br>
 * New Music objects should be created with the {@link #parseMusic(Scanner)} method.
 * 
 * @author Andrew McLeod
 */
public class Music {
	
	/**
	 * The notes present in this score.
	 */
	private final List<Note> notes;
	
	/**
	 * The voices of this score.
	 */
	private final List<Voice> voices;
	
	/**
	 * The metrical structure of this score.
	 */
	private final Meter meter;
	
	/**
	 * The key signature and changes of this score.
	 */
	private final KeyProgression keyProgression;
	
	/**
	 * The chord progression of this score.
	 */
	private final ChordProgression chordProgression;
	
	/**
	 * The last time of this score.
	 */
	private final int lastTime;

	/**
	 * Create a new Music object with the given fields.
	 * <br>
	 * This should not usually be called directly. Rather, use {@link #parseMusic(Scanner)}.
	 * 
	 * @param notes {@link #notes}
	 * @param voices {@link #voices}
	 * @param meter {@link #meter}
	 * @param keyProgression {@link #keyProgression}
	 * @param chordProgression {@link #chordProgression}
	 * @param lastTime {@link #lastTime}
	 */
	public Music(List<Note> notes, List<Voice> voices, Meter meter, KeyProgression keyProgression, ChordProgression chordProgression,
			int lastTime) {
		this.notes = notes;
		Collections.sort(notes);
		
		this.voices = voices;
		this.meter = meter;
		this.keyProgression = keyProgression;
		this.chordProgression = chordProgression;
		this.lastTime = lastTime;
		
		for (Voice voice : voices) {
			voice.createConnections();
		}
	}
	
	/**
	 * Get a list of lists of notes, sorted by onset time. Each 2nd level list
	 * contains all notes which share an identical onset time.
	 * 
	 * @return A list of lists of notes.
	 */
	public List<List<Note>> getNoteLists() {
		List<List<Note>> lists = new ArrayList<List<Note>>();
		if (notes.isEmpty()) {
			return lists;
		}
		// Initialize with the first note
		List<Note> mostRecentList = new ArrayList<Note>();
		int mostRecentValueOnsetTime = notes.get(0).valueOnsetTime;
		lists.add(mostRecentList);
		mostRecentList.add(notes.get(0));
		
		for (int i = 1; i < notes.size(); i++) {
			Note note = notes.get(i);
			
			// Still at the same onset time
			if (mostRecentValueOnsetTime == note.valueOnsetTime) {
				mostRecentList.add(note);
				
			// New onset time
			} else {
				mostRecentList = new ArrayList<Note>();
				mostRecentValueOnsetTime = note.valueOnsetTime;
				lists.add(mostRecentList);
				mostRecentList.add(note);
			}
		}
		
		return lists;
	}
	
	/**
	 * Evaluate a given transcription, treating <code>this</code> object as the ground truth.
	 * 
	 * @param transcription The transcription to evaluate.
	 * 
	 * @return The MV2H evaluation scores object.
	 */
	public MV2H evaluateTranscription(Music transcription) {
		// Tracking objects for notes
		List<Note> transcriptionNotes = new ArrayList<Note>(transcription.notes);
		List<Note> groundTruthNotes = new ArrayList<Note>(notes);
		
		// Tracking lists for voices, which will include only matched notes
		List<Voice> transcriptionVoices = new ArrayList<Voice>(transcription.voices.size());
		for (int i = 0; i < transcription.voices.size(); i++) {
			transcriptionVoices.add(new Voice());
		}
		
		List<Voice> groundTruthVoices = new ArrayList<Voice>(voices.size());
		for (int i = 0; i < voices.size(); i++) {
			groundTruthVoices.add(new Voice());
		}
		
		// Notes which we can check for value accuracy
		List<Note> valueCheckNotes = new ArrayList<Note>();
		
		// A mapping for the ground truth.
		Map<Note, Note> groundTruthNoteMapping = new HashMap<Note, Note>();
		
		
		// Multi-pitch accuracy
		int multiPitchTruePositives = 0;
		Iterator<Note> transcriptionIterator = transcriptionNotes.iterator();
		while (transcriptionIterator.hasNext()) {
			Note transcriptionNote = transcriptionIterator.next();
			
			Iterator<Note> groundTruthIterator = groundTruthNotes.iterator();
			while (groundTruthIterator.hasNext()) {
				Note groundTruthNote = groundTruthIterator.next();
				
				// Match found
				if (transcriptionNote.matches(groundTruthNote)) {
					multiPitchTruePositives++;
					
					groundTruthNoteMapping.put(transcriptionNote, groundTruthNote);
					
					transcriptionVoices.get(transcriptionNote.voice).addNote(transcriptionNote);
					groundTruthVoices.get(groundTruthNote.voice).addNote(groundTruthNote);
					
					groundTruthIterator.remove();
					transcriptionIterator.remove();
					break;
				}
			}
		}
		int multiPitchFalsePositives = transcriptionNotes.size();
		int multiPitchFalseNegatives = groundTruthNotes.size();
		
		double multiPitchF1 = Main.getF1(multiPitchTruePositives, multiPitchFalsePositives, multiPitchFalseNegatives);
		
		// Make voice connections
		for (Voice voice : transcriptionVoices) {
			voice.createConnections();
		}
		for (Voice voice : groundTruthVoices) {
			voice.createConnections();
		}
		
		// Voice separation
		double voiceTruePositives = 0;
		double voiceFalsePositives = 0;
		double voiceFalseNegatives = 0;
		// Go through each voice in the transcription (this is only matched notes)
		for (Voice transcriptionVoice : transcriptionVoices) {
			
			// Go through each note cluster in the transcription voice
			for (NoteCluster transcriptionCluster : transcriptionVoice.noteClusters.values()) {
				
				// Create list of notes which are linked to in the transcription
				List<Note> nextTranscriptionNotesFinal = new ArrayList<Note>();
				for (NoteCluster nextTranscriptionCluster : transcriptionCluster.nextClusters) {
					for (Note nextTranscriptionNote : nextTranscriptionCluster.notes) {
						nextTranscriptionNotesFinal.add(nextTranscriptionNote);
					}
				}
				
				// Go through each note in the note cluster
				for (Note transcriptionNote : transcriptionCluster.notes) {
					Note groundTruthNote = groundTruthNoteMapping.get(transcriptionNote);
				
					// Find the matching ground truth note and its place in its voice
					Voice groundTruthVoice = groundTruthVoices.get(groundTruthNote.voice);
					NoteCluster groundTruthCluster = groundTruthVoice.getNoteCluster(groundTruthNote);
					
					// Create list of notes which are linked to in the ground truth
					List<Note> nextGroundTruthNotesFinal = new ArrayList<Note>();
					for (NoteCluster nextGroundTruthCluster : groundTruthCluster.nextClusters) {
						for (Note nextGroundTruthNote : nextGroundTruthCluster.notes) {
							nextGroundTruthNotesFinal.add(nextGroundTruthNote);
						}
					}
					List<Note> nextGroundTruthNotes = new ArrayList<Note>(nextGroundTruthNotesFinal);
					
					// Save a copy of the linked transcription notes list
					List<Note> nextTranscriptionNotes = new ArrayList<Note>(nextTranscriptionNotesFinal);
					
					// Count how many tp, fp, and fn for these connection sets
					int connectionTruePositives = 0;
					Iterator<Note> transcriptionConnectionIterator = nextTranscriptionNotes.iterator();
					while (transcriptionConnectionIterator.hasNext()) {
						Note nextTranscriptionNote = transcriptionConnectionIterator.next();
						
						Iterator<Note> groundTruthConnectionIterator = nextGroundTruthNotes.iterator();
						while (groundTruthConnectionIterator.hasNext()) {
							Note nextGroundTruthNote = groundTruthConnectionIterator.next();
							
							// Match found
							if (nextTranscriptionNote.matches(nextGroundTruthNote)) {
								connectionTruePositives++;
								
								groundTruthConnectionIterator.remove();
								transcriptionConnectionIterator.remove();
								break;
							}
						}
					}
					int connectionFalsePositives = nextTranscriptionNotes.size();
					int connectionFalseNegatives = nextGroundTruthNotes.size();
					
					// Normalize counts before adding to totals, so that each connection is weighted equally
					double outWeight = (nextGroundTruthNotesFinal.size() + nextTranscriptionNotesFinal.size()) / 2.0;
					if (outWeight > 0) {
						voiceTruePositives += ((double) connectionTruePositives) / (outWeight * transcriptionCluster.notes.size());
						voiceFalsePositives += ((double) connectionFalsePositives) / (outWeight * transcriptionCluster.notes.size());
						voiceFalseNegatives += ((double) connectionFalseNegatives) / (outWeight * transcriptionCluster.notes.size());
					}
					
					// Add note to list to noteValue check
					
					// List of notes which are linked to in the original ground truth (including multi-pitch non-TPs)
					List<Note> nextOriginalGroundTruthNotes = new ArrayList<Note>();
					for (NoteCluster nextGroundTruthCluster : voices.get(groundTruthNote.voice).getNoteCluster(groundTruthNote).nextClusters) {
						for (Note nextGroundTruthNote : nextGroundTruthCluster.notes) {
							nextOriginalGroundTruthNotes.add(nextGroundTruthNote);
						}
					}
					
					// Both are the end of a voice
					if (nextOriginalGroundTruthNotes.isEmpty() && nextTranscriptionNotesFinal.isEmpty()) {
						valueCheckNotes.add(transcriptionNote);
						
					} else {
						// Check if at least one original ground truth connection was correct
						boolean match = false;
						for (Note nextGroundTruthNote : nextOriginalGroundTruthNotes) {
							for (Note nextTranscriptionNote : nextTranscriptionNotesFinal) {
								if (nextTranscriptionNote.matches(nextGroundTruthNote)) {
									match = true;
									valueCheckNotes.add(transcriptionNote);
									break;
								}
							}
							
							if (match) {
								break;
							}
						}
					}
				}
			}
		}
		double voiceF1 = Main.getF1(voiceTruePositives, voiceFalsePositives, voiceFalseNegatives);
		
		
		// Meter
		double meterF1 = transcription.meter.getF1(meter);
		
		
		// Note value (check only GT matches and GT voice matches)
		double valueScoreSum = 0.0;
		for (Note transcriptionNote : valueCheckNotes) {
			valueScoreSum += transcriptionNote.getValueScore(groundTruthNoteMapping.get(transcriptionNote));
		}
		double valueScore = valueScoreSum / valueCheckNotes.size();
		if (Double.isNaN(valueScore)) {
			valueScore = 0.0;
		}
		
		
		// Harmony
		double keyScore = transcription.keyProgression.getScore(keyProgression, lastTime);
		double progressionScore = transcription.chordProgression.getScore(chordProgression, lastTime);
		
		double harmonyScore = (keyScore + progressionScore) / 2;
		if (Double.isNaN(progressionScore)) {
			harmonyScore = keyScore;
		}
		
		if (Double.isNaN(keyScore)) {
			harmonyScore = progressionScore;
		}
		
		if (Double.isNaN(harmonyScore)) {
			harmonyScore = 0.0;
		}
		
		// MV2H
		return new MV2H(multiPitchF1, voiceF1, meterF1, valueScore, harmonyScore);
	}

	/**
	 * Get a new Music object whose times are mapped to the corresponding ground truth's
	 * times given the alignment.
	 * 
	 * @param gt The ground truth Music object.
	 * @param alignment The alignment to re-map with.
	 * An alignment is a list containing, for each ground truth note list, the index of the transcription
	 * note list to which it is aligned, or -1 if it was not aligned with any transcription note.
	 * 
	 * @return A new Music object with the given alignment.
	 */
	public Music align(Music gt, List<Integer> alignment) {
		List<Note> newNotes = new ArrayList<Note>(notes.size());
		List<Voice> newVoices = new ArrayList<Voice>(voices.size());
		
		// Convert each note into a new note
		for (Note note : notes) {
			newNotes.add(new Note(
					note.pitch,
					Aligner.convertTime(note.onsetTime, gt, this, alignment),
					Aligner.convertTime(note.valueOnsetTime, gt, this, alignment),
					Aligner.convertTime(note.valueOffsetTime, gt, this, alignment),
					note.voice));
			
			while (note.voice >= newVoices.size()) {
				newVoices.add(new Voice());
			}
			newVoices.get(note.voice).addNote(newNotes.get(newNotes.size() - 1));
		}
		
		// Convert the metrical structure times
		Meter newMeter = new Meter(Aligner.convertTime(0, gt, this, alignment));
		for (Hierarchy h : meter.getHierarchies()) {
			newMeter.addHierarchy(new Hierarchy(h.beatsPerBar, h.subBeatsPerBeat, h.tatumsPerSubBeat, h.anacrusisLengthTatums,
					Aligner.convertTime(h.time, gt, this, alignment)));
		}
		for (Tatum tatum : meter.getTatums()) {
			newMeter.addTatum(new Tatum(Aligner.convertTime(tatum.time, gt, this, alignment)));
		}
		
		// Convert the key change times
		KeyProgression newKeyProgression = new KeyProgression();
		for (Key key : keyProgression.getKeys()) {
			newKeyProgression.addKey(new Key(key.tonic, key.isMajor, Aligner.convertTime(key.time, gt, this, alignment)));
		}
		
		// Convert the chord change times
		ChordProgression newChordProgression = new ChordProgression();
		for (Chord chord : chordProgression.getChords()) {
			newChordProgression.addChord(new Chord(chord.chord, Aligner.convertTime(chord.time, gt, this, alignment)));
		}
		
		// Create and return the new Music object
		return new Music(newNotes, newVoices, newMeter, newKeyProgression, newChordProgression,
				         Aligner.convertTime(lastTime, gt, this, alignment));
	}

	/**
	 * Parse a musical score from the given scanner in mv2h format and return a corresponding
	 * Music object.
	 * 
	 * @param input The input stream to read from.
	 * @return The parsed Music object.
	 * @throws IOException If there was an error in reading or parsing the stream.
	 */
	public static Music parseMusic(Scanner input) throws IOException {
		// Tracking variables
		List<Note> notes = new ArrayList<Note>();
		List<Voice> voices = new ArrayList<Voice>();
		Meter meter = new Meter();
		ChordProgression chordProgression = new ChordProgression();
		KeyProgression keyProgression = new KeyProgression();
		int lastTime = Integer.MIN_VALUE;
		
		// Read through input
		while (input.hasNextLine()) {
			String line = input.nextLine();
			
			// Check for matching prefixes, and pass each to its corresponding parser.
			if (line.startsWith("Note")) {
				Note note = Note.parseNote(line);
				notes.add(note);
				
				// Add note to voice
				while (note.voice >= voices.size()) {
					voices.add(new Voice());
				}
				voices.get(note.voice).addNote(note);
				
				lastTime = Math.max(lastTime, note.valueOffsetTime);
				
			} else if (line.startsWith("Tatum")) {
				Tatum tatum = Tatum.parseTatum(line);
				meter.addTatum(tatum);
				
				lastTime = Math.max(lastTime, tatum.time);
				
			} else if (line.startsWith("Chord")) {
				Chord chord = Chord.parseChord(line);
				chordProgression.addChord(chord);
				
				lastTime = Math.max(lastTime, chord.time);

			} else if (line.startsWith("Hierarchy")) {
				Hierarchy hierarchy = Hierarchy.parseHierarchy(line);
				meter.addHierarchy(hierarchy);

			} else if (line.startsWith("Key")) {
				Key key = Key.parseKey(line);
				keyProgression.addKey(key);
				
				lastTime = Math.max(lastTime, key.time);
			}
		}
		input.close();
		
		return new Music(notes, voices, meter, keyProgression, chordProgression, lastTime);
	}

}
