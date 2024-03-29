# MV2H

This is the code for the MV2H metric, originally proposed in my 2018 ISMIR paper.

If you use the metric, please cite it:

```
@inproceedings{McLeod:18a,
  title={Evaluating automatic polyphonic music transcription},
  author={McLeod, Andrew and Steedman, Mark},
  booktitle={International Society for Music Information Retrieval Conference (ISMIR)},
  year={2018},
  pages={42--49}
}
```

To compile the code, simply run `make` in the base directory.

## Usage
* The standard text-based file format is described [here](#File-Format).
* Converters for use with other file formats (such as MusicXML and MIDI) are described [here](#Other-File-Formats).

### Non-aligned Data
Use the `-a` flag to evaluate a non-time-aligned transcription:
* `java -cp bin mv2h.Main -g gt.txt -t transcription.txt -a|-A [-p DOUBLE] [-v]`

* `-a` or `-A`: Perform normal (`-a`) or verbose (`-A`, will also print out note-by-note alignment details) alignment.

* `-p DOUBLE` (new in v2.2): Set the DTW insertion and deletion penalties to the given `DOUBLE` (default 1.0).
Higher values will be faster, but force the DTW alignment to align as many notes as possible, even if they don't match well.
Lower values will be slower, but make it only rarely align notes that don't match exactly.
The default of 1.0 should be good in most cases. Values of 0.5 and 1.0 are sort of inflection points
(you will find large changes in the number of potential alignments around these values).
_NOTE: You should use the same value throughout your whole evaluation for a fair comparison._

* `-v`: Use verbose printing. With `-a`, this will print the evaluation score for each alignment.
With `-A`, this will also print each alignment itself.

### Aligned Data
To evaluate a time-aligned transcription and ground truth:
* `java -cp bin mv2h.Main -g gt.txt -t transcription.txt`

### Other File Formats
#### MusicXML
There is now a bash script that will perform this evaluation in one command (if you have musescore3): `evaluate_xml.bash gt.xml transcription.xml`

You can also perform the process manually by first converting the MusicXML files into MIDI, and then following the [instructions for MIDI files](#MIDI).
 - The recommended way to convert MusicXML to MIDI is to use Musescore3:
`musescore3 -o file.mid file.xml`
 - Other methods may also work, but not all will handle anacrusis (pick-up) measures correctly (`music21`, for example, did not when I tested it and will require manual setting during the MIDI conversion with `-a INT`).
 - MusicXML files without a time signature are treated as 4/4.

#### MIDI
1. Convert a MIDI file into the MV2H format:
`java -cp bin mv2h.tools.Converter -m -i gt.mid -o gt_converted.txt`
`-a INT` can be used to set the anacrusis (pick-up bar) length to INT sub beats, in case it is not aligned correctly in the MIDI.
Different parsed voices can be generated using `--channel` or `--track`. Default uses both.

2. Evaluate with alignment using the `-a` flag:
`java -cp bin mv2h.Main -g gt_converted.txt -t transcription_converted.txt -a`

Chord symbols will not be parsed.

### Averaging Multiple Evaluations
To get the averages of many MV2H evaluations:
1. Concatenate the evaluations into a single txt file: `cat res*.txt >all_results.txt`
2. Use the `-F` flag: `java -cp bin mv2h.Main -F <all_results.txt`


## Examples
The examples directory contains two example transcriptions of an ground truth. To perform evaluation, run the following commands and you should get the results shown:

 * `java -cp bin mv2h.Main -g examples/GroundTruth.txt -t examples/Transcription1.txt`  
Multi-pitch: 0.9302325581395349  
Voice: 0.8125  
Meter: 0.7368421052631577  
Value: 0.9642857142857143  
Harmony: 1.0  
MV2H: 0.8887720755376813

 * `java -cp bin mv2h.Main -g examples/GroundTruth.txt -t examples/Transcription2.txt`  
Multi-pitch: 0.7727272727272727  
Voice: 1.0  
Meter: 1.0  
Value: 1.0  
Harmony: 0.5  
MV2H: 0.8545454545454545

 * `java -cp bin mv2h.Main -F <examples/FullOut.txt`  
Multi-pitch: mean=0.8514799154334038 stdev=0.0787526427061301  
Voice: mean=0.90625 stdev=0.09375  
Meter: mean=0.8684210526315789 stdev=0.13157894736842105  
Value: mean=0.9821428571428572 stdev=0.017857142857137718  
Harmony: mean=0.75 stdev=0.25  
MV2H: mean=0.8716587650415679 stdev=0.017113310496113164  


## File Format
The file format for reading a ground truth or transcription is text-based, and each line can be one of the following, and ordering of the lines does not matter:

 * `Note pitch on onVal offVal voice`
This represents a note. Pitch, on, onVal, offVal, and voice are all integers, representing the pitch, onset time, onset time quantised to the meter, offset time quantised to the meter, and voice id. All times are in milliseconds.

 * `Tatum time`
This represents a tatum pulse at the given time (an integer, in milliseconds).

 * `Hierarchy bpb,sbpb tpsb a=al [time]`
This represents the metrical hierarchy, where bpb, sbpb, tpsb, and al are all integers representing the beats per bar, sub beats per beat, tatums per sub beat, and anacrusis length (in tatums) respectively. The time is in milliseconds, and assumed to be 0 if not given.
Beginning a new hierarchy on a non-downbeat, while not recommended, is supported. The previous downbeat, and start to the most recent beat and sub beat are saved, and the tatum position is updated as in the new bar. Thus, 1 beat of a 2/4 bar, followed by 3 beats of a 4/4 bar with a 3 beat anacrusis, is equivalent to a single 4/4 bar.

 * `Key tonic maj/min [time]`
This is a string representing the key signature, where tonic is an integer between 0 and 11 inclusive, representing the tonic note (with C=0), and maj/min is either the string "maj" or "min". The time (optional integer, milliseconds) is used for key changes, and defaults to 0 if not given.

 * `Chord time chord`
Representing a chord, where time is the start time of the chord, in milliseconds, and chord is any string representing the chord.

For any duplicate times, only the last given chord, key, or hierarchy are saved. Duplicate tatums are ignored.


## Python Version
Big thanks to @lucasmpaim for his work porting MV2H to python [here](https://github.com/lucasmpaim/pyMV2H).

The python version does run significantly slower at the moment, particularly on long pieces with inaccuracte transcriptions.

NOTE: The java version of MV2H should always be considered the canonical, "correct" version, and should be used for final evaluation of a system. However, I have no reason to believe that the python code has any problems with it, and we are working on writing tests to confirm this (see [this issue](https://github.com/apmcleod/MV2H/issues/10)).

## Version History
- [v2.2](https://github.com/apmcleod/MV2H/releases/tag/v2.2) added the ability to tune the insertion/deletion penalty during DTW alignment, fixed a bug with time signatures with small denominators (e.g. 2 or 1), and sped up the DTW process significantly.
- [v2.1](https://github.com/apmcleod/MV2H/releases/tag/v2.1) added support for evaluating homophonic and polyphonic voice separation (see [Homophony.md](https://github.com/apmcleod/MV2H/blob/master/Homophony.md)).
- [v2.0](https://github.com/apmcleod/MV2H/releases/tag/v2.0) added support for non-time-aligned transcriptions (i.e. musical score to musical score evaluation), as detailed in the technical report on [arXiv](https://arxiv.org/abs/1906.00566) and presented in the ISMIR 2019 LBD session.
- [v1.0](https://github.com/apmcleod/MV2H/releases/tag/v1.0): the original version of the code, as it was for the ISMIR 2018 submission.
