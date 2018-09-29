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
                // parse_class()
                break;
            case token_type::DEF:
                stmts.push_back(parse_def());
                break;
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

statement * skiff::new_parser::parse_expression()
{
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

    if(results.match.size() != 0)
    {
        switch(results.on)
        {
            case token_type::EQUAL:
                std::cout << "assign ";
                new_parser(results.match.at(0)).parse_expression();
                std::cout << " to ";
                new_parser(results.match.at(1)).parse_expression();
                break;

            case token_type::OR:
                new_parser(results.match.at(0)).parse_expression();
                std::cout << " or ";
                new_parser(results.match.at(1)).parse_expression();
                break;

            case token_type::AND:
                new_parser(results.match.at(0)).parse_expression();
                std::cout << " and ";
                new_parser(results.match.at(1)).parse_expression();
                break;

            case token_type::LEFT_ANGLE_BRACE:
                new_parser(results.match.at(0)).parse_expression();
                std::cout << " less than ";
                new_parser(results.match.at(1)).parse_expression();
                break;

            case token_type::RIGHT_ANGLE_BRACE:
                new_parser(results.match.at(0)).parse_expression();
                std::cout << " greater than ";
                new_parser(results.match.at(1)).parse_expression();
                break;

            case token_type::DOUBLE_EQUAL:
                new_parser(results.match.at(0)).parse_expression();
                std::cout << " is equal to ";
                new_parser(results.match.at(1)).parse_expression();
                break;
        }
        return nullptr;
    }

    if(get_current().get_type() == token_type::NAME && get_next().get_type() == token_type::LEFT_PAREN)
    {
        std::cout << "call to ";
        new_parser({get_current()}).parse_expression();
        consume(token_type::NAME);
        vector<token> parens = consume_parens(token_type::LEFT_PAREN, token_type::RIGHT_PAREN);
        slice_ends(&parens);
        split_results params = new_parser(parens).braced_split({token_type::COMMA}, -1);
        std::cout << " on ";
        for(vector<token> s : params.match)
        {
            new_parser(s).parse_expression();
            std::cout << " with ";
        }
        return nullptr;
    }

    std::cout << tokenizer::sequencetostring(tokens);
}

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

token skiff::new_parser::get_current()
{
    if(this->current_token >= this->tokens.size())
    {
        return token(token_type::FILEEND, tokenizer::literal(), 0, 0);
    }
    return this->tokens.at(this->current_token);
}

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

skiff::new_parser::split_results skiff::new_parser::braced_split(vector<token_type> on, int count)
{
    vector<vector<token>> segments;
    vector<token> segment;
    stack<token_type> braces;

    int used_id = -1;
    int check_id;

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
    segments.push_back(segment);

    skiff::new_parser::split_results result;
    result.match = segments;
    if(used_id != -1)
    {
        result.on = on.at(used_id);
    }
    return result;
}

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

void skiff::new_parser::consume(token_type typ)
{
    if(get_current().get_type() != typ)
    {
        // throw error
    }
    this->current_token++;
}

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
}

statement *skiff::new_parser::parse_def()
{
    consume(token_type::DEF);

    std::cout << "def ";
    new_parser({get_current()}).parse_expression();
    consume(token_type::NAME);

    std::cout << " parameterized by ";
    vector<token> param_list = consume_parens(token_type::LEFT_PAREN, token_type::RIGHT_PAREN);
    slice_ends(&param_list);
    split_results params = new_parser(param_list).braced_split({token_type::COMMA}, -1);

    for(vector<token> s : params.match)
    {
        new_parser(s).parse_expression();
        std::cout << " with ";
    }

    if(get_current().get_type() == token_type::COLON)
    {
        consume(token_type::COLON);
        std::cout << "returns ";
        new_parser({get_current()}).parse_expression();
        consume(token_type::NAME);
    }

    vector<token> body = consume_parens(token_type::LEFT_BRACE, token_type::RIGHT_BRACE);
    slice_ends(&body);
    std::cout << std::endl;

    vector<statement *> stmt_body = new_parser(body).parse_statement();
}


