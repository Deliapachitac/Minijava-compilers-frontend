all: compile

compile:
	java -jar ../jtb133di.jar tern.jj
	java -jar ../javacc5.jar tern-jtb.jj
	javac Main.java

clean:
	rm -f *.class *~
