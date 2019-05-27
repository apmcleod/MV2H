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

public class Music {
	
	private final List<Note> notes;
	private final List<Voice> voices;
	private final Meter meter;
	private final KeyProgression keyProgression;
	private final ChordProgression chordProgression;
	
	private final int lastTime;

	public Music(List<Note> notes, List<Voice> voices, Meter meter, KeyProgression keyProgression, ChordProgression progression,
			int lastTime) {
		this.notes = notes;
		Collections.sort(notes);
		
		this.voices = voices;
		this.meter = meter;
		this.keyProgression = keyProgression;
		this.chordProgression = progression;
		this.lastTime = lastTime;
	}
	
	public List<List<Note>> getNoteLists() {
		List<List<Note>> lists = new ArrayList<List<Note>>();
		if (notes.isEmpty()) {
			return lists;
		}
		
		List<Note> mostRecentList = new ArrayList<Note>();
		int mostRecentValueOnsetTime = notes.get(0).valueOnsetTime;
		lists.add(mostRecentList);
		mostRecentList.add(notes.get(0));
		
		for (int i = 1; i < notes.size(); i++) {
			Note note = notes.get(i);
			
			if (mostRecentValueOnsetTime == note.valueOnsetTime) {
				mostRecentList.add(note);
				
			} else {
				mostRecentList = new ArrayList<Note>();
				mostRecentValueOnsetTime = note.valueOnsetTime;
				lists.add(mostRecentList);
				mostRecentList.add(note);
			}
		}
		
		return lists;
	}
	
	public MV2H evaluateTranscription(Music transcription) {
		if (!Main.PERFORM_ALIGNMENT) {
			return evaluateTranscription(transcription, null);
		}
		
		MV2H best = new MV2H(0, 0, 0, 0, 0);
		
		for (List<Integer> alignment : Aligner.getPossibleAlignments(this, transcription)) {
			MV2H candidate = evaluateTranscription(transcription, alignment);
			
			if (candidate.compareTo(best) > 0) {
				best = candidate;
			}
		}
		
		return best;
	}
		
	public MV2H evaluateTranscription(Music transcription, List<Integer> alignment) {
		if (alignment != null) {
			transcription = transcription.align(this, alignment);
		}
		
		// Tracking objects
		List<Note> transcriptionNotes = new ArrayList<Note>(transcription.notes);
		List<Note> groundTruthNotes = new ArrayList<Note>(notes);
		
		List<List<Note>> transcriptionVoices = new ArrayList<List<Note>>(transcription.voices.size());
		for (int i = 0; i < transcription.voices.size(); i++) {
			transcriptionVoices.add(new ArrayList<Note>());
		}
		
		List<List<Note>> groundTruthVoices = new ArrayList<List<Note>>(voices.size());
		for (int i = 0; i < voices.size(); i++) {
			groundTruthVoices.add(new ArrayList<Note>());
		}
		
		List<Note> valueCheckNotes = new ArrayList<Note>();
		
		Map<Note, Note> groundTruthNoteMapping = new HashMap<Note, Note>();
		
		
		// Multi-pitch accuracy
		int multiPitchTruePositives = 0;
		Iterator<Note> transcriptionIterator = transcriptionNotes.iterator();
		while (transcriptionIterator.hasNext()) {
			Note transcriptionNote = transcriptionIterator.next();
			
			Iterator<Note> groundTruthIterator = groundTruthNotes.iterator();
			while (groundTruthIterator.hasNext()) {
				Note groundTruthNote = groundTruthIterator.next();
				
				if (transcriptionNote.matches(groundTruthNote)) {
					multiPitchTruePositives++;
					
					groundTruthNoteMapping.put(transcriptionNote, groundTruthNote);
					
					transcriptionVoices.get(transcriptionNote.voice).add(transcriptionNote);
					groundTruthVoices.get(groundTruthNote.voice).add(groundTruthNote);
					
					groundTruthIterator.remove();
					transcriptionIterator.remove();
					break;
				}
			}
		}
		int multiPitchFalsePositives = transcriptionNotes.size();
		int multiPitchFalseNegatives = groundTruthNotes.size();
		
		double multiPitchF1 = Main.getF1(multiPitchTruePositives, multiPitchFalsePositives, multiPitchFalseNegatives);
		
		for (List<Note> voice : transcriptionVoices) {
			Collections.sort(voice);
		}
		for (List<Note> voice : groundTruthVoices) {
			Collections.sort(voice);
		}
		
		// Voice separation
		int voiceTruePositives = 0;
		int voiceFalsePositives = 0;
		for (List<Note> transcriptionVoice : transcriptionVoices) {
			for (int transcriptionIndex = 0; transcriptionIndex < transcriptionVoice.size() - 1; transcriptionIndex++) {
				Note transcriptionNote = transcriptionVoice.get(transcriptionIndex);
				Note groundTruthNote = groundTruthNoteMapping.get(transcriptionNote);
				
				List<Note> groundTruthVoice = groundTruthVoices.get(groundTruthNote.voice);
				int groundTruthNoteIndex = Collections.binarySearch(groundTruthVoice, groundTruthNote);
				
				if (groundTruthNoteIndex != groundTruthVoice.size() - 1 && groundTruthNoteIndex >= 0 &&
						groundTruthVoice.get(groundTruthNoteIndex + 1).equals(groundTruthNoteMapping.get(transcriptionVoice.get(transcriptionIndex + 1)))) {
					voiceTruePositives++;
					
					// Check if we should count this note for note value score
					Iterator<Note> originalGroundTruthVoiceIterator = voices.get(groundTruthNote.voice).getNotes().iterator();
					while (originalGroundTruthVoiceIterator.hasNext()) {
						Note originalGroundTruthNote = originalGroundTruthVoiceIterator.next();
						
						// We found the correct original ground truth note
						if (originalGroundTruthNote.equals(groundTruthNote)) {
							
							// The next note in the original ground truth voice is the next note in the ground truth voice
							// We don't need to count this note if it has no following original ground truth. Those are checked later.
							if (originalGroundTruthVoiceIterator.hasNext() &&
									originalGroundTruthVoiceIterator.next().equals(groundTruthVoice.get(groundTruthNoteIndex + 1))) {
								valueCheckNotes.add(transcriptionNote);
							}
							
							break;
						}
					}
					
				} else {
					voiceFalsePositives++;
				}
			}
		}
		
		// Check if last notes should be checked for note value
		for (List<Note> transcriptionVoice : transcriptionVoices) {
			if (transcriptionVoice.isEmpty()) {
				continue;
			}
			Note lastNote = transcriptionVoice.get(transcriptionVoice.size() - 1);
			Note groundTruthNote = groundTruthNoteMapping.get(lastNote);
			Voice originalGroundTruthVoice = voices.get(groundTruthNote.voice);
			List<Note> originalGroundTruthNotes = originalGroundTruthVoice.getNotes();
			
			if (Collections.binarySearch(originalGroundTruthNotes, groundTruthNote) ==
					originalGroundTruthNotes.size() - 1) {
				valueCheckNotes.add(lastNote);
			}
		}
		
		int voiceFalseNegatives = -voiceTruePositives;
		for (List<Note> groundTruthVoice : groundTruthVoices) {
			if (!groundTruthVoice.isEmpty()) {
				voiceFalseNegatives += groundTruthVoice.size() - 1;
			}
		}
		
		double voiceF1 = Main.getF1(voiceTruePositives, voiceFalsePositives, voiceFalseNegatives);
		
		
		// Meter
		double meterF1 = transcription.meter.getF1(meter);
		
		
		// Note value
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
		
		// MV2H
		return new MV2H(multiPitchF1, voiceF1, meterF1, valueScore, harmonyScore);
	}

	/**
	 * Get a new Music object whose times are mapped to the corresponding ground truth's
	 * times given the alignment.
	 * 
	 * @param gt The ground truth Music object.
	 * @param alignment The alignment to re-map with.
	 * 
	 * @return A new Music object with the given alignment.
	 */
	private Music align(Music gt, List<Integer> alignment) {
		List<Note> newNotes = new ArrayList<Note>(notes.size());
		List<Voice> newVoices = new ArrayList<Voice>(voices.size());
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
		
		Meter newMeter = new Meter();
		newMeter.setHierarchy(meter.getHierarchy());
		for (Tatum tatum : meter.getTatums()) {
			newMeter.addTatum(new Tatum(Aligner.convertTime(tatum.time, gt, this, alignment)));
		}
		
		KeyProgression newKeyProgression = new KeyProgression();
		for (Key key : keyProgression.getKeys()) {
			newKeyProgression.addKey(new Key(key.tonic, key.isMajor, Aligner.convertTime(key.time, gt, this, alignment)));
		}
		
		ChordProgression newChordProgression = new ChordProgression();
		for (Chord chord : chordProgression.getChords()) {
			newChordProgression.addChord(new Chord(chord.chord, Aligner.convertTime(chord.time, gt, this, alignment)));
		}
		
		return new Music(newNotes, newVoices, newMeter, newKeyProgression, newChordProgression, Aligner.convertTime(lastTime, gt, this, alignment));
	}

	public static Music parseMusic(Scanner input) throws IOException {
		List<Note> notes = new ArrayList<Note>();
		List<Voice> voices = new ArrayList<Voice>();
		Meter meter = new Meter();
		ChordProgression chordProgression = new ChordProgression();
		KeyProgression keyProgression = new KeyProgression();
		int lastTime = Integer.MIN_VALUE;
		
		while (input.hasNextLine()) {
			String line = input.nextLine();
			
			// Check for matching prefixes
			if (line.startsWith("Note")) {
				Note note = Note.parseNote(line);
				notes.add(note);
				
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
				meter.setHierarchy(hierarchy);

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
