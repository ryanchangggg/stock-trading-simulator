JAVA_HOME := /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
MVN := JAVA_HOME=$(JAVA_HOME) mvn

.PHONY: build test run clean package

build:
	$(MVN) compile

test:
	$(MVN) test

run:
	$(MVN) javafx:run

clean:
	$(MVN) clean

package:
	$(MVN) package
