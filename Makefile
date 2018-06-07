CC = g++
CFLAGS = -g -Wall -std=c++14
PYTHON = python3

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

test: Target/Test/main.o Target/Test/test_util.o
	$(CC) $(CFLAGS) -o Target/SkiffTest Target/Test/*.o Target/Core/*.o
	Target/SkiffTest

Target/Test/main.o: Test/main.cpp Test/Tests/parse_test.cpp Test/Tests/util_test.cpp Test/Tests/execution_test.cpp
	$(CC) $(CFLAGS) -c -o Target/Test/main.o Test/main.cpp

Test/main.cpp: Test/build_test.py Test/Tests/parse_test.cpp Test/Tests/util_test.cpp Test/Tests/execution_test.cpp
	$(PYTHON) Test/build_test.py

Target/Test/test_util.o: Test/test_util.cpp
	$(CC) $(CFLAGS) -c -o Target/Test/test_util.o Test/test_util.cpp

clean:
	rm Target/*.o || true
	rm Target/Core/*.o || true
	rm Target/Test/*.o || true
	rm Target/SkiffTest || true
	rm Target/Skiff || true
	rm Test/main.cpp || true

lines:
	git ls-files | xargs wc -l