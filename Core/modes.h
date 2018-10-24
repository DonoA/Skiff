#pragma once

#include <queue>
#include <string>
#include <fstream>
#include <iostream>

#include "interpreter/evaluated_types.h"
#include "compiler/compilation_types.h"
#include "parser/new_parser.h"
#include "statement.h"
//#include "parser/parser.h"
#include "utils.h"
#include "parser/token.h"

namespace skiff
{
    namespace modes
    {
        std::vector<statements::statement *> parse_file(std::string infile);

        void evaluate(environment::scope *env, std::vector<statements::statement *> statements);

        void compile(compilation_types::compilation_scope *env, std::vector<statements::statement *> statements, std::string outfile);
    }
}


