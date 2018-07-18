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
using std::stack;
using skiff::tokenizer::token;
using skiff::tokenizer::token_type;
using skiff::tokenizer::literal;
using skiff::statements::statement;

namespace skiff
{

    struct parse_match
    {
        vector<vector<token>> match_groups;
        vector<literal *> selected_literals;
    };

    enum parse_pattern_type
    {
        TOKEN, CAPTURE, TERMINATE
    };

    struct parse_pattern_data
    {
        token_type tkn;
        vector<token> cap;
        token_type term;
    };

    struct parse_pattern_part
    {
        parse_pattern_type type;
        parse_pattern_data value;
    };

    class parse_pattern
    {
    public:
        explicit parse_pattern(token_type tkn);
        parse_pattern then(token_type tkn);
        parse_pattern capture();
        parse_pattern terminate(token_type tkn);
        parse_match * match(vector<token> tokens);
    private:
        vector<parse_pattern_part> rules;
    };

    class parser
    {
    public:
        parser(vector<token> stmt);
        statement * parse();
    private:
        token peek(int i);
        vector<token> consume_to(token_type tkn);
        void expect_next(token_type tkn);
        vector<token> stmt;
        size_t pos;
    };

    bool handle_line(std::string input, char c, std::stack<skiff::statements::braced_block *> * stmts, bool debug);
}
