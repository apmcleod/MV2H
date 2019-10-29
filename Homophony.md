This file describes the new homophonic and polyphonic voice separation evaluation support added in
[v2.1](https://github.com/apmcleod/MV2H/releases/tag/v2.1)

The process described is performed after removing all notes not matched in multi-pitch detection, as described
in the original paper.

Within each voice, notes are grouped into clusters containing those notes with equal value (quantized) onset
and value (quantized) offset times. From each of these clusters, links are added to:
 - All of the clusters in that voice onset time is equal to the that cluster's offset time.
 - If no links were added, add links to all of the clusters with the minimum onset time > that cluster's
 offset time.

During evaluation, these links are each treated as links between every pair of notes across the two clusters.
However, these links are weighted so that each cluster's link is of relatively equal importance.

Specifically, after calculating TP, FP, and FN counts for the links out of a given transcribed note, these
counts are all divided by the product of:
 1. The arithmetic mean of the number of links out of that note in the transcription and the ground truth.
 2. The arithmetic mean of the size of the cluster of that note in the transcription and the ground truth.
 
The first term normalizes to weight each note equally, no matter the number of connections that follow it. 
The second term normalizes to weight each cluster equally, no matter its size.
