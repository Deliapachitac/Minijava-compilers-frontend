all: compile

compile:
	java -jar lib/jtb133di.jar -te lib/minijava.jj
	java -jar lib/javacc5.jar lib/minijava-jtb.jj


clean:
	rm -rf *.class syntaxtree visitor lib/minijava-jtb.jj *~
	rm JavaCharStream.java MiniJavaParser.java MiniJavaParserTokenManager.java MiniJavaParserConstants.java
	rm Token.java TokenMgrError.java
	rm ParseException.java