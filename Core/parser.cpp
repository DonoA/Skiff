#include "parser.h"
#include <iostream>

namespace skiff
{

    bool vec_contains_token(vector<token_type > v, token_type elem)
    {
        return std::find(v.begin(), v.end(), elem) != v.end();
    }

    parse_pattern_logic::parse_pattern_logic(token_type tkn) {
        this->rules = vector<token_type>();
        this->rules.push_back(tkn);
    }

    parse_pattern_logic parse_pattern_logic::maybe(token_type tkn) {
        this->rules.push_back(tkn);
        return *this;
    }

    vector<token_type> parse_pattern_logic::get_rules() {
        return rules;
    }

    parse_pattern_data::parse_pattern_data() {
        this->tkn = token_type(0);
        this->cap = vector<token>();
        this->term = token_type(0);
        this->multimatch = vector<token_type>();
    }

    parse_pattern_data::parse_pattern_data(token_type tkn){
        this->tkn = tkn;
        this->term = tkn;
    }

    parse_pattern_data::parse_pattern_data(vector<token> cap) : parse_pattern_data() {
        this->cap = cap;
    }

    parse_pattern_data::parse_pattern_data(vector<token_type> multimatch) : parse_pattern_data() {
        this->multimatch = multimatch;
    }

    parse_pattern::parse_pattern(token_type tkn) {
        rules = vector<parse_pattern_part>();
        parse_pattern_part pp = parse_pattern_part();
        pp.type = parse_pattern_type::TOKEN;
        pp.value = parse_pattern_data(tkn);
        rules.push_back(pp);
    }

    parse_pattern::parse_pattern(parse_pattern_logic ppl) {
        parse_pattern_part pp = parse_pattern_part();
        pp.type = parse_pattern_type::MULTIMATCH;
        pp.value = parse_pattern_data(ppl.get_rules());
        rules.push_back(pp);
    }

    parse_pattern::parse_pattern() {
        rules = vector<parse_pattern_part>();
        parse_pattern_part pp = parse_pattern_part();
        pp.type = parse_pattern_type::CAPTURE;
        pp.value = parse_pattern_data(vector<token>());
        rules.push_back(pp);
    }

    parse_pattern parse_pattern::then(token_type tkn) {
        parse_pattern_part pp = parse_pattern_part();
        pp.type = parse_pattern_type::TOKEN;
        pp.value = parse_pattern_data(tkn);
        rules.push_back(pp);
        return *this;
    }

    parse_pattern parse_pattern::then(parse_pattern_logic ppl) {
        parse_pattern_part pp = parse_pattern_part();
        pp.type = parse_pattern_type::MULTIMATCH;
        pp.value = parse_pattern_data(ppl.get_rules());
        rules.push_back(pp);
        return *this;
    }

    parse_pattern parse_pattern::capture() {
        parse_pattern_part pp = parse_pattern_part();
        pp.type = parse_pattern_type::CAPTURE;
        pp.value = parse_pattern_data(vector<token>());
        rules.push_back(pp);
        return *this;
    }

    parse_pattern parse_pattern::terminate(token_type tkn) {
        parse_pattern_part pp = parse_pattern_part();
        pp.type = parse_pattern_type::TERMINATE;
        pp.value = parse_pattern_data(tkn);
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
        // rules must end with a token or termination to match
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
                            (rules.at(rule_pos + 1).value.term == tokens.at(i).get_type())) ||
                        (rules.at(rule_pos + 1).type == parse_pattern_type::MULTIMATCH &&
                            vec_contains_token(rules.at(rule_pos + 1).value.multimatch, tokens.at(i).get_type()))
                        ))
                {
                    match->match_groups.push_back(rules.at(rule_pos).value.cap);
                    rule_pos++;
                }
                else if(braces.empty() && (rules.at(rule_pos + 1).type == parse_pattern_type::TERMINATE &&
                                           (i + 1 >= tokens.size()))) {
                    rules.at(rule_pos).value.cap.push_back(tokens.at(i));
                    match->captured++;
                    i++;
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
            else if(rules.at(rule_pos).type == parse_pattern_type::MULTIMATCH)
            {
                if(vec_contains_token(rules.at(rule_pos).value.multimatch, tokens.at(i).get_type()))
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
            else if(rules.at(rule_pos).type == parse_pattern_type::TERMINATE)
            {
                if(rules.at(rule_pos).value.term == tokens.at(i).get_type())
                {
                    match->selected_literals.push_back(tokens.at(i).get_lit());
                    return match;
                }
                else if(rule_pos + 1 >= rules.size())
                {
                    return match;
                }
                else
                {
                    return nullptr;
                }
            }
        }
        if(rules.at(rule_pos).type == parse_pattern_type::TERMINATE || rule_pos + 1 >= rules.size())
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

        parse_pattern PARENS =
                parse_pattern(token_type::LEFT_PAREN).capture().then(token_type::RIGHT_PAREN);

        parse_pattern DECLARE =
                parse_pattern(token_type::NAME).then(token_type::COLON).then(token_type::NAME).terminate(token_type::SEMICOLON);

        parse_pattern ASSIGNMENT =
                parse_pattern().then(token_type::EQUAL).capture().terminate(token_type::SEMICOLON);

        parse_pattern DECLARE_ASSIGN =
                parse_pattern(token_type::NAME).then(token_type::COLON).then(token_type::NAME).then(token_type::EQUAL)
                        .capture().terminate(token_type::SEMICOLON);

        parse_pattern FUNCTION_CALL =
                parse_pattern(token_type::NAME).capture().then(token_type::LEFT_PAREN).capture().then(token_type::RIGHT_PAREN)
                        .terminate(token_type::SEMICOLON);

        parse_pattern FUNCTION_DEF =
                parse_pattern(token_type::DEF).then(token_type::NAME).capture().then(token_type::LEFT_PAREN).capture()
                        .then(token_type::RIGHT_PAREN).then(token_type::COLON).then(token_type::NAME)
                        .then(token_type::LEFT_BRACE).capture().then(token_type::RIGHT_BRACE);

        parse_pattern CLASS_DEF =
                parse_pattern(token_type::CLASS).then(token_type::NAME).capture().then(token_type::LEFT_BRACE).capture()
                        .then(token_type::RIGHT_BRACE);

        parse_pattern STRUCT_DEF =
                parse_pattern(token_type::STRUCT).then(token_type::NAME).capture().then(token_type::LEFT_BRACE).capture()
                        .then(token_type::RIGHT_BRACE);

        parse_pattern NEW =
                parse_pattern(token_type::NEW).then(token_type::NAME).capture().then(token_type::LEFT_PAREN)
                        .capture().then(token_type::RIGHT_PAREN).terminate(token_type::SEMICOLON);

        parse_pattern NEW_ANNON_CLASS =
                parse_pattern(token_type::NEW).then(token_type::NAME).capture().then(token_type::LEFT_PAREN)
                        .capture().then(token_type::RIGHT_PAREN).then(token_type::LEFT_BRACE).capture()
                        .then(token_type::RIGHT_BRACE);

        parse_pattern NEW_QUCK_INIT =
                parse_pattern(token_type::NEW).then(token_type::NAME).capture().then(token_type::LEFT_PAREN)
                        .capture().then(token_type::RIGHT_PAREN).then(token_type::LEFT_BRACE).then(token_type::LEFT_BRACE).capture()
                        .then(token_type::RIGHT_BRACE).then(token_type::RIGHT_BRACE);

        parse_pattern RETURN =
                parse_pattern(token_type::RETURN).capture().then(token_type::SEMICOLON);

        parse_pattern BITWISE =
                parse_pattern().then(
                        parse_pattern_logic(token_type::BIT_AND).maybe(token_type::BIT_OR).maybe(token_type::BIT_XOR)
                            .maybe(token_type::BIT_RIGHT).maybe(token_type::BIT_LEFT).maybe(token_type::BIT_NOT)
                ).capture().terminate(token_type::SEMICOLON);

        parse_pattern BOOL_AND =
                parse_pattern().then(token_type::AND).capture().terminate(token_type::SEMICOLON);

        parse_pattern BOOL_OR =
                parse_pattern().then(token_type::OR).capture().terminate(token_type::SEMICOLON);

        parse_pattern BOOL_NOT =
                parse_pattern().then(token_type::BANG).capture().terminate(token_type::SEMICOLON);

        parse_pattern COMPARE =
                parse_pattern().then(
                        parse_pattern_logic(token_type::DOUBLE_EQUAL).maybe(token_type::LEFT_ANGLE_BRACE)
                                .maybe(token_type::RIGHT_ANGLE_BRACE).maybe(token_type::LESS_THAN_EQUAL)
                                .maybe(token_type::GREATER_THAN_EQUAL)
                ).capture().terminate(token_type::SEMICOLON);

        parse_pattern ADD_SUB =
                parse_pattern().then(
                        parse_pattern_logic(token_type::PLUS).maybe(token_type::MINUS)
                ).capture().terminate(token_type::SEMICOLON);

        parse_pattern DIV_MUL =
                parse_pattern().then(
                        parse_pattern_logic(token_type::STAR).maybe(token_type::DIV)
                ).capture().terminate(token_type::SEMICOLON);

        parse_pattern EXP =
                parse_pattern().then(token_type::EXP).capture().terminate(token_type::SEMICOLON);

        parse_pattern LIST_ACCESS =
                parse_pattern(token_type::NAME).then(token_type::LEFT_BRACKET).capture()
                        .then(token_type::RIGHT_BRACKET).terminate(token_type::SEMICOLON);

        parse_pattern IMPORT_LOCAL =
                parse_pattern(token_type::IMPORT).then(token_type::LITERAL);

        parse_pattern IMPORT_SYS =
                parse_pattern(token_type::IMPORT).then(token_type::LEFT_ANGLE_BRACE).then(token_type::NAME)
                        .then(token_type::RIGHT_ANGLE_BRACE);

        parse_pattern FLOW =
                parse_pattern(
                        parse_pattern_logic(token_type::IF).maybe(token_type::WHILE).maybe(token_type::FOR)
                ).then(token_type::LEFT_PAREN).capture().then(token_type::RIGHT_PAREN)
                        .terminate(token_type::LEFT_BRACE).capture().then(token_type::RIGHT_BRACE);

        parse_pattern SWITCH_MATCH =
                parse_pattern(
                        parse_pattern_logic(token_type::SWITCH).maybe(token_type::MATCH)
                ).then(token_type::LEFT_PAREN).capture().then(token_type::RIGHT_PAREN)
                        .terminate(token_type::LEFT_BRACE).capture().then(token_type::RIGHT_BRACE);

        parse_pattern CASE =
                parse_pattern(token_type::CASE).capture().then(token_type::ARROW).terminate(token_type::LEFT_BRACE)
                        .capture().then(token_type::RIGHT_BRACE);

        parse_pattern THROW =
                parse_pattern(token_type::THROW).capture().terminate(token_type::SEMICOLON);

        parse_pattern TRY =
                parse_pattern(token_type::TRY).capture().then(token_type::LEFT_BRACE).capture()
                        .then(token_type::RIGHT_BRACE);

        parse_pattern CATCH =
                parse_pattern(token_type::CATCH).then(token_type::LEFT_PAREN).capture().then(token_type::RIGHT_PAREN)
                        .then(token_type::LEFT_BRACE).capture().then(token_type::RIGHT_BRACE);

        parse_pattern FINALLY =
                parse_pattern(token_type::FINALLY).then(token_type::LEFT_BRACE).capture().then(token_type::RIGHT_BRACE);

        parse_pattern DEF_ANNOTATION =
                parse_pattern(token_type::ANNOTATION).then(token_type::NAME).then(token_type::LEFT_BRACE).capture()
                        .then(token_type::RIGHT_BRACE);

        parse_pattern ANNOTATION =
                parse_pattern(token_type::AT).then(token_type::NAME);

        parse_pattern ANNOTATION_PARAMS =
                parse_pattern(token_type::AT).then(token_type::NAME).then(token_type::LEFT_PAREN).capture()
                        .then(token_type::RIGHT_PAREN);

        parse_pattern ENUM =
                parse_pattern(token_type::ENUM).capture().then(token_type::NAME).capture().then(token_type::LEFT_BRACE)
                        .capture().then(token_type::RIGHT_BRACE);

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

            cap = BOOL_AND.match(pos, stmt);
            if(cap)
            {
                statements.push_back(
                        new statements::boolean_conjunction(
                                parser(cap->match_groups.at(0)).parse().at(0),
                                statements::boolean_conjunction::conjunction_type::AND,
                                parser(cap->match_groups.at(1)).parse().at(0)
                        ));
                pos += cap->captured + 1;
                continue;
            }

            cap = BOOL_OR.match(pos, stmt);
            if(cap)
            {
                statements.push_back(
                        new statements::boolean_conjunction(
                                parser(cap->match_groups.at(0)).parse().at(0),
                                statements::boolean_conjunction::conjunction_type::OR,
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
