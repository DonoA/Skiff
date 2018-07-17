#include "parser.h"
#include <iostream>

namespace skiff
{
    parse_pattern::parse_pattern(token_type tkn) {
        rules = vector<parse_pattern_part>();
        rules.push_back({&tkn, nullptr, nullptr});
    }

    parse_pattern parse_pattern::then(token_type tkn) {
        rules.push_back({&tkn, nullptr, nullptr});
    }

    parse_pattern parse_pattern::capture() {
        rules.push_back({nullptr, new vector<token>(), nullptr});
    }

    parse_pattern parse_pattern::terminate(token_type tkn) {
        rules.push_back({nullptr, nullptr, tkn});
    }

    void check_back_brace(token_type op, stack<token_type > * braces)
    {
        if (braces->empty() || braces->top() != op)
        {
            std::cout << "brace mismatch" << std::endl;
        }
        else
        {
            braces->pop();
        }
    }

    void track_brace(token_type tkn, stack<token_type> * braces)
    {
        switch (tkn)
        {
            case token_type::LEFT_BRACE:
            case token_type::LEFT_PAREN:
            case token_type::LEFT_BRACKET:
                braces->push(tkn);
                break;
            case token_type::RIGHT_BRACE:
                check_back_brace(token_type::LEFT_BRACE, braces);
                break;
            case token_type::RIGHT_PAREN:
                check_back_brace(token_type::LEFT_PAREN, braces);
                break;
            case token_type::RIGHT_BRACKET:
                check_back_brace(token_type::LEFT_BRACKET, braces);
                break;
            default: break;
        }
    }

    vector<vector<token> *> * parse_pattern::match(vector<token> tokens) {
        size_t rule_pos = 0;
        stack<token_type> braces;
        vector<vector<token> *> * captures = new vector<vector<token> *>();
        // rules must end with a token to match
        for(size_t i = 0; i < tokens.size(); i++)
        {
            track_brace(tokens.at(i).get_type(), &braces);
            if(rules.at(rule_pos).tkn != nullptr)
            {
                if(*rules.at(rule_pos).tkn == tokens.at(i).get_type())
                {
                    rule_pos++;
                }
                else
                {
                    std::cout << "Did not expect token!" << std::endl;
                    return nullptr;
                }
            }
            else if(rules.at(rule_pos).cap != nullptr)
            {
                // if we are not in a brace and (the next token matches our next rule or is the terminator and
                // (we are at the end or the next token is it))
                if(braces.empty() &&
                        (rules.at(rule_pos + 1).tkn != nullptr &&
                            *rules.at(rule_pos + 1).tkn == tokens.at(i).get_type()) ||
                        (rules.at(rule_pos + 1).term != nullptr &&
                            (*rules.at(rule_pos + 1).term == tokens.at(i).get_type()) || i + 1 >= tokens.size())
                        )
                {
                    captures->push_back(rules.at(rule_pos).cap);
                    rule_pos++;
                }
                else
                {
                    rules.at(rule_pos).cap->push_back(tokens.at(i));
                }
            }
        }
        return captures;
    }

    parser::parser(vector<token> stmt) {
        this->stmt = stmt;
        this->pos = 0;
    }

    statement *parser::parse() {

        parse_pattern DECLARE =
                parse_pattern(token_type::NAME).then(token_type::COLON).then(token_type::NAME).terminate(token_type::SEMICOLON);

        parse_pattern ASSIGNMENT =
                parse_pattern(token_type::NAME).then(token_type::EQUAL).capture().terminate(token_type::SEMICOLON);

        parse_pattern DECLARE_ASSIGN =
                parse_pattern(token_type::NAME).then(token_type::COLON).then(token_type::NAME).then(token_type::EQUAL)
                        .capture().terminate(token_type::SEMICOLON);

        parse_pattern FUNCTION_CALL =
                parse_pattern(token_type::NAME).then(token_type::LEFT_PAREN).capture().then(token_type::RIGHT_PAREN)
                        .terminate(token_type::SEMICOLON);

        parse_pattern FUNCTION_CALL_GENERIC =
                parse_pattern(token_type::NAME).then(token_type::LEFT_ANGLE_BRACE).then(token_type::NAME)
                        .then(token_type::RIGHT_ANGLE_BRACE).then(token_type::LEFT_PAREN).capture().then(token_type::RIGHT_PAREN)
                        .terminate(token_type::SEMICOLON);

        parse_pattern FUNCTION_DEF =
                parse_pattern(token_type::DEF).then(token_type::NAME).then(token_type::LEFT_PAREN).capture()
                        .then(token_type::RIGHT_PAREN).then(token_type::COLON).then(token_type::NAME)
                        .terminate(token_type::LEFT_BRACE);

        parse_pattern FUNCTION_DEF_GENERIC =
                parse_pattern(token_type::DEF).then(token_type::NAME).then(token_type::LEFT_ANGLE_BRACE).then(token_type::NAME)
                        .then(token_type::RIGHT_ANGLE_BRACE).then(token_type::LEFT_PAREN).capture()
                        .then(token_type::RIGHT_PAREN).then(token_type::COLON).then(token_type::NAME)
                        .terminate(token_type::LEFT_BRACE);



        vector<vector<token> *> * cap;
        cap = DECLARE.match(stmt);
        cap = ASSIGNMENT.match(stmt);
        cap = DECLARE.match(stmt);
        cap = DECLARE.match(stmt);

    }

    token parser::peek(int i)
    {
        if(this->pos + i >= stmt.size())
        {
            return token(token_type::FILEEND, nullptr, 0, 0);
        }
        else
        {
            return stmt.at(this->pos + i);
        }
    }

    vector<token> parser::consume_to(token_type tkn) {
        vector<token> tkns = vector<token>();
        for(; pos < stmt.size() && stmt.at(pos).get_type() != tkn; pos++)
        {
            tkns.push_back(stmt.at(pos));
        }
        return tkns;
    }

    void parser::expect_next(token_type tkn) {
        if(peek(1).get_type() != tkn)
        {
            std::cout << "This is a problem" << std::endl;
        }
    }
}
