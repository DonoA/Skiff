#include "parser.h"
#include <iostream>

namespace skiff
{
    parse_pattern::parse_pattern(token_type tkn) {
        rules = vector<parse_pattern_part>();
        parse_pattern_part pp = parse_pattern_part();
        pp.type = parse_pattern_type::TOKEN;
        pp.value = {tkn, vector<token>(), tkn};
        rules.push_back(pp);
    }

    parse_pattern::parse_pattern() {
        rules = vector<parse_pattern_part>();
        parse_pattern_part pp = parse_pattern_part();
        pp.type = parse_pattern_type::CAPTURE;
        pp.value = {token_type(0), vector<token>(), token_type(0)};
        rules.push_back(pp);
    }

    parse_pattern parse_pattern::then(token_type tkn) {
        parse_pattern_part pp = parse_pattern_part();
        pp.type = parse_pattern_type::TOKEN;
        pp.value = {tkn, vector<token>(), tkn};
        rules.push_back(pp);
        return *this;
    }

    parse_pattern parse_pattern::capture() {
        parse_pattern_part pp = parse_pattern_part();
        pp.type = parse_pattern_type::CAPTURE;
        pp.value = {token_type(0), vector<token>(), token_type(0)};
        rules.push_back(pp);
        return *this;
    }

    parse_pattern parse_pattern::terminate(token_type tkn) {
        parse_pattern_part pp = parse_pattern_part();
        pp.type = parse_pattern_type::TERMINATE;
        pp.value = {tkn, vector<token>(), tkn};
        rules.push_back(pp);
        return *this;
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

    parse_match * parse_pattern::match(size_t strt, vector<token> tokens) {
        size_t rule_pos = 0;
        stack<token_type> braces;
        parse_match * match = new parse_match();
        match->selected_literals = vector<literal *>();
        match->match_groups = vector<vector<token>>();
        match->captured = 0;
        // rules must end with a token to match
        for(size_t i = strt; i < tokens.size();)
        {
            track_brace(tokens.at(i).get_type(), &braces);
            if(rules.at(rule_pos).type == parse_pattern_type::TOKEN)
            {
                if(rules.at(rule_pos).value.tkn == tokens.at(i).get_type())
                {
                    match->selected_literals.push_back(tokens.at(i).get_lit());
                    rule_pos++;
                    match->captured++;
                    i++;
                }
                else
                {
                    return nullptr;
                }
            }
            else if(rules.at(rule_pos).type == parse_pattern_type::CAPTURE)
            {
                // if we are not in a brace and (the next token matches our next rule or is the terminator and
                // (we are at the end or the next token is it))
                if(braces.empty() && (
                        (rules.at(rule_pos + 1).type == parse_pattern_type::TOKEN &&
                            rules.at(rule_pos + 1).value.tkn == tokens.at(i).get_type()) ||
                        (rules.at(rule_pos + 1).type == parse_pattern_type::TERMINATE &&
                            (rules.at(rule_pos + 1).value.term == tokens.at(i).get_type()) || i + 1 >= tokens.size())
                        ))
                {
                    match->match_groups.push_back(rules.at(rule_pos).value.cap);
                    rule_pos++;
                }
                else
                {
                    rules.at(rule_pos).value.cap.push_back(tokens.at(i));
                    match->captured++;
                    i++;
                }
            }
            else if(rules.at(rule_pos).type == parse_pattern_type::TERMINATE)
            {
                if(rules.at(rule_pos).value.term == tokens.at(i).get_type())
                {
                    match->selected_literals.push_back(tokens.at(i).get_lit());
                    return match;
                }
                else
                {
                    return nullptr;
                }
            }
        }
        if(rules.at(rule_pos).type == parse_pattern_type::TERMINATE)
        {
            return match;
        }
        else
        {
            return nullptr;
        }
    }

    parser::parser(vector<token> stmt) {
        this->stmt = stmt;
        this->pos = 0;
    }

    vector<statement *> parser::parse() {

        parse_pattern DECLARE =
                parse_pattern(token_type::NAME).then(token_type::COLON).then(token_type::NAME).terminate(token_type::SEMICOLON);

        parse_pattern ASSIGNMENT =
                parse_pattern().then(token_type::EQUAL).capture().terminate(token_type::SEMICOLON);

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

        parse_pattern LITERAL =
                parse_pattern(token_type::LITERAL).terminate(token_type::SEMICOLON);

        parse_pattern VARIABLE =
                parse_pattern(token_type::NAME).terminate(token_type::SEMICOLON);

        vector<statement *> statements = vector<statement *>();

        while(peek(0).get_type() != token_type::FILEEND)
        {
            parse_match * cap = nullptr;

            cap = DECLARE.match(pos, stmt);
            if(cap)
            {
                statements.push_back(
                        new statements::declaration(
                                cap->selected_literals.at(0)->to_string(),
                                statements::type_statement(cap->selected_literals.at(2)->to_string())
                        ));
                pos += cap->captured + 1;
                continue;
            }

            cap = ASSIGNMENT.match(pos, stmt);
            if(cap)
            {
                statements.push_back(
                        new statements::assignment(
                                parser(cap->match_groups.at(0)).parse().at(0),
                                parser(cap->match_groups.at(1)).parse().at(0)
                        ));
                pos += cap->captured + 1;
                continue;
            }

            cap = LITERAL.match(pos, stmt);
            if(cap)
            {
                statements.push_back(
                        new statements::value(
                                cap->selected_literals.at(0)->to_string()
                        ));
                pos += cap->captured + 1;
                continue;
            }

            cap = VARIABLE.match(pos, stmt);
            if(cap)
            {
                statements.push_back(
                        new statements::variable(
                                cap->selected_literals.at(0)->to_string()
                        ));
                pos += cap->captured + 1;
                continue;
            }

            pos++;
        }

        return statements;
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
