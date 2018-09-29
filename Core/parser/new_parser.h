#pragma once

#include "token.h"
#include "statement.h"
#include <vector>
#include <stack>
#include <algorithm>
#include <iostream>

using std::vector;
using std::stack;

namespace skiff
{
    class new_parser
    {
    public:
        struct split_results {
            vector<vector<tokenizer::token>> match;
            tokenizer::token_type on;
        };

        new_parser(vector<tokenizer::token> tokens);
        vector<statements::statement *> parse_statement();
        statements::statement * parse_expression();

        split_results braced_split(vector<tokenizer::token_type> on, int count);
        split_results precedence_braced_split(vector<vector<tokenizer::token_type>> rankings);
    private:
        vector<tokenizer::token> tokens;
        size_t current_token = 0;

        vector<tokenizer::token> consume_parens(tokenizer::token_type leftparen, tokenizer::token_type rightparen);
        tokenizer::token get_current();
        tokenizer::token get_next();

        void consume(tokenizer::token_type typ);
        vector<tokenizer::token> consume_til(tokenizer::token_type typ);

        statements::statement * parse_if();
        statements::statement * parse_def();
    };
}
