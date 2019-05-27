# MV2H

This is the code and data from my 2018 ISMIR paper. If you use it, please cite it:

```
@inproceedings{McLeod:18a,
  title={Evaluating automatic polyphonic music transcription},
  author={McLeod, Andrew and Steedman, Mark},
  booktitle={{ISMIR}},
  year={2018},
  pages={42--49}
}
```

Note that while the paper and the code on the master branch only works on time-aligned input and ground truth pairs, the code in the regress branch uses Dynamic Time Warping to automatically align non-aligned input and ground truth. For now, please still cite the original publication if you use this new version.

## Project Overview
The goal of this project is to create an automatic, joint, quantitative metric for complete transcription of polyphonic music. This branch includes code to evaluate a non-aligned score and ground truth.

### Using with Non-aligned Data
The program will perform alignment itself if used with the `-a` flag. Note that this sets the time window for onset, offset, and groupings to 0ms (since they should be aligned). For the time information in the input text files (particularly the ground truth), you should use some reasonable tempo, like 100 BMP.

The dataset directory contains files generated when evaluating the transcriptions from the MusicXML files of the following paper:

```
Andrea Cogliati, Zhiyao Duan, A metric for Music Notation Transcription Accuracy, Proc. of International Society for Music Information Retrieval Conference (ISMIR), Suzhou, China, Oct 2017.
```

For details on this directory, or how to score MusicXML files with metric, see the MusicXML section below.

## Installing
The java files can all be compiled into class files in a bin directory using the Makefile
included with the project with the following command: `$ make`.

## Running
Run the program one of the following ways:
* To evaluate a transcription: `$ java -cp bin mv2h.Main -g FILE -t FILE`
* To combine a number of evaluations: `$ java -cp bin mv2h.Main -F`

ARGS:
 * `-g FILE` = Use the given FILE as ground truth.
 * `-t FILE` = Use the given FILE as the transcription.
 * `-a` = Perform alignment between the ground truth and the transcription.
 * `-F` = Combine many evaluations from standard in, where standard in contains many outputs created by using -g and -t.

### Examples
The examples directory contains two example transcriptions of an ground truth. To perform evaluation, run the following commands and you should get the results shown:

 * `$ java -cp bin mv2h.Main -g examples/GroundTruth.txt -t examples/Transcription1.txt`  
Multi-pitch: 0.9302325581395349  
Voice: 0.8125  
Meter: 0.7368421052631577  
Value: 0.9642857142857143  
Harmony: 1.0  
MV2H: 0.8887720755376813  
 
 * `$ java -cp bin mv2h.Main -g examples/GroundTruth.txt -t examples/Transcription2.txt`  
Multi-pitch: 0.7727272727272727  
Voice: 1.0  
Meter: 1.0  
Value: 1.0  
Harmony: 0.5  
MV2H: 0.8545454545454545  
 
 * `$ java -cp bin mv2h.Main -F <examples/FullOut.txt`  
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
 
 
 * `Hierarchy bpb,sbpb tpsb a=al`  
This represents the metrical hierarchy, where bpb, sbpb, tpsb, and al are all integers representing the beats per bar, sub beats per beat, tatums per sub beat, and anacrusis length (in tatums) respectively.
 
 * `Key tonic maj/min [time]`  
This is a string representing the key signature, where tonic is an integer between 0 and 11 inclusive, representing the tonic note (with C=0), and maj/min is either the string "maj" or "min". The time (optional integer, milliseconds) is used for key changes, and defaults to 0 if not given.
 
 * `Chord time chord`  
Representing a chord, where time is the start time of the chord, in milliseconds, and chord is any string representing the chord.

## MusicXML
To score MusicXML files:
* First, use the included C++ converter with `MusicXMLParser/MusicXMLToFmt1x in.xml out.txt` to convert a MusicXML file into a text-based format.
* Next, use `java -cp bin mv2h.tools.Converter <out.txt >converted.txt` to convert that text format into the text format used by mv2h (reads and writes with std in and out). The tempo defaults to 500 ms per MusicXML beat.
* Use the standard mv2h as above with the `-a` flag.


The dataset directory contains files generated using the above process on the dataset from the paper:
```
Andrea Cogliati, Zhiyao Duan, A metric for Music Notation Transcription Accuracy, Proc. of International Society for Music Information Retrieval Conference (ISMIR), Suzhou, China, Oct 2017.
```
The original music XML files are available here: https://github.com/AndreaCogliati/MetricForScoreSimilarity
* dataset/parsed-xml contains the outputs of the C++ converter.
* dataset/converted contains the outputs of my java Converter tool.
* dataset/outs contains the resulting evaluation scores for each transcription.
