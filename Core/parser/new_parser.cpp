#include "new_parser.h"

using skiff::tokenizer::token;
using skiff::tokenizer::token_type;
using skiff::statements::statement;

skiff::new_parser::new_parser(vector<skiff::tokenizer::token> tokens)
{
    this->tokens = tokens;
}

void slice_ends(vector<token> * tokens)
{
    tokens->pop_back();
    tokens->erase(tokens->begin());
}
statement * parse_math(skiff::new_parser::split_results results, skiff::statements::math_statement::op opr,
                       bool self_assign)
{
    statement *var = skiff::new_parser(results.match.at(0)).parse_expression();
    statement *math = skiff::new_parser(results.match.at(1)).parse_expression();
    if(self_assign)
    {
        return new skiff::statements::assignment(
                var,
                new skiff::statements::math_statement(var, opr, math));
    }
    else
    {
        return new skiff::statements::math_statement(var, opr, math);
    }
}

statement * parse_compare(skiff::new_parser::split_results results, skiff::statements::comparison::comparison_type typ)
{
    statement * p1 = skiff::new_parser(results.match.at(0)).parse_expression();
    statement * p2 = skiff::new_parser(results.match.at(1)).parse_expression();
    return new skiff::statements::comparison(p1, typ, p2);
}

// Parse top level statement, cannot be contained within an expression
vector<statement *> skiff::new_parser::parse_statement()
{
    using tokenizer::token_type;
    vector<statement *> stmts;
    while (get_current().get_type() != token_type::FILEEND)
    {
        switch (get_current().get_type())
        {
            case token_type::IF:
                stmts.push_back(parse_if());
                break;
            case token_type::WHILE:
                // parse_while()
            case token_type::CLASS:
                stmts.push_back(parse_class());
                break;
            case token_type::DEF:
                stmts.push_back(parse_def());
                break;
            case token_type::RETURN:
            {
                consume(token_type::RETURN);
                new_parser return_parser = new_parser(consume_til(token_type::SEMICOLON));
                stmts.push_back(new statements::return_statement(return_parser.parse_expression()));
                break;
            }
            default: {
                new_parser subparser = new_parser(consume_til(token_type::SEMICOLON));
                stmts.push_back(subparser.parse_expression());
                std::cout << std::endl;
                break;
            }
        }

    }
    return stmts;
}

// Parse an expression, can contain other expressions
statement * skiff::new_parser::parse_expression()
{
    if(tokens.empty())
    {
        return new statement();
    }
    // Split based on the priorities provided (inefficient, yet effective)
    vector<vector<token_type>> precedence = {
            {token_type::EQUAL},
            {token_type::OR},
            {token_type::AND},
            {
                token_type::LESS_THAN_EQUAL, token_type::GREATER_THAN_EQUAL,
                token_type::BANG_EQUAL, token_type::DOUBLE_EQUAL,
                token_type::LEFT_ANGLE_BRACE, token_type::RIGHT_ANGLE_BRACE
            },
            {
                token_type::PLUS_EQUAL, token_type::MINUS_EQUAL,
                token_type::STAR_EQUAL, token_type::DIV_EQUAL,
                token_type::MOD_EQUAL
            },
            {token_type::PLUS, token_type::MINUS},
            {token_type::STAR, token_type::DIV, token_type::MOD},
            {token_type::BIT_AND, token_type::BIT_OR, token_type::BIT_XOR}
    };

    split_results results = precedence_braced_split(precedence);

    // Act upon split results
    if(!results.match.empty())
    {
        switch(results.on)
        {
            // Assign declare
            case token_type::EQUAL:
            {
                split_results name_split = new_parser(results.match.at(0)).braced_split({token_type::COLON}, 1);
                if (name_split.match.size() != 2)
                {
                    statement *name = new_parser(results.match.at(0)).parse_expression();
                    statement *value = new_parser(results.match.at(1)).parse_expression();
                    return new statements::assignment(name, value);
                }
                else
                {
                    string name = name_split.match.at(0).at(0).get_lit().get_value();
                    statements::type_statement typ =
                            statements::type_statement(name_split.match.at(1).at(0).get_lit().get_value());
                    statement *value = new_parser(results.match.at(1)).parse_expression();
                    return new statements::declaration_with_assignment(name, typ, value);
                }
            }
            // Logic joins
            case token_type::OR:
            {
                statement *p1 = new_parser(results.match.at(0)).parse_expression();
                statement *p2 = new_parser(results.match.at(1)).parse_expression();
                return new statements::boolean_conjunction(p1, statements::boolean_conjunction::OR, p2);
            }
            case token_type::AND:
            {
                statement *p1 = new_parser(results.match.at(0)).parse_expression();
                statement *p2 = new_parser(results.match.at(1)).parse_expression();
                return new statements::boolean_conjunction(p1, statements::boolean_conjunction::AND, p2);
            }
            // Compares
            case token_type::LEFT_ANGLE_BRACE: return parse_compare(results, statements::comparison::LESS_THAN);
            case token_type::RIGHT_ANGLE_BRACE: return parse_compare(results, statements::comparison::GREATER_THAN);
            case token_type::LESS_THAN_EQUAL: return parse_compare(results, statements::comparison::LESS_THAN_EQUAL_TO);
            case token_type::GREATER_THAN_EQUAL: return parse_compare(results, statements::comparison::GREATER_THAN_EQUAL_TO);
            case token_type::DOUBLE_EQUAL: return parse_compare(results, statements::comparison::EQUAL);
            case token_type::BANG_EQUAL: return parse_compare(results, statements::comparison::NOT_EQUAL);
            // Self assign
            case token_type::PLUS_EQUAL: return parse_math(results, statements::math_statement::ADD, true);
            case token_type::MINUS_EQUAL: return parse_math(results, statements::math_statement::SUB, true);
            case token_type::STAR_EQUAL: return parse_math(results, statements::math_statement::MUL, true);
            case token_type::DIV_EQUAL: return parse_math(results, statements::math_statement::DIV, true);
            // Math
            case token_type::PLUS: return parse_math(results, statements::math_statement::ADD, false);
            case token_type::MINUS: return parse_math(results, statements::math_statement::SUB, false);
            case token_type::STAR: return parse_math(results, statements::math_statement::MUL, false);
            case token_type::DIV: return parse_math(results, statements::math_statement::DIV, false);
        }
        return nullptr;
    }

    // If not split on anything, test for compound dotted statement
    split_results compound_segments = braced_split({token_type::DOT}, -1);

    if(compound_segments.match.size() != 1)
    {
        vector<statement *> cmp_bits;
        for(vector<token> seg : compound_segments.match)
        {
            cmp_bits.push_back(new_parser(seg).parse_expression());
        }
        return new statements::compound(cmp_bits);
    }

    // If no dots to split on, the expression must be a function call, treat it as such
    if(get_current().get_type() == token_type::NAME && get_next().get_type() == token_type::LEFT_PAREN)
    {
        std::cout << "call to ";
        statements::type_statement ts(get_current().get_lit().get_value());
        consume(token_type::NAME);
        vector<token> parens = consume_parens(token_type::LEFT_PAREN, token_type::RIGHT_PAREN);
        slice_ends(&parens);
        split_results params = new_parser(parens).braced_split({token_type::COMMA}, -1);
        vector<statement *> param_stmts;
        std::cout << " on (";
        for(vector<token> s : params.match)
        {
            param_stmts.push_back(new_parser(s).parse_expression());
            std::cout << " with ";
        }
        std::cout << ") ";
        return new statements::function_call(ts, param_stmts);
    }

    // If nothing else, the expression must be a variable name or literal value
    std::cout << tokenizer::sequencetostring(tokens);
    if(tokens.size() == 1)
    {
        if(get_current().get_type() == token_type::NAME)
        {
            return new statements::variable(get_current().get_lit().get_value());
        }
        if(get_current().get_type() == token_type::LITERAL)
        {
            return new statements::value(get_current().get_lit());
        }
    }
    return new statements::statement(tokenizer::sequencetostring(tokens));
}

// Select next parens, keeping track of other parens so as not to split up statements
vector<skiff::tokenizer::token> skiff::new_parser::consume_parens(tokenizer::token_type leftparen, tokenizer::token_type rightparen)
{
    vector<token> selected;
    stack<token> braces;
    do
    {
        if(get_current().get_type() == tokenizer::token_type::FILEEND)
        {
            // unexpected eof
        }
        selected.push_back(get_current());
        if(get_current().get_type() == leftparen)
        {
            braces.push(get_current());
        }
        if(get_current().get_type() == rightparen)
        {
            braces.pop();
        }
        this->current_token++;
    }
    while(!braces.empty());
    return selected;
}

// Get the currently examined token, or EOF
token skiff::new_parser::get_current()
{
    if(this->current_token >= this->tokens.size())
    {
        return token(token_type::FILEEND, tokenizer::literal(), 0, 0);
    }
    return this->tokens.at(this->current_token);
}

// Look ahead one token
token skiff::new_parser::get_next()
{
    if(this->current_token + 1 >= this->tokens.size())
    {
        return token(token_type::FILEEND, tokenizer::literal(), 0, 0);
    }
    return this->tokens.at(this->current_token + 1);
}


bool is_left_paren(token_type typ)
{
    return typ == token_type::LEFT_PAREN ||
            typ == token_type::LEFT_BRACE ||
            typ == token_type::LEFT_BRACKET; //||
//            typ == token_type::LEFT_ANGLE_BRACE;
}

bool is_right_paren(token_type typ)
{
    return typ == token_type::RIGHT_PAREN ||
           typ == token_type::RIGHT_BRACE ||
           typ == token_type::RIGHT_BRACKET;// ||
//           typ == token_type::RIGHT_ANGLE_BRACE;
}

bool parens_match(token_type typ1, token_type typ2)
{
    return (typ1 == token_type::LEFT_PAREN && typ2 == token_type::RIGHT_PAREN) ||
            (typ1 == token_type::LEFT_BRACE && typ2 == token_type::RIGHT_BRACE) ||
            (typ1 == token_type::LEFT_BRACKET && typ2 == token_type::RIGHT_BRACKET) ||
            (typ1 == token_type::LEFT_ANGLE_BRACE && typ2 == token_type::RIGHT_ANGLE_BRACE);
}

int vector_contains_token_type(vector<token_type> v, token_type t)
{
    for(int i = 0; i < v.size(); i++)
    {
        if(v.at(i) == t)
        {
            return i;
        }
    }
    return -1;
}

// Split up to count times on the given token types
skiff::new_parser::split_results skiff::new_parser::braced_split(vector<token_type> on, int count)
{
    vector<vector<token>> segments;
    vector<token> segment;
    stack<token_type> braces;

    int used_id = -1;
    int check_id;

    // Loop through tokens
    for(size_t i = this->current_token; i < tokens.size(); i++)
    {
        if(is_left_paren(tokens.at(i).get_type()))
        {
            braces.push(tokens.at(i).get_type());
        }
        if(is_right_paren(tokens.at(i).get_type()))
        {
            if(parens_match(braces.top(), tokens.at(i).get_type()))
            {
                braces.pop();
            }
            else
            {
                // Bad brace match
            }
        }
        // Check for search token type
        if((check_id = vector_contains_token_type(on, tokens.at(i).get_type())) != -1 &&
                braces.empty() && (count == -1 || segments.size() < count))
        {
            used_id = check_id;
            segments.push_back(segment);
            segment = vector<token>();
        }
        else
        {
            segment.push_back(tokens.at(i));
        }
    }
    if(segment.size() != 0)
    {
        segments.push_back(segment);
    }

    skiff::new_parser::split_results result;
    result.match = segments;
    if(used_id != -1)
    {
        result.on = on.at(used_id);
    }
    return result;
}

// try to split on any of the given split precedences
skiff::new_parser::split_results skiff::new_parser::precedence_braced_split(vector<vector<token_type>> rankings)
{
    skiff::new_parser::split_results result;
    vector<vector<token>> splt;
    for(vector<token_type> p : rankings)
    {
        result = braced_split(p, 1);
        if(result.match.size() == 2)
        {
            return result;
        }
    }
    return skiff::new_parser::split_results();
}

// advance search slot with expectation
void skiff::new_parser::consume(token_type typ)
{
    if(get_current().get_type() != typ)
    {
        // throw error
    }
    this->current_token++;
}

// advance search slot as many times as needed to locate token
vector<token> skiff::new_parser::consume_til(token_type typ)
{
    vector<token> segment;
    stack<token_type> braces;

    while(get_current().get_type() != token_type::FILEEND)
    {
        if(is_left_paren(get_current().get_type()))
        {
            braces.push(get_current().get_type());
        }
        if(is_right_paren(get_current().get_type()))
        {
            if(parens_match(braces.top(), get_current().get_type()))
            {
                braces.pop();
            }
            else
            {
                // Bad brace match
            }
        }
        if(get_current().get_type() == typ && braces.empty())
        {
            this->current_token++;
            return segment;
        }
        else
        {
            segment.push_back(get_current());
            this->current_token++;
        }
    }
    // unexpected eof
    return vector<token>();
}

// Consume an IF block
statement *skiff::new_parser::parse_if()
{
    consume(token_type::IF);

    vector<token> condition = consume_parens(token_type::LEFT_PAREN, token_type::RIGHT_PAREN);
    slice_ends(&condition);

    vector<token> body = consume_parens(token_type::LEFT_BRACE, token_type::RIGHT_BRACE);
    slice_ends(&body);

    std::cout << "if ";
    statement * stmt_condition = new_parser(condition).parse_expression();
    std::cout << std::endl;
    vector<statement *> stmt_body = new_parser(body).parse_statement();
    return new statements::if_directive(stmt_condition, stmt_body);
}

// Consume a function declaration
statement *skiff::new_parser::parse_def()
{
    using skiff::statements::function_definition;
    consume(token_type::DEF);

    string name = get_current().get_lit().get_value();
    consume(token_type::NAME);

    vector<token> param_list = consume_parens(token_type::LEFT_PAREN, token_type::RIGHT_PAREN);
    slice_ends(&param_list);
    split_results params = new_parser(param_list).braced_split({token_type::COMMA}, -1);

    vector<function_definition::function_parameter> func_params;

    for(vector<token> s : params.match)
    {
        new_parser(s).parse_expression();
        split_results bits = new_parser(s).braced_split({token_type::COLON}, 1);
        func_params.push_back(function_definition::function_parameter(
                bits.match.at(0).at(0).get_lit().get_value(),
                statements::type_statement(bits.match.at(1).at(0).get_lit().get_value())
        ));
    }

    statements::type_statement typ_stmt("None");

    if(get_current().get_type() == token_type::COLON)
    {
        consume(token_type::COLON);
        std::cout << "returns ";
        new_parser({get_current()}).parse_expression();
        typ_stmt = statements::type_statement(get_current().get_lit().get_value());
        consume(token_type::NAME);
    }

    vector<token> body = consume_parens(token_type::LEFT_BRACE, token_type::RIGHT_BRACE);
    slice_ends(&body);
    std::cout << std::endl;

    vector<statement *> stmt_body = new_parser(body).parse_statement();

    return new statements::function_definition(name, func_params, typ_stmt, stmt_body);
}

statement *skiff::new_parser::parse_class()
{
    consume(token_type::CLASS);

    string name = get_current().get_lit().get_value();
    consume(token_type::NAME);

    vector<token> body = consume_parens(token_type::LEFT_BRACE, token_type::RIGHT_BRACE);
    slice_ends(&body);

    vector<statement *> stmt_body = new_parser(body).parse_statement();
    return new statements::class_heading(statements::class_heading::CLASS, name, stmt_body);
}


