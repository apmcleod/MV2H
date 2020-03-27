#!/usr/bin/env bash

if [ "$#" -ne 2 ]; then
    echo "USAGE: evaluate.bash ref.xml transcription.xml"
    exit 1
fi

./MusicXMLParser/MusicXMLToFmt1x $1 $1.txt
java -cp bin mv2h.tools.Converter -x <$1.txt >$1.conv.txt
rm $1.txt

./MusicXMLParser/MusicXMLToFmt1x $2 $2.txt
java -cp bin mv2h.tools.Converter -x <$2.txt >$2.conv.txt
rm $2.txt

java -cp bin mv2h.Main -g $1.conv.txt -t $2.conv.txt -a
rm $1.conv.txt $2.conv.txt
