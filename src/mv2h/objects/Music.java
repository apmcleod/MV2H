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
		this.voices = voices;
		this.meter = meter;
		this.keyProgression = keyProgression;
		this.chordProgression = progression;
		this.lastTime = lastTime;
	}
	
	public String evaluateTranscription(Music transcription) {
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
		double mv2h = (multiPitchF1 + voiceF1 + meterF1 + valueScore + harmonyScore) / 5;
		
		
		// Create return string
		StringBuilder sb = new StringBuilder();
		sb.append("Multi-pitch: " + multiPitchF1 + "\n");
		sb.append("Voice: " + voiceF1 + "\n");
		sb.append("Meter: " + meterF1 + "\n");
		sb.append("Value: " + valueScore + "\n");
		sb.append("Harmony: " + harmonyScore + "\n");
		sb.append("MV2H: " + mv2h);
		
		return sb.toString();
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
