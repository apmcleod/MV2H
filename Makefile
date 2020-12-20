all:
	mkdir -p bin
	javac -d bin -cp src src/mv2h/*.java
	javac -d bin -cp src src/mv2h/*/*.java
