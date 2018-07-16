#pragma once
#include <string>
#include <queue>
#include <iostream>
#include <stack>
#include <algorithm>
#include <assert.h>

#include "statement.h"
#include "types.h"
#include "utils.h"
#include "token.h"

using std::vector;
using skiff::tokenizer::token;
using skiff::statements::statement;

namespace skiff
{
	statement * parse_statement(vector<token> stmt);

	bool handle_line(std::string input, char c, std::stack<skiff::statements::braced_block *> * stmts, bool debug);
}
