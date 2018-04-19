#pragma once
#include <string>
#include <queue>
#include <iostream>
#include <queue>
#include <algorithm>
#include <assert.h>

#include "statement.h"
#include "types.h"
#include "utils.h"

namespace skiff
{
	skiff::statements::statement * parse_statement(std::string stmt);

	bool handle_line(std::string input, char c, std::queue<skiff::statements::statement *> * stmts);
}
