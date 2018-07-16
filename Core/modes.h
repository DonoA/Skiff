#pragma once
#include <queue>
#include <string>
#include <fstream>
#include <iostream>

#include "types.h"
#include "statement.h"
#include "parser.h"
#include "utils.h"
#include "token.h"

namespace skiff
{
	namespace modes
	{
		void interactive_mode();
		std::queue<statements::statement *> parse_file(std::string infile, bool debug);
		void print_parse(std::queue<statements::statement *> statements);
		void translate_c(std::queue<statements::statement *> statements);
		void evaluate(environment::scope * env, std::queue<statements::statement *> statements);
	}
}


