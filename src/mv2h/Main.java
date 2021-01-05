package mv2h;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import mv2h.objects.MV2H;
import mv2h.objects.Music;
import mv2h.objects.Note;
import mv2h.tools.Aligner;
import mv2h.tools.AlignmentNode;

/**
 * The <code>Main</code> class is the class called to evaluate anything with the MV2H package.
 *
 * @author Andrew McLeod
 */
public class Main {

	/**
	 * The difference in duration between two {@link mv2h.objects.Note}s for their value
	 * to be counted as a match.
	 * <br>
	 * Measured in milliseconds.
	 */
	public static int DURATION_DELTA = 100;

	/**
	 * The difference in onset time between two {@link mv2h.objects.Note}s for them to be
	 * counted as a match.
	 * <br>
	 * Measured in milliseconds.
	 */
	public static int ONSET_DELTA = 50;

	/**
	 * The difference in time between beginning and end times of a {@link mv2h.objects.meter.Grouping}
	 * for it to be counted as a match.
	 * <br>
	 * Measured in milliseconds.
	 */
	public static int GROUPING_EPSILON = 50;

	/**
	 * A flag representing if alignment should be performed. Defaults to <code>false</code>.
	 * Can be set to <code>true</code> with the <code>-a</code> or <code>-A</code> flags.
	 * <br>
	 * @see #PRINT_ALIGNMENT
	 */
	private static boolean PERFORM_ALIGNMENT = false;

	/**
	 * A flag representing if the alignment should be printed or not. Defaults to <code>false</code>.
	 * Can be set to <code>true</code> with the <code>-A</code> flag (which also sets
	 * {@link #PERFORM_ALIGNMENT} to true).
	 * <br>
	 * @see #PERFORM_ALIGNMENT
	 */
	private static boolean PRINT_ALIGNMENT = false;

	/**
	 * The penalty assigned for insertion and deletion errors when performing alignment.
	 * The default value of <code>1</code> leads to a reasonably fast, but not exhaustive
	 * search through alignments. Can be set with the <code>-p</code> flag.
	 */
	public static double NON_ALIGNMENT_PENALTY = 1.0;

	/**
	 * Use verbose printing.
	 */
	public static boolean VERBOSE = false;

	/**
	 * Run the program. There are 2 different modes. Each can be made verbose with <code>-v</code>.
	 * <br>
	 * 1. Perform an evaluation:
	 * <ul>
	 * <li><code>-g FILE</code> = The ground truth file.</li>
	 * <li><code>-t FILE</code> = The transcription file.</li>
	 * <li><code>-a</code> = Perform alignment.</li>
	 * <li><code>-A</code> = Perform and print alignment.</li>
	 * <li><code>-p DOUBLE</code> = Set the DTW insertion and deletion penalty.</li>
	 * </ul>
	 * <br>
	 * 2. Get the means and standard deviations of many outputs of this program
	 * (read from standard in): <code>-F</code>
	 *
	 * @param args The command line arguments, as described.
	 *
	 * @throws IOException If a File given with <code>-g</code> or <code>-t</code> cannot be
	 * read.
	 */
	public static void main(String[] args) throws IOException {
		File groundTruth = null;
		File transcription = null;

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
							return;

						case 'A':
							PRINT_ALIGNMENT = true;

						case 'a':
							DURATION_DELTA = 20;
							ONSET_DELTA = 0;
							GROUPING_EPSILON = 20;
							PERFORM_ALIGNMENT = true;
							break;

						case 'p':
							i++;
							if (args.length <= i) {
								argumentError("No non-alignment penalty given with -p.");
							}
							try {
								NON_ALIGNMENT_PENALTY = Double.parseDouble(args[i]);
							} catch (NumberFormatException e) {
								argumentError("Non-alignment penalty must be a decimal value. Given: " + args[i]);
							}
							break;

						// Evaluate!
						case 'g':
							i++;
							if (args.length <= i) {
								argumentError("No ground truth file given with -g.");
							}
							if (groundTruth != null) {
								argumentError("-g FILE can only be used once.");
							}
							groundTruth = new File(args[i]);
							if (!groundTruth.exists()) {
								argumentError("Ground truth file " + groundTruth + " does not exist.");
							}
							break;

						case 't':
							i++;
							if (args.length <= i) {
								argumentError("No transcription file given with -t.");
							}
							if (transcription != null) {
								argumentError("-t FILE can only be used once.");
							}
							transcription = new File(args[i]);
							if (!transcription.exists()) {
								argumentError("Transcription file " + transcription + " does not exist.");
							}
							break;

						case 'v':
							VERBOSE = true;
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

		if (groundTruth != null && transcription != null) {
			evaluateGroundTruth(groundTruth, transcription);
		} else {
			argumentError("Must give either -F, or both -g FILE and -t FILE.");
		}
	}

	/**
	 * Evaluate the given transcription against the given ground truth file.
	 * Prints the result to std out.
	 *
	 * @param groundTruthFile The ground truth.
	 * @param transcriptionFile The transcription.
	 *
	 * @throws IOException If one of the Files could not be read.
	 */
	public static void evaluateGroundTruth(File groundTruthFile, File transcriptionFile) throws IOException {
		Music groundTruth = Music.parseMusic(new Scanner(groundTruthFile));
		Music transcription = Music.parseMusic(new Scanner(transcriptionFile));

		// Get scores
		if (PERFORM_ALIGNMENT) {

			// Choose the best possible alignment out of all potential alignments.
			MV2H best = new MV2H(0, 0, 0, 0, 0);
			List<Integer> bestAlignment = new ArrayList<Integer>();

			List<AlignmentNode> alignmentNodes = Aligner.getPossibleAlignments(groundTruth, transcription);
			long total = 0;
			for (AlignmentNode alignmentNode : alignmentNodes) {
				total += alignmentNode.count;
			}

			long i = 0;
			String lineEnding = VERBOSE ? "\n" : "\r";
			for (AlignmentNode alignmentNode : alignmentNodes) {
				for (int alignmentIndex = 0; alignmentIndex < alignmentNode.count; alignmentIndex++) {
					System.out.print("Evaluating alignment " + (++i) + " / " + total + lineEnding);

					List<Integer> alignment = alignmentNode.getAlignment(alignmentIndex);

					MV2H candidate = groundTruth.evaluateTranscription(transcription.align(groundTruth, alignment));

					if (VERBOSE) {
						if (PRINT_ALIGNMENT) {
							System.out.println(getAlignmentString(groundTruth, transcription, alignment));
						}
						System.out.println(candidate);
					}

					if (candidate.compareTo(best) > 0) {
						best = candidate;
						bestAlignment = alignment;
					}
				}
			}
			System.out.println();

			if (PRINT_ALIGNMENT) {
				System.out.println("BEST ALIGNMENT         ");
				System.out.println("==============");

				System.out.println(getAlignmentString(groundTruth, transcription, bestAlignment));
				System.out.println();
			}

			if (VERBOSE || PRINT_ALIGNMENT) {
				System.out.println("BEST MV2H");
				System.out.println("=========");
			}

			System.out.println(best);

		} else {
			// No alignment
			System.out.println(groundTruth.evaluateTranscription(transcription));
		}
	}

	/**
	 * Generate and return a verbose alignment string for the given alignment.
	 *
	 * @param groundTruth The ground truth piece that is aligned to.
	 * @param transcription The transcription that has been aligned to the ground truth.
	 * @param alignmentToPrint The alignment List to convert to a printable String.
	 * @return The String to print for the given alignment.
	 */
	private static String getAlignmentString(Music groundTruth, Music transcription, List<Integer> alignmentToPrint) {
		StringBuilder sb = new StringBuilder("Aligned notes (transcribed -> ground truth):\n");

		List<List<Note>> nonAlignedNotes = new ArrayList<List<Note>>();
		for (int noteIndex = 0; noteIndex < transcription.getNoteLists().size(); noteIndex++) {
			int alignment = alignmentToPrint.indexOf(noteIndex);

			if (alignment == -1) {
				nonAlignedNotes.add(transcription.getNoteLists().get(noteIndex));
			} else {
				sb.append(transcription.getNoteLists().get(noteIndex) + " -> " +
						  (alignment == -1 ? "nothing." : groundTruth.getNoteLists().get(alignment)));
				sb.append('\n');
			}
		}

		sb.append("\nNon-aligned transcription notes:");
		for (List<Note> notes : nonAlignedNotes) {
			sb.append('\n');
			sb.append(notes);
		}

		return sb.toString();
	}

	/**
	 * Calculate and print mean and standard deviation of Multi-pitch, Voice, Meter, Value, Harmony, and MV2H
	 * scores as produced by this program, read from std in.
	 */
	private static void checkFull() {
		// Initialize counters
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

		// Parse std in
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

		// Calculate means and standard deviations
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

		// Print
		System.out.println("Multi-pitch: mean=" + multiPitchMean + " stdev=" + Math.sqrt(multiPitchVariance));
		System.out.println("Voice: mean=" + voiceMean + " stdev=" + Math.sqrt(voiceVariance));
		System.out.println("Meter: mean=" + meterMean + " stdev=" + Math.sqrt(meterVariance));
		System.out.println("Value: mean=" + valueMean + " stdev=" + Math.sqrt(valueVariance));
		System.out.println("Harmony: mean=" + harmonyMean + " stdev=" + Math.sqrt(harmonyVariance));
		System.out.println("MV2H: mean=" + mv2hMean + " stdev=" + Math.sqrt(mv2hVariance));
	}

	/**
	 * Calculate the F-measure given counts of TP, FP, and FN.
	 *
	 * @param truePositives The number of true positives.
	 * @param falsePositives The number of false positives.
	 * @param falseNegatives The number of false negatives.
	 *
	 * @return The F-measure of the given counts, or 0 if the result is otherwise NaN.
	 */
	public static double getF1(double truePositives, double falsePositives, double falseNegatives) {
		double precision = truePositives / (truePositives + falsePositives);
		double recall = truePositives / (truePositives + falseNegatives);

		double f1 = 2.0 * recall * precision / (recall + precision);
		return Double.isNaN(f1) ? 0.0 : f1;
	}

	/**
	 * Some argument error occurred. Print the given message and the usage instructions to std err
	 * and exit.
	 *
	 * @param message The message to print to std err.
	 */
	private static void argumentError(String message) {
		StringBuilder sb = new StringBuilder(message).append('\n');

		sb.append("Usage: Main ARGS\n");
		sb.append("ARGS:\n");

		sb.append("-a = Perform DTW alignment to evaluate non-aligned transcriptions.\n");
		sb.append("-A = Perform and print the DTW alignment.\n");

		sb.append("-g FILE = Use the given FILE as the ground truth (defaults to std in).\n");
		sb.append("-t FILE = Use the given FILE as the transcription (defaults to std in).\n");
		sb.append("Either -g or -t (or both) must be given to evaluate, since both cannot be read from std in.\n\n");

		sb.append("-p DOUBLE = Use the given value as the insertion and deletion penalty for alignment.\n");
		sb.append("-v = Use verbose printing. With -a, this will the evaluation score of every possible alignment. " +
		          "With -A, this will also print each alignment.\n\n");

		sb.append("-F = Combine the scores from std in (from this program's output) into final");
		sb.append(" global mean and standard deviation distributions for each score.\n");

		System.err.println(sb);
		System.exit(1);
	}
}
