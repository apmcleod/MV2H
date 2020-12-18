#!/usr/bin/env bash

if [ "$#" -ne 2 ]; then
    echo "USAGE: evaluate.bash ref.xml transcription.xml"
    exit 1
fi

musescore3 -o $1.mid $1
musescore3 -o $2.mid $2

java -cp bin mv2h.tools.Converter -i $1.mid -o $1.mid.txt
java -cp bin mv2h.tools.Converter -i $2.mid -o $2.mid.txt
rm $1.mid $2.mid

java -cp bin mv2h.Main -g $1.mid.txt -t $2.mid.txt -a
rm $1.mid.txt $2.mid.txt
