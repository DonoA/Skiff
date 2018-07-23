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

    parse_pattern::parse_pattern(parse_pattern_logic ppl, parse_pattern_type typ) {
        parse_pattern_part pp = parse_pattern_part();
        pp.type = typ;
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

    parse_pattern parse_pattern::capture(parse_pattern_logic ppl) {
        parse_pattern_part pp = parse_pattern_part();
        pp.type = parse_pattern_type::CAPTURE;
        pp.value = parse_pattern_data(ppl.get_rules());
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

    void track_generic_brace(token_type tkn, stack<token_type> * braces)
    {
        switch (tkn)
        {
            case token_type::LEFT_ANGLE_BRACE:
                braces->push(tkn);
                break;
            case token_type::RIGHT_ANGLE_BRACE:
                check_back_brace(token_type::LEFT_ANGLE_BRACE, braces);
                break;
            default: break;
        }
    }

    parse_match * parse_pattern::match(size_t strt, vector<token> tokens) {
        return this->match_with_brace_track(strt, tokens, track_brace);
    }

    parse_match *parse_pattern::match_with_brace_track(size_t strt, vector<token> tokens,
                                                       std::function<void(token_type, stack<token_type> *)> brace_tracker) {
        size_t rule_pos = 0;
        stack<token_type> braces;
        parse_match * match = new parse_match();
        match->selected_tokens = vector<token>();
        match->match_groups = vector<vector<token>>();
        match->captured = 0;
        // rules must end with a token or termination to match
        for(size_t i = strt; i < tokens.size() && rule_pos < rules.size();)
        {
            if(rules.at(rule_pos).type == parse_pattern_type::TOKEN)
            {
                if(rules.at(rule_pos).value.tkn == tokens.at(i).get_type())
                {
                    match->selected_tokens.push_back(tokens.at(i));
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
                                           (i + 1 >= tokens.size())) &&
                        (rules.at(rule_pos).value.multimatch.empty() ||
                         vec_contains_token(rules.at(rule_pos).value.multimatch, tokens.at(i).get_type())))
                {
                    rules.at(rule_pos).value.cap.push_back(tokens.at(i));
                    match->captured++;
                    i++;
                    match->match_groups.push_back(rules.at(rule_pos).value.cap);
                    rule_pos++;
                }
                else if(rules.at(rule_pos).value.multimatch.empty() ||
                        vec_contains_token(rules.at(rule_pos).value.multimatch, tokens.at(i).get_type()))
                {
                    brace_tracker(tokens.at(i).get_type(), &braces);
                    rules.at(rule_pos).value.cap.push_back(tokens.at(i));
                    match->captured++;
                    i++;
                }
                else
                {
                    return nullptr;
                }
            }
            else if(rules.at(rule_pos).type == parse_pattern_type::MULTIMATCH)
            {
                if(vec_contains_token(rules.at(rule_pos).value.multimatch, tokens.at(i).get_type()))
                {
                    match->selected_tokens.push_back(tokens.at(i));
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
                    match->selected_tokens.push_back(tokens.at(i));
                    return match;
                }
                else if(i + 1 >= tokens.size())
                {
                    return match;
                }
                else
                {
                    return nullptr;
                }
            }
        }
        if(rule_pos + 1 >= rules.size() || rules.at(rule_pos).type == parse_pattern_type::TERMINATE)
        {
            return match;
        }
        else
        {
            return nullptr;
        }
    }

    vector<vector<token>> split_with_generic_braces(vector<token> tokens, token_type split)
    {
        vector<vector<token>> bits = vector<vector<token>>();
        vector<token> buffer = vector<token>();
        stack<token_type> braces;
        for(size_t i = 0; i < tokens.size(); i++)
        {
            track_generic_brace(tokens.at(i).get_type(), &braces);
            if(braces.empty() && tokens.at(i).get_type() == split)
            {
                bits.push_back(buffer);
                buffer.clear();
            }
            else
            {
                buffer.push_back(tokens.at(i));
            }
        }
        bits.push_back(buffer);
        buffer.clear();
        return bits;
    }

    vector<statement *> split_and_parse(vector<token> tokens, token_type split)
    {
        vector<statement *> stmts = vector<statement *>();
        vector<token> buffer = vector<token>();
        stack<token_type> braces;
        for(size_t i = 0; i < tokens.size(); i++)
        {
            track_brace(tokens.at(i).get_type(), &braces);
            if(braces.empty() && tokens.at(i).get_type() == split)
            {
                vector<statement *> parsed = parser(buffer).parse();
                for(statement * stmt : parsed)
                {
                    stmts.push_back(stmt);
                }
                buffer.clear();
            }
            else
            {
                buffer.push_back(tokens.at(i));
            }
        }
        vector<statement *> parsed = parser(buffer).parse();
        for(statement * stmt : parsed)
        {
            stmts.push_back(stmt);
        }
        buffer.clear();
        return stmts;
    }

    parser::parser(vector<token> stmt) {
        this->stmt = stmt;
        this->pos = 0;
    }

    statements::type_statement parse_type_call(vector<token> tkns)
    {
        parse_pattern GENERIC_TYPE =
                parse_pattern().then(token_type::LEFT_ANGLE_BRACE).capture().then(token_type::RIGHT_ANGLE_BRACE);

        parse_match * cap = GENERIC_TYPE.match_with_brace_track(0, tkns, track_generic_brace);

        vector<statements::type_statement> generic_types = vector<statements::type_statement>();

        if(cap)
        {
            vector<vector<token>>  typs = split_with_generic_braces(cap->match_groups.at(1), token_type::COMMA);
            for(vector<token> seq : typs)
            {
                generic_types.push_back(parse_type_call(seq));
                // delete it here
            }
            tkns = cap->match_groups.at(0);
        }

        statements::type_statement stmt = statements::type_statement(parser(tkns).parse().at(0), generic_types, nullptr);

        return stmt;
    }

    vector<statements::function_definition::function_parameter> parse_function_params(vector<token> tokens)
    {
        parse_pattern FX_PARAM =
                parse_pattern(token_type::NAME).then(token_type::COLON).then(token_type::NAME).terminate(token_type::COMMA);

        using param = statements::function_definition::function_parameter;
        vector<param> params = vector<param>();
        size_t pos = 0;
        while(pos < tokens.size())
        {
            parse_match * cap = FX_PARAM.match(pos, tokens);
            if(cap)
            {
//                params.emplace_back(
//                        cap->selected_tokens.at(0).get_lit()->to_string(),
//                        statements::type_statement(cap->selected_tokens.at(2).get_lit()->to_string())
//                );
                pos += cap->captured + 1;
                continue;
            }
            else
            {
                std::cout << "Param messed" << std::endl;
            }
        }
        return params;
    }

    vector<statement *> parser::parse() {

        parse_pattern_logic TYPE_CALL = parse_pattern_logic(token_type::NAME).maybe(token_type::LEFT_ANGLE_BRACE)
                .maybe(token_type::RIGHT_ANGLE_BRACE).maybe(token_type::COMMA).maybe(token_type::DOT);

        parse_pattern_logic TYPE_DEF = parse_pattern_logic(token_type::NAME).maybe(token_type::LEFT_ANGLE_BRACE)
                .maybe(token_type::RIGHT_ANGLE_BRACE).maybe(token_type::COMMA).maybe(token_type::DOT).maybe(token_type::COLON);




        parse_pattern PARENS =
                parse_pattern(token_type::LEFT_PAREN).capture().then(token_type::RIGHT_PAREN);

        parse_pattern DECLARE =
                parse_pattern(token_type::NAME).then(token_type::COLON).capture(TYPE_CALL).terminate(token_type::SEMICOLON);

        parse_pattern ASSIGNMENT =
                parse_pattern().then(token_type::EQUAL).capture().terminate(token_type::SEMICOLON);

        parse_pattern DECLARE_ASSIGN =
                parse_pattern(token_type::NAME).then(token_type::COLON).capture(TYPE_CALL).then(token_type::EQUAL)
                        .capture().terminate(token_type::SEMICOLON);

        parse_pattern FUNCTION_CALL =
                parse_pattern(TYPE_CALL, parse_pattern_type::CAPTURE).then(token_type::LEFT_PAREN).capture().then(token_type::RIGHT_PAREN)
                        .terminate(token_type::SEMICOLON);

        parse_pattern FUNCTION_DEF =
                parse_pattern(token_type::DEF).then(token_type::NAME).capture().then(token_type::LEFT_PAREN).capture()
                        .then(token_type::RIGHT_PAREN).then(token_type::COLON).then(token_type::NAME)
                        .then(token_type::LEFT_BRACE).capture().then(token_type::RIGHT_BRACE);

        parse_pattern CLASS_DEF =
                parse_pattern(parse_pattern_logic(token_type::CLASS).maybe(token_type::STRUCT), parse_pattern_type::MULTIMATCH)
                        .then(token_type::NAME).capture().then(token_type::LEFT_BRACE).capture()
                        .then(token_type::RIGHT_BRACE);

        parse_pattern NEW =
                parse_pattern(token_type::NEW).capture(TYPE_CALL).then(token_type::LEFT_PAREN)
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
                ).capture().terminate(token_type::SEMICOLON);

        parse_pattern BIT_SHIFT_LEFT =
                parse_pattern().then(token_type::LEFT_ANGLE_BRACE).then(token_type::LEFT_ANGLE_BRACE).capture()
                        .terminate(token_type::SEMICOLON);

        parse_pattern BIT_SHIFT_RIGHT =
                parse_pattern().then(token_type::RIGHT_ANGLE_BRACE).then(token_type::RIGHT_ANGLE_BRACE).capture().terminate(token_type::SEMICOLON);

        parse_pattern BITWISE_NOT =
                parse_pattern(token_type::BIT_NOT).capture().terminate(token_type::SEMICOLON);

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

        parse_pattern FLOW_CONTROL =
                parse_pattern(parse_pattern_logic(token_type::BREAK).maybe(token_type::NEXT), parse_pattern_type::MULTIMATCH)
                        .then(token_type::SEMICOLON);

        parse_pattern DIV_MUL =
                parse_pattern().then(
                        parse_pattern_logic(token_type::STAR).maybe(token_type::DIV)
                ).capture().terminate(token_type::SEMICOLON);

        parse_pattern EXP =
                parse_pattern().then(token_type::EXP).capture().terminate(token_type::SEMICOLON);

        parse_pattern LIST_ACCESS =
                parse_pattern().then(token_type::LEFT_BRACKET).capture()
                        .then(token_type::RIGHT_BRACKET).terminate(token_type::SEMICOLON);

        parse_pattern IMPORT_LOCAL =
                parse_pattern(token_type::IMPORT).then(token_type::LITERAL);

        parse_pattern IMPORT_SYS =
                parse_pattern(token_type::IMPORT).then(token_type::LEFT_ANGLE_BRACE).then(token_type::NAME)
                        .then(token_type::RIGHT_ANGLE_BRACE);

        parse_pattern ELSE =
                parse_pattern(token_type::ELSE).capture().then(token_type::LEFT_BRACE).capture().then(token_type::RIGHT_BRACE);

        parse_pattern FLOW =
                parse_pattern(
                        parse_pattern_logic(token_type::IF).maybe(token_type::WHILE).maybe(token_type::FOR),
                        parse_pattern_type::MULTIMATCH
                ).then(token_type::LEFT_PAREN).capture().then(token_type::RIGHT_PAREN)
                        .then(token_type::LEFT_BRACE).capture().then(token_type::RIGHT_BRACE);

        parse_pattern BASIC_FOR_DIRECTIVE =
                parse_pattern().then(token_type::SEMICOLON).capture().then(token_type::SEMICOLON).capture()
                        .terminate(token_type::RIGHT_PAREN);

        parse_pattern ITR_FOR_DIRECTIVE =
                parse_pattern(token_type::NAME).then(token_type::COLON).then(token_type::NAME).then(token_type::COLON)
                        .capture().terminate(token_type::RIGHT_PAREN);

        parse_pattern SWITCH_MATCH =
                parse_pattern(
                        parse_pattern_logic(token_type::SWITCH).maybe(token_type::MATCH),
                        parse_pattern_type::MULTIMATCH
                ).then(token_type::LEFT_PAREN).capture().then(token_type::RIGHT_PAREN)
                        .then(token_type::LEFT_BRACE).capture().then(token_type::RIGHT_BRACE);

        parse_pattern CASE =
                parse_pattern(token_type::CASE).capture().then(token_type::ARROW).then(token_type::LEFT_BRACE)
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

        parse_pattern DEF_ENUM =
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

            cap = DECLARE_ASSIGN.match(pos, stmt);
            if(cap)
            {
                statements.push_back(
                        new statements::declaration_with_assignment(
                                cap->selected_tokens.at(0).get_lit()->to_string(),
                                parse_type_call(cap->match_groups.at(0)),
                                parser(cap->match_groups.at(1)).parse().at(0)
                        ));
                pos += cap->captured + 1;
                continue;
            }

            cap = DECLARE.match(pos, stmt);
            if(cap)
            {
                statements.push_back(
                        new statements::declaration(
                                cap->selected_tokens.at(0).get_lit()->to_string(),
                                parse_type_call(cap->match_groups.at(0))
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

            cap = FUNCTION_DEF.match(pos, stmt);
            if(cap)
            {
//                statements.push_back(
//                        new statements::function_definition(
//                                cap->selected_tokens.at(1).get_lit()->to_string(),
//                                parse_function_params(cap->match_groups.at(1)),
//                                statements::type_statement(
//                                        cap->selected_tokens.at(5).get_lit()->to_string()
//                                ),
//                                parser(cap->match_groups.at(2)).parse()
//                        ));
                pos += cap->captured + 1;
                continue;
            }

            cap = NEW.match(pos, stmt);
            if(cap)
            {
                statements.push_back(
                        new statements::new_object_statement(
                                parse_type_call(cap->match_groups.at(0)),
                                split_and_parse(cap->match_groups.at(1), token_type::COMMA)
                        ));
                pos += cap->captured + 1;
                continue;
            }

            cap = RETURN.match(pos, stmt);
            if(cap)
            {
                statements.push_back(
                        new statements::return_statement(
                                parser(cap->match_groups.at(0)).parse().at(0)
                        ));
                pos += cap->captured + 1;
                continue;
            }

            cap = FLOW_CONTROL.match(pos, stmt);
            if(cap)
            {
                using type = statements::flow_statement::type;
                type op;
                switch(cap->selected_tokens.at(0).get_type())
                {
                    case token_type::BREAK: op = type::BREAK; break;
                    case token_type::NEXT: op = type::NEXT; break;
                    default: std::cout << "Not break or next" << std::endl;
                }
                statements.push_back(new statements::flow_statement(op));
                pos += cap->captured + 1;
                continue;
            }

            // the type needs denoting not terminated, causing problems
            cap = IMPORT_LOCAL.match(pos, stmt);
            if(cap)
            {
                statements.push_back(
                        new statements::import_statement(
                                cap->selected_tokens.at(1).get_lit()->to_string()
                        ));
                pos += cap->captured + 1;
                continue;
            }

            cap = IMPORT_SYS.match(pos, stmt);
            if(cap)
            {
                statements.push_back(
                        new statements::import_statement(
                                cap->selected_tokens.at(2).get_lit()->to_string()
                        ));
                pos += cap->captured + 1;
                continue;
            }

            cap = FUNCTION_CALL.match(pos, stmt);
            if(cap)
            {
                statements.push_back(
                        new statements::function_call(
                                parse_type_call(cap->match_groups.at(0)),
                                split_and_parse(cap->match_groups.at(1), token_type::COMMA)
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

            cap = BOOL_NOT.match(pos, stmt);
            if(cap)
            {
                statements.push_back(
                        new statements::invert(
                                parser(cap->match_groups.at(1)).parse().at(0)
                        ));
                pos += cap->captured + 1;
                continue;
            }

            cap = COMPARE.match(pos, stmt);
            if(cap)
            {
                using cap_type = statements::comparison::comparison_type;
                cap_type cp;
                switch(cap->selected_tokens.at(0).get_type())
                {
                    case token_type::DOUBLE_EQUAL: cp = cap_type::EQUAL; break;
                    case token_type::BANG_EQUAL: cp = cap_type::NOT_EQUAL; break;
                    case token_type::LEFT_ANGLE_BRACE: cp = cap_type::LESS_THAN; break;
                    case token_type::LESS_THAN_EQUAL: cp = cap_type::LESS_THAN_EQUAL_TO; break;
                    case token_type::RIGHT_ANGLE_BRACE: cp = cap_type::GREATER_THAN; break;
                    case token_type::GREATER_THAN_EQUAL: cp = cap_type::GREATER_THAN_EQUAL_TO; break;
                    default: std::cout << "Bad comparison match" << std::endl;
                }
                statements.push_back(
                        new statements::comparison(
                                parser(cap->match_groups.at(0)).parse().at(0),
                                cp,
                                parser(cap->match_groups.at(1)).parse().at(0)
                        ));
                pos += cap->captured + 1;
                continue;
            }

            cap = BITWISE.match(pos, stmt);
            if(cap)
            {
                using bit_op = statements::bitwise::operation ;
                bit_op op;
                switch(cap->selected_tokens.at(0).get_type())
                {
                    case token_type::BIT_AND: op = bit_op::AND; break;
                    case token_type::BIT_OR: op = bit_op::OR; break;
                    case token_type::BIT_XOR: op = bit_op::XOR; break;
                    default: std::cout << "Bad bitwise match" << std::endl;
                }
                statements.push_back(
                        new statements::bitwise(
                                parser(cap->match_groups.at(0)).parse().at(0),
                                op,
                                parser(cap->match_groups.at(1)).parse().at(0)
                        ));
                pos += cap->captured + 1;
                continue;
            }

            cap = BITWISE_NOT.match(pos, stmt);
            if(cap)
            {
                statements.push_back(
                        new statements::bitinvert(
                                parser(cap->match_groups.at(0)).parse().at(0)
                        ));
                pos += cap->captured + 1;
                continue;
            }

            cap = ELSE.match(pos, stmt);
            if(cap)
            {
                statements.push_back(
                        new statements::else_directive(
                                parser(cap->match_groups.at(1)).parse()
                        ));
                pos += cap->captured + 1;
                continue;
            }

            cap = FLOW.match(pos, stmt);
            if(cap)
            {
                switch(cap->selected_tokens.at(0).get_type())
                {
                    case token_type::IF: {
                        statements.push_back(
                                new statements::if_directive(
                                        parser(cap->match_groups.at(0)).parse().at(0),
                                        parser(cap->match_groups.at(1)).parse()
                                ));
                        break;
                    }
                    case token_type::WHILE: {
                        statements.push_back(
                                new statements::while_directive(
                                        parser(cap->match_groups.at(0)).parse().at(0),
                                        parser(cap->match_groups.at(1)).parse()
                                ));
                        break;
                    }
                    case token_type::FOR: {
                        parse_match * inner_cap = BASIC_FOR_DIRECTIVE.match(0, cap->match_groups.at(0));
                        if(inner_cap)
                        {
                            statements.push_back(
                                    new statements::for_classic_directive(
                                            parser(inner_cap->match_groups.at(0)).parse().at(0),
                                            parser(inner_cap->match_groups.at(1)).parse().at(0),
                                            parser(inner_cap->match_groups.at(2)).parse().at(0),
                                            parser(cap->match_groups.at(1)).parse()
                                    ));
                            break;
                        }

                        inner_cap = ITR_FOR_DIRECTIVE.match(0, cap->match_groups.at(0));
                        if(inner_cap)
                        {
//                            statements.push_back(
//                                    new statements::for_itterator_directive(
//                                            inner_cap->selected_tokens.at(0).get_lit()->to_string(),
//                                            statements::type_statement(
//                                                    inner_cap->selected_tokens.at(2).get_lit()->to_string()
//                                            ),
//                                            parser(inner_cap->match_groups.at(0)).parse().at(0),
//                                            parser(cap->match_groups.at(1)).parse()
//                                    ));
                            break;
                        }
                        std::cout << "Bad for loop" << std::endl;
                        break;
                    }
                    default: std::cout << "Unknown loop in flow match" << std::endl; break;
                }

                pos += cap->captured + 1;
                continue;
            }

            cap = LIST_ACCESS.match(pos, stmt);
            if(cap)
            {
                statements.push_back(
                        new statements::list_accessor(
                                parser(cap->match_groups.at(0)).parse().at(0),
                                parser(cap->match_groups.at(1)).parse().at(0)
                        ));
                pos += cap->captured + 1;
                continue;
            }

            // PEMDAS
            cap = EXP.match(pos, stmt);
            if(cap)
            {
                statements.push_back(
                        new statements::math_statement(
                                parser(cap->match_groups.at(0)).parse().at(0),
                                statements::math_statement::op::EXP,
                                parser(cap->match_groups.at(1)).parse().at(0)
                        ));
                pos += cap->captured + 1;
                continue;
            }

            cap = DIV_MUL.match(pos, stmt);
            if(cap)
            {
                statements::math_statement::op opr;
                switch(cap->selected_tokens.at(0).get_type())
                {
                    case token_type::STAR: opr = statements::math_statement::MUL; break;
                    case token_type::DIV: opr = statements::math_statement::DIV; break;
                    default: std::cout << "Bad math op for div mul" << std::endl; break;
                }
                statements.push_back(
                        new statements::math_statement(
                                parser(cap->match_groups.at(0)).parse().at(0),
                                opr,
                                parser(cap->match_groups.at(1)).parse().at(0)
                        ));
                pos += cap->captured + 1;
                continue;
            }

            cap = ADD_SUB.match(pos, stmt);
            if(cap)
            {
                statements::math_statement::op op;
                switch(cap->selected_tokens.at(0).get_type())
                {
                    case token_type::PLUS: op = statements::math_statement::ADD; break;
                    case token_type::MINUS: op = statements::math_statement::SUB; break;
                    default: std::cout << "Bad math op for add sub" << std::endl; break;
                }
                statements.push_back(
                        new statements::math_statement(
                                parser(cap->match_groups.at(0)).parse().at(0),
                                op,
                                parser(cap->match_groups.at(1)).parse().at(0)
                        ));
                pos += cap->captured + 1;
                continue;
            }

            cap = THROW.match(pos, stmt);
            if(cap)
            {
                statements.push_back(
                        new statements::throw_statement(
                                parser(cap->match_groups.at(0)).parse().at(0)
                        ));
                pos += cap->captured + 1;
                continue;
            }

            cap = LITERAL.match(pos, stmt);
            if(cap)
            {
                statements.push_back(
                        new statements::value(
                                cap->selected_tokens.at(0).get_lit()->to_string()
                        ));
                pos += cap->captured + 1;
                continue;
            }

            cap = VARIABLE.match(pos, stmt);
            if(cap)
            {
                statements.push_back(
                        new statements::variable(
                                cap->selected_tokens.at(0).get_lit()->to_string()
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
