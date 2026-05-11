all: compile

compile:
	java -jar ../jtb133di.jar calc.jj
	java -jar ../javacc5.jar calc-jtb.jj
	javac Main.java

clean:
	rm -f *.class *~
