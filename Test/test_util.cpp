#include "test_util.h"

#include <iostream>

void run_test( void (*f)(), string name)
{
    total_test_count++;
    last_failed = false;
    current_test = name;
    f();
    if(last_failed)
    {
        tests_failed++;
        std::cout << "\033[31;41m \033[0m";
    }
    else
    {
        std::cout << "\033[32;42m \033[0m";
    }
}

void Assert::AreEqual(string s1, string s2)
{
    if(s1 != s2)
    {
        assert_failures.push_back(current_test + ": Got " + s2 + " expected " + s1);
        last_failed = true;
    }
}

void Assert::IsTrue(bool condition)
{
    if(!condition)
    {
        assert_failures.push_back(current_test + ": Is true failed");
        last_failed = true;
    }
}