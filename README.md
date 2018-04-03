# MV2H

## Project Overview
The goal of this project is to create an automatic, joint, quantitative metric for complete transcription of polyphonic music.

## Installing
The java files can all be compiled into class files in a bin directory using the Makefile
included with the project with the following command: `$ make`.

## Running
Run the program as follows:
`$ java -cp bin mv2h.Main ARGS`

ARGS:
 * `-E FILE` = Perform evaluation with the given FILE as ground truth. The transcription will be read from standard in.
 * `-F` = Perform a full meta-evaluation from standard in, where standard in may contain many results created by using -E.

### Examples
The examples directory contains two example transcriptions of an ground truth. To perform evaluation, run the following commands and you should get the results shown:

 * `$ java -cp bin -E examples/GroundTruth.txt <examples/Transcription1.txt`  
Multi-pitch: 0.9302325581395349  
Voice: 0.8125  
Meter: 0.7368421052631577  
Value: 0.9642857142857143  
Harmony: 1.0  
MV2H: 0.8887720755376813  
 
 * `$ java -cp bin -E examples/GroundTruth.txt <examples/Transcription2.txt`  
Multi-pitch: 0.7727272727272727  
Voice: 1.0  
Meter: 1.0  
Value: 1.0  
Harmony: 0.5  
MV2H: 0.8545454545454545  
 
 * `$ java -cp bin -F <examples/FullOut.txt`  
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
 
 * `Key tonic maj/min`  
This is a string representing the key signature, where tonic is an integer between 0 and 11 inclusive, representing the tonic note (with C=0), and maj/min is either the string "maj" or "min".
 
 * `Chord time chord`  
Representing a chord, where time is the start time of the chord, in milliseconds, and chord is any string representing the chord.
