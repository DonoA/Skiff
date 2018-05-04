CC = g++
CFLAGS = -g -Wall -std=c++14

default: build

build: Target/Core/builtin.o Target/Core/modes.o Target/Core/parsers.o Target/Core/parsed_statement.o Target/Core/eval_statement.o Target/Core/types.o Target/Core/utils.o Target/main.o
	$(CC) $(CFLAGS) -o Target/Skiff Target/main.o Target/Core/*.o

Target/Core/builtin.o: Core/builtin.cpp Core/builtin.h
	$(CC) $(CFLAGS) -c -o Target/Core/builtin.o Core/builtin.cpp

Target/Core/modes.o: Core/modes.cpp Core/modes.h
	$(CC) $(CFLAGS) -c -o Target/Core/modes.o Core/modes.cpp

Target/Core/parsers.o: Core/parsers.cpp Core/parsers.h
	$(CC) $(CFLAGS) -c -o Target/Core/parsers.o Core/parsers.cpp

Target/Core/parsed_statement.o: Core/parsed_statement.cpp Core/statement.h
	$(CC) $(CFLAGS) -c -o Target/Core/parsed_statement.o Core/parsed_statement.cpp

Target/Core/eval_statement.o: Core/eval_statement.cpp  Core/statement.h
	$(CC) $(CFLAGS) -c -o Target/Core/eval_statement.o Core/eval_statement.cpp 

Target/Core/types.o: Core/types.cpp Core/types.h
	$(CC) $(CFLAGS) -c -o Target/Core/types.o Core/types.cpp

Target/Core/utils.o: Core/utils.cpp Core/utils.h
	$(CC) $(CFLAGS) -c -o Target/Core/utils.o Core/utils.cpp

Target/main.o: Skiff/main.cpp
	$(CC) $(CFLAGS) -c -o Target/main.o Skiff/main.cpp

clean:
	rm Target/*.o
	rm Target/Core/*.o 
	rm Target/Skiff

lines:
	git ls-files | xargs wc -l