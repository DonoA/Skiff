#pragma once

#include <queue>
#include <string>
#include <fstream>
#include <iostream>

#include "evaluated_types.h"
#include "statement.h"
#include "parser.h"
#include "utils.h"
#include "token.h"

namespace skiff
{
    namespace modes
    {
        std::vector<statements::statement *> parse_file(std::string infile);

        void evaluate(environment::scope *env, std::vector<statements::statement *> statements);
    }
}


