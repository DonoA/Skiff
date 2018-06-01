#include "test_util.h"
#include "util_test.cpp"
#include "parse_test.cpp"

#include <vector>
#include <string>
#include <iostream>

using std::vector;
using std::string;

vector<string> assert_failures;
bool last_failed;
size_t total_test_count = 0;
size_t tests_failed = 0;
string current_test;

int main()
{
    std::cout << "Running Skiff Tests:" << std::endl;
    std::cout << "========" << std::endl;

    std::cout << "Utils:" << std::endl << "\t";
    CALL_TEST_METHOD(Utils, Padding);
    CALL_TEST_METHOD(Utils, BracedSplit);
    std::cout << std::endl;
    
    std::cout << "Parsing:" << std::endl << "\t";
    CALL_TEST_METHOD(Parsing, Declaration);
    CALL_TEST_METHOD(Parsing, Assignment);
    CALL_TEST_METHOD(Parsing, AssignmentAndDeclaration);
    CALL_TEST_METHOD(Parsing, FunctionCall);
    CALL_TEST_METHOD(Parsing, FunctionDef);
    CALL_TEST_METHOD(Parsing, ClassDef);
    CALL_TEST_METHOD(Parsing, StructDef);
    CALL_TEST_METHOD(Parsing, InstanceClass);
    CALL_TEST_METHOD(Parsing, ReturnStatement);
    CALL_TEST_METHOD(Parsing, Literals);
    CALL_TEST_METHOD(Parsing, Bitwise);
    CALL_TEST_METHOD(Parsing, BooleanOperations);
    CALL_TEST_METHOD(Parsing, Comparison);
    CALL_TEST_METHOD(Parsing, BasicMath);
    CALL_TEST_METHOD(Parsing, ListOperations);
    CALL_TEST_METHOD(Parsing, IfStatement);
    CALL_TEST_METHOD(Parsing, WhileStatement);
    CALL_TEST_METHOD(Parsing, ForStatement);
    CALL_TEST_METHOD(Parsing, FlowControls);
    CALL_TEST_METHOD(Parsing, ImportStatement);
    CALL_TEST_METHOD(Parsing, DeclarationModifiers);
    CALL_TEST_METHOD(Parsing, Annotation);
    CALL_TEST_METHOD(Parsing, SwitchMatchHeading);
    std::cout << std::endl;

    for(string s : assert_failures)
    {
        std::cout << s << std::endl;
    }

    std::cout << total_test_count << " tests run" << std::endl;
    std::cout << total_test_count - tests_failed << " tests passed" << std::endl;
    std::cout << tests_failed << " tests failed" << std::endl;
}