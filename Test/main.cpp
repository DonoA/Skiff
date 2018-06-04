
#include "test_util.h"
#include "Tests/util_test.cpp"
#include "Tests/parse_test.cpp"

#include <vector>
#include <string>
#include <iostream>

using std::vector;
using std::string;

vector<string> assert_failures;
bool last_failed;
size_t tests_passed;
size_t tests_failed;
string current_test;

size_t total_run = 0;
int main()
{
    std::cout << "Running Skiff Tests:" << std::endl;
    std::cout << "========" << std::endl;

    tests_passed = 0;
    tests_failed = 0;
    std::cout << "Utils:" << std::endl << "	";
    run_test(Test::Utils_tests::Padding_test, "Padding");
    run_test(Test::Utils_tests::BracedSplit_test, "BracedSplit");
    std::cout << "      " << tests_passed + tests_failed << " run, " << tests_failed << " failures" << std::endl;
    total_run += tests_passed + tests_failed;
    
    tests_passed = 0;
    tests_failed = 0;
    std::cout << "Parsing:" << std::endl << "	";
    run_test(Test::Parsing_tests::Declaration_test, "Declaration");
    run_test(Test::Parsing_tests::Assignment_test, "Assignment");
    run_test(Test::Parsing_tests::AssignmentAndDeclaration_test, "AssignmentAndDeclaration");
    run_test(Test::Parsing_tests::FunctionCall_test, "FunctionCall");
    run_test(Test::Parsing_tests::FunctionDef_test, "FunctionDef");
    run_test(Test::Parsing_tests::ClassDef_test, "ClassDef");
    run_test(Test::Parsing_tests::StructDef_test, "StructDef");
    run_test(Test::Parsing_tests::InstanceClass_test, "InstanceClass");
    run_test(Test::Parsing_tests::ReturnStatement_test, "ReturnStatement");
    run_test(Test::Parsing_tests::Literals_test, "Literals");
    run_test(Test::Parsing_tests::Bitwise_test, "Bitwise");
    run_test(Test::Parsing_tests::BooleanOperations_test, "BooleanOperations");
    run_test(Test::Parsing_tests::Comparison_test, "Comparison");
    run_test(Test::Parsing_tests::BasicMath_test, "BasicMath");
    run_test(Test::Parsing_tests::ListOperations_test, "ListOperations");
    run_test(Test::Parsing_tests::IfStatement_test, "IfStatement");
    run_test(Test::Parsing_tests::WhileStatement_test, "WhileStatement");
    run_test(Test::Parsing_tests::ForStatement_test, "ForStatement");
    run_test(Test::Parsing_tests::FlowControls_test, "FlowControls");
    run_test(Test::Parsing_tests::ImportStatement_test, "ImportStatement");
    run_test(Test::Parsing_tests::DeclarationModifiers_test, "DeclarationModifiers");
    run_test(Test::Parsing_tests::Annotation_test, "Annotation");
    run_test(Test::Parsing_tests::SwitchMatchHeading_test, "SwitchMatchHeading");
    std::cout << "      " << tests_passed + tests_failed << " run, " << tests_failed << " failures" << std::endl;
    total_run += tests_passed + tests_failed;
    
    std::cout << std::endl;

    for(string s : assert_failures)
    {
        std::cout << s << std::endl << std::endl;
    }

    std::cout << total_run << " tests run" << std::endl;
}