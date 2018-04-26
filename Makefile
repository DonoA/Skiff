CC = g++
CFLAGS = -g -Wall -std=c++14

EXT = cpp

MAIN = SuperCC/SuperCC.cpp

TEST = Test/unittest1.cpp

default: build

build: builtin.o modes.o parsers.o statement.o types.o utils.o main.o
	$(CC) $(CFLAGS) -o Target/Skiff Target/main.o Target/Core/*.o

builtin.o: Core/builtin.cpp Core/builtin.h
	$(CC) $(CFLAGS) -c -o Target/Core/builtin.o Core/builtin.cpp

modes.o: Core/modes.cpp Core/modes.h
	$(CC) $(CFLAGS) -c -o Target/Core/modes.o Core/modes.cpp

parsers.o: Core/parsers.cpp Core/parsers.h
	$(CC) $(CFLAGS) -c -o Target/Core/parsers.o Core/parsers.cpp

statement.o: Core/statement.cpp Core/statement.h
	$(CC) $(CFLAGS) -c -o Target/Core/statement.o Core/statement.cpp

types.o: Core/types.cpp Core/types.h
	$(CC) $(CFLAGS) -c -o Target/Core/types.o Core/types.cpp

utils.o: Core/utils.cpp Core/utils.h
	$(CC) $(CFLAGS) -c -o Target/Core/utils.o Core/utils.cpp

main.o: Skiff/main.cpp
	$(CC) $(CFLAGS) -c -o Target/main.o Skiff/main.cpp
