#pragma once

#include <string>
#include <vector>

using std::string;
using std::vector;

#define TEST_CLASS(name, ord) namespace name ## _tests

#define TEST_METHOD(name) void name ## _test()

extern vector<string> assert_failures;
extern bool last_failed;
extern size_t tests_passed;
extern size_t tests_failed;
extern string current_test;

void run_test( void (*f)(), string name);

namespace Assert
{
    void AreEqual(string first, string second);
    void IsTrue(bool condition);
}