import os, re

test_packs = {}
test_order = []

current_test_class = None

class_match = re.compile(r"TEST_CLASS\((\w+),\s*(\d+)\)")
method_match = re.compile(r"TEST_METHOD\((\w+)\)")

for file in os.listdir("Test/Tests"):
    f = open("Test/Tests/" + file, "r")
    for line in f.readlines():
        is_class = class_match.search(line)
        if is_class:
            current_test_class = is_class.groups()[0]
            test_ord = is_class.groups()[1]
            if current_test_class not in test_packs:
                test_packs[current_test_class] = []
                test_order.append((current_test_class, test_ord))
            continue
        is_method = method_match.search(line)
        if is_method:
            test_packs[current_test_class].append(is_method.groups()[0])
            continue
    f.close()

test_order.sort(key=lambda x: x[1])

main_cpp = open("Test/main.cpp", "w+")

main_cpp.write("""
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
""")

main_cpp.write("int main()\n{")

main_cpp.write("""
    std::cout << "Running Skiff Tests:" << std::endl;
    std::cout << "========" << std::endl;
""")

for key, ord_val in test_order:
    main_cpp.write("""
    tests_passed = 0;
    tests_failed = 0;
    std::cout << "{}:" << std::endl << "\t";""".format(key))
    for test in test_packs[key]:
        main_cpp.write("""
    run_test(Test::{}_tests::{}_test, \"{}\");""".format(key, test, test))
    main_cpp.write("""
    std::cout << "      " << tests_passed + tests_failed << " run, " << tests_failed << " failures" << std::endl;
    total_run += tests_passed + tests_failed;
    """.format(key))

main_cpp.write("""
    std::cout << std::endl;

    for(string s : assert_failures)
    {
        std::cout << s << std::endl << std::endl;
    }

    std::cout << total_run << " tests run" << std::endl;
""")

main_cpp.write("}")

