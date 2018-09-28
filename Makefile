CC = g++
CFLAGS = -g -Wall -std=c++14
PYTHON = python3

default: build

build: Target/Core/builtin.o Target/Core/evaluated_types.o Target/Core/modes.o Target/Core/parser.o Target/Core/statement_eval.o Target/Core/statement_parsed.o Target/Core/token.o Target/Core/utils.o Target/main.o
	$(CC) $(CFLAGS) -o Target/Skiff Target/main.o Target/Core/*.o

Target/Core/interpreter/builtin.o: Core/interpreter/builtin.cpp Core/interpreter/builtin.h
	$(CC) $(CFLAGS) -c -o $@ Core/interpreter/builtin.cpp

Target/Core/interpreter/evaluated_types.o: Core/interpreter/evaluated_types.cpp Core/interpreter/evaluated_types.h
	$(CC) $(CFLAGS) -c -o $@ Core/interpreter/evaluated_types.cpp

Target/Core/modes.o: Core/modes.cpp Core/modes.h
	$(CC) $(CFLAGS) -c -o $@ Core/modes.cpp

Target/Core/parser/parser.o: Core/parser/parser.cpp Core/parser/parser.h
	$(CC) $(CFLAGS) -c -o $@ Core/parser/parser.cpp

Target/Core/interpreter/statement_eval.o: Core/interpreter/statement_eval.cpp  Core/interpreter/statement.h
	$(CC) $(CFLAGS) -c -o $@ Core/interpreter/statement_eval.cpp

Target/Core/parser/statement_parsed.o: Core/parser/statement_parsed.cpp Core/parser/statement.h
	$(CC) $(CFLAGS) -c -o $@ Core/parser/statement_parsed.cpp

Target/Core/parser/token.o: Core/parser/token.cpp Core/parser/token.h
	$(CC) $(CFLAGS) -c -o $@ Core/parser/token.cpp

Target/Core/utils.o: Core/utils.cpp Core/utils.h
	$(CC) $(CFLAGS) -c -o $@ Core/utils.cpp

Target/main.o: Skiff/main.cpp
	$(CC) $(CFLAGS) -c -o Target/main.o Skiff/main.cpp

test: Target/Test/main.o Target/Test/test_util.o
	$(CC) $(CFLAGS) -o Target/SkiffTest Target/Test/*.o Target/Core/*.o
	Target/SkiffTest

Target/Test/main.o: Test/main.cpp Test/Tests/parse_test.cpp Test/Tests/util_test.cpp Test/Tests/execution_test.cpp Test/Tests/tokenizer_test.cpp
	$(CC) $(CFLAGS) -c -o Target/Test/main.o Test/main.cpp

Test/main.cpp: Test/build_test.py Test/Tests/parse_test.cpp Test/Tests/util_test.cpp Test/Tests/execution_test.cpp Test/Tests/tokenizer_test.cpp
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