SHELL = bash

PACKAGE = enc

SRC = $(wildcard $(PACKAGE)/*.java)

default: $(SRC)
	javac -d ../classes -g $(SRC)

clean:
	$(RM) *.class *~ OUT
