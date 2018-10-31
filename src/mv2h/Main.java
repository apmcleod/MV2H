package mv2h;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import mv2h.objects.Music;

public class Main {

	public static void main(String[] args) throws IOException {
		File groundTruth = null;
		
		// No args given
		if (args.length == 0) {
			argumentError("No arguments given");
		}
		
		for (int i = 0; i < args.length; i++) {
			switch (args[i].charAt(0)) {
				// ARGS
				case '-':
					if (args[i].length() == 1) {
						argumentError("Unrecognized option: " + args[i]);
					}
					
					switch (args[i].charAt(1)) {
						// Check Full
						case 'F':
							checkFull();
							break;
						
						// Evaluate!
						case 'E':
							i++;
							if (args.length <= i) {
								argumentError("No ground truth file given for -E option.");
							}
							if (groundTruth != null) {
								argumentError("-E option can only be given once.");
							}
							groundTruth = new File(args[i]);
							if (!groundTruth.exists()) {
								argumentError("Ground truth file " + groundTruth + " does not exist.");
							}
							break;
							
						// Error
						default:
							argumentError("Unrecognized option: " + args[i]);
					}
					break;
					
				// Error
				default:
					argumentError("Unrecognized option: " + args[i]);
			}
		}
		
		if (groundTruth != null) {
			evaluateGroundTruth(groundTruth);
		}
	}
	
	/**
	 * Evaluate the transcription (from std in) with the given ground truth file.
	 * Prints the result to std out.
	 * 
	 * @param groundTruthFile The ground truth file.
	 * @throws IOException 
	 */
	public static void evaluateGroundTruth(File groundTruthFile) throws IOException {
		Music groundTruth = Music.parseMusic(new Scanner(groundTruthFile));
		Music transcription = Music.parseMusic(new Scanner(System.in));
		
		// Get scores
		System.out.println(groundTruth.evaluateTranscription(transcription));
	}
	
	/**
	 * Calculate and print mean and standard deviation of Multi-pitch, Voice, Meter, Value, Harmony, and MV2H
	 * scores as produced by Main -E, read from std in.
	 */
	private static void checkFull() {
		int multiPitchCount = 0;
		double multiPitchSum = 0.0;
		double multiPitchSumSquared = 0.0;
		
		int voiceCount = 0;
		double voiceSum = 0.0;
		double voiceSumSquared = 0.0;
		
		int meterCount = 0;
		double meterSum = 0.0;
		double meterSumSquared = 0.0;
		
		int valueCount = 0;
		double valueSum = 0.0;
		double valueSumSquared = 0.0;
		
		int harmonyCount = 0;
		double harmonySum = 0.0;
		double harmonySumSquared = 0.0;
		
		int mv2hCount = 0;
		double mv2hSum = 0.0;
		double mv2hSumSquared = 0.0;
		
		Scanner input = new Scanner(System.in);
		while (input.hasNextLine()) {
			String line = input.nextLine();
			
			int breakPoint = line.indexOf(": ");
			if (breakPoint == -1) {
				continue;
			}
			
			String prefix = line.substring(0, breakPoint);
			
			// Check for matching prefixes
			if (prefix.equalsIgnoreCase("Multi-pitch")) {
				double score = Double.parseDouble(line.substring(breakPoint + 2));
				multiPitchSum += score;
				multiPitchSumSquared += score * score;
				multiPitchCount++;
				
			} else if (prefix.equalsIgnoreCase("Voice")) {
				double score = Double.parseDouble(line.substring(breakPoint + 2));
				voiceSum += score;
				voiceSumSquared += score * score;
				voiceCount++;
				
			} else if (prefix.equalsIgnoreCase("Meter")) {
				double score = Double.parseDouble(line.substring(breakPoint + 2));
				meterSum += score;
				meterSumSquared += score * score;
				meterCount++;
				
			} else if (prefix.equalsIgnoreCase("Value")) {
				double score = Double.parseDouble(line.substring(breakPoint + 2));
				valueSum += score;
				valueSumSquared += score * score;
				valueCount++;
				
			} else if (prefix.equalsIgnoreCase("Harmony")) {
				double score = Double.parseDouble(line.substring(breakPoint + 2));
				harmonySum += score;
				harmonySumSquared += score * score;
				harmonyCount++;
				
			} else if (prefix.equalsIgnoreCase("MV2H")) {
				double score = Double.parseDouble(line.substring(breakPoint + 2));
				mv2hSum += score;
				mv2hSumSquared += score * score;
				mv2hCount++;
			}
		}
		input.close();
		
		double multiPitchMean = multiPitchSum / multiPitchCount;
		double multiPitchVariance = multiPitchSumSquared / multiPitchCount - multiPitchMean * multiPitchMean;
		
		double voiceMean = voiceSum / voiceCount;
		double voiceVariance = voiceSumSquared / voiceCount - voiceMean * voiceMean;
		
		double meterMean = meterSum / meterCount;
		double meterVariance = meterSumSquared / meterCount - meterMean * meterMean;
		
		double valueMean = valueSum / valueCount;
		double valueVariance = valueSumSquared / valueCount - valueMean * valueMean;
		
		double harmonyMean = harmonySum / harmonyCount;
		double harmonyVariance = harmonySumSquared / harmonyCount - harmonyMean * harmonyMean;
		
		double mv2hMean = mv2hSum / mv2hCount;
		double mv2hVariance = mv2hSumSquared / mv2hCount - mv2hMean * mv2hMean;
		
		System.out.println("Multi-pitch: mean=" + multiPitchMean + " stdev=" + Math.sqrt(multiPitchVariance));
		System.out.println("Voice: mean=" + voiceMean + " stdev=" + Math.sqrt(voiceVariance));
		System.out.println("Meter: mean=" + meterMean + " stdev=" + Math.sqrt(meterVariance));
		System.out.println("Value: mean=" + valueMean + " stdev=" + Math.sqrt(valueVariance));
		System.out.println("Harmony: mean=" + harmonyMean + " stdev=" + Math.sqrt(harmonyVariance));
		System.out.println("MV2H: mean=" + mv2hMean + " stdev=" + Math.sqrt(mv2hVariance));
	}
	
	public static double getF1(int truePositives, int falsePositives, int falseNegatives) {
		double precision = ((double) truePositives) / (truePositives + falsePositives);
		double recall = ((double) truePositives) / (truePositives + falseNegatives);
		
		double f1 = 2.0 * recall * precision / (recall + precision);
		return Double.isNaN(f1) ? 0.0 : f1;
	}
	
	/**
	 * Some argument error occurred. Print the message to std err and exit.
	 * 
	 * @param message The message to print to std err.
	 */
	private static void argumentError(String message) {
		StringBuilder sb = new StringBuilder(message).append('\n');
		
		sb.append("Usage: Main ARGS\n");
		sb.append("ARGS:\n");
		
		sb.append("-E FILE = Evaluate the transcription from std in with the given");
		sb.append(" ground truth FILE.\n");
		
		sb.append("-F = Combine the scores from std in (generated by -E) into final");
		sb.append(" global mean and standard deviation distributions for each score.\n");
		
		System.err.println(sb);
		System.exit(1);
	}
}