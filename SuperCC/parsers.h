#pragma once
#include <string>
#include <stack>
#include <vector>
#include "statement.h"
#include "types.h"

using std::stack;
using std::vector;
using std::string;

void check_back_brace(char op, stack<char> * braces);

void try_push(char op, stack<char> * braces);

vector<string> parse_argument_list(string list);

statement * parse_statement(string stmt, scope * env);

