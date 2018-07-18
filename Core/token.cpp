#include "token.h"
#include <iostream>

namespace skiff
{
	namespace tokenizer
	{

        literal::literal(literal_type type, void * value)
        {
            this->type = type;
            this->literal_value = value;
        }

        token::token(token_type type, literal * lit, size_t line, size_t pos)
        {
            this->type = type;
            this->lit = lit;
            this->line = line;
            this->pos = pos;
        }

        string consumeTil(string seq, size_t * i, char del)
        {
            size_t start_i = *i;
            (*i)++;
            for(;!(seq.at(*i) == del && seq.at(*i - 1) != '\\') && *i < seq.length(); (*i)++);
            std::cout << *i << " " << start_i << std::endl;
            return seq.substr(start_i + 1, *i - start_i - 1);
        }

        bool isDot(char c)
        {
            return c == '.';
        }

        bool isUnderscore(char c)
        {
            return c == '_';
        }

        bool isChar(char c)
        {
            return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'); 
        }

        bool isDigit(char c)
        {
            return (c >= '0' && c <= '9'); 
        }

        string selectKeyword(string seq, size_t * i) {
            size_t start_i = *i;
            while(isChar(seq.at(*i)) || isDigit(seq.at(*i)) || isUnderscore(seq.at(*i))) (*i)++;
            (*i)--;
            return seq.substr(start_i, (*i + 1) - start_i);
        }

        string selectNumber(string seq, size_t * i) {
            size_t start_i = *i;
            while(isDigit(seq.at(*i)) || isDot(seq.at(*i))) (*i)++;
            (*i)--;
            return seq.substr(start_i, (*i + 1) - start_i);
        }

        vector<token> tokenize(string segment)
        {
            vector<token> tokens;
            size_t line_id = 1;
            size_t col = 0;
            size_t total_col = 0;
            for(size_t i = 0; i < segment.length(); i++)
            {
                col = i - total_col ;
                char self = segment.at(i);
                char next = '\0';
                if(i + 1 < segment.length())
                {
                    next = segment.at(i + 1);
                }

                switch(self)
                {
                    case ':': tokens.push_back(token(token_type::COLON, nullptr, line_id, col)); break;
                    case ';': tokens.push_back(token(token_type::SEMICOLON, nullptr, line_id, col)); break;
                    case ',': tokens.push_back(token(token_type::COMMA, nullptr, line_id, col)); break;
                    case '(': tokens.push_back(token(token_type::LEFT_PAREN, nullptr, line_id, col)); break;
                    case ')': tokens.push_back(token(token_type::RIGHT_PAREN, nullptr, line_id, col)); break;
                    case '{': tokens.push_back(token(token_type::LEFT_BRACE, nullptr, line_id, col)); break;
                    case '}': tokens.push_back(token(token_type::RIGHT_BRACE, nullptr, line_id, col)); break;
                    case '[': tokens.push_back(token(token_type::LEFT_BRACKET, nullptr, line_id, col)); break;
                    case ']': tokens.push_back(token(token_type::RIGHT_BRACKET, nullptr, line_id, col)); break;
                    case '.': tokens.push_back(token(token_type::DOT, nullptr, line_id, col)); break;
                    
                    case ' ':
                    case '\r':
                    case '\t':
                        break;
                    
                    case '\n': {
                        line_id++;
                        total_col = i;
                        break;
                    }

                    case '=': {
                        switch(next)
                        {
                            case '=': {
                                tokens.push_back(token(token_type::DOUBLE_EQUAL, nullptr, line_id, col));
                                i++;
                                break;
                            }
                            case '>': {
                                tokens.push_back(token(token_type::ARROW, nullptr, line_id, col));
                                i++;
                                break;
                            }
                            default: {
                                tokens.push_back(token(token_type::EQUAL, nullptr, line_id, col));
                                break;
                            }
                        }
                        break;
                    }

                    case '+': {
                        switch(next)
                        {
                            case '=': {
                                tokens.push_back(token(token_type::PLUS_EQUAL, nullptr, line_id, col));
                                i++;
                                break;
                            }
                            case '+': {
                                tokens.push_back(token(token_type::INC, nullptr, line_id, col));
                                i++;
                                break;
                            }
                            default: {
                                tokens.push_back(token(token_type::PLUS, nullptr, line_id, col));
                                break;
                            }
                        }
                        break;
                    }

                    case '-': {
                        switch(next)
                        {
                            case '=': {
                                tokens.push_back(token(token_type::MINUS_EQUAL, nullptr, line_id, col));
                                i++;
                                break;
                            }
                            case '-': {
                                tokens.push_back(token(token_type::DEC, nullptr, line_id, col));
                                i++;
                                break;
                            }
                            default: {
                                tokens.push_back(token(token_type::MINUS, nullptr, line_id, col));
                                break;
                            }
                        }
                        break;
                    }

                    case '*': {
                        switch(next)
                        {
                            case '=': {
                                tokens.push_back(token(token_type::STAR_EQUAL, nullptr, line_id, col));
                                i++;
                                break;
                            }
                            case '*': {
                                tokens.push_back(token(token_type::EXP, nullptr, line_id, col));
                                i++;
                                break;
                            }
                            case '/': {
                                tokens.push_back(token(token_type::COMMENT_END, nullptr, line_id, col));
                                i++;
                                break;
                            }
                            default: {
                                tokens.push_back(token(token_type::STAR, nullptr, line_id, col));
                                break;
                            }
                        }
                        break;
                    }

                    case '/': {
                        switch(next)
                        {
                            case '=': {
                                tokens.push_back(token(token_type::DIV_EQUAL, nullptr, line_id, col));
                                i++;
                                break;
                            }
                            case '/': {
                                i++;
                                string * comment = new string(consumeTil(segment, &i, '\n'));
                                i--;
                                tokens.push_back(token(
                                    token_type::LINE_COMMENT, 
                                    new literal(literal_type::STRING, comment),
                                    line_id, 
                                    col));
                                break;
                            }
                            case '*': {
                                tokens.push_back(token(token_type::COMMENT_START, nullptr, line_id, col));
                                i++;
                                break;
                            }
                            default: {
                                tokens.push_back(token(token_type::DIV, nullptr, line_id, col));
                                break;
                            }
                        }
                        break;
                    }

                    case '%': {
                        switch(next)
                        {
                            case '=': {
                                tokens.push_back(token(token_type::MOD_EQUAL, nullptr, line_id, col));
                                i++;
                                break;
                            }
                            default: {
                                tokens.push_back(token(token_type::MOD, nullptr, line_id, col));
                                break;
                            }
                        }
                        break;
                    }

                    case 'r': {
                        switch(next)
                        {
                            case '/': {
                                tokens.push_back(token(token_type::R, nullptr, line_id, col));
                                i++;
                                break;
                            }
                        }
                        break;
                    }

                    case '&': {
                        switch(next)
                        {
                            case '&': {
                                tokens.push_back(token(token_type::AND, nullptr, line_id, col));
                                i++;
                                break;
                            }
                            default: {
                                tokens.push_back(token(token_type::BIT_AND, nullptr, line_id, col));
                                break;
                            }
                        }
                        break;
                    }

                    case '|': {
                        switch(next)
                        {
                            case '|': {
                                tokens.push_back(token(token_type::OR, nullptr, line_id, col));
                                i++;
                                break;
                            }
                            default: {
                                tokens.push_back(token(token_type::BIT_OR, nullptr, line_id, col));
                                break;
                            }
                        }
                        break;
                    }

                    case '^': {
                        tokens.push_back(token(token_type::BIT_XOR, nullptr, line_id, col));
                        break;
                    }

                    case '>': {
                        switch(next)
                        {
                            case '>': {
                                tokens.push_back(token(token_type::BIT_RIGHT, nullptr, line_id, col));
                                i++;
                                break;
                            }
                            case '=': {
                                tokens.push_back(token(token_type::GREATER_THAN_EQUAL, nullptr, line_id, col));
                                i++;
                                break;
                            }
                            default: {
                                tokens.push_back(token(token_type::RIGHT_ANGLE_BRACE, nullptr, line_id, col));
                                break;
                            }
                        }
                        break;
                    }

                    case '<': {
                        switch(next)
                        {
                            case '<': {
                                tokens.push_back(token(token_type::BIT_LEFT, nullptr, line_id, col));
                                i++;
                                break;
                            }
                            case '=': {
                                tokens.push_back(token(token_type::LESS_THAN_EQUAL, nullptr, line_id, col));
                                i++;
                                break;
                            }
                            default: {
                                tokens.push_back(token(token_type::LEFT_ANGLE_BRACE, nullptr, line_id, col));
                                break;
                            }
                        }
                        break;
                    }

                    case '!': {
                        switch(next)
                        {
                            case '=': {
                                tokens.push_back(token(token_type::BANG_EQUAL, nullptr, line_id, col));
                                i++;
                                break;
                            }
                            default: {
                                tokens.push_back(token(token_type::BANG, nullptr, line_id, col));
                                break;
                            }
                        }
                        break;
                    }

                    case '@': {
                        tokens.push_back(token(token_type::AT, nullptr, line_id, col));
                        break;
                    }

                    case '"': {
                        string * str = new string(consumeTil(segment, &i, '"'));
                        std::cout << "proc str " << *str << std::endl;
                        tokens.push_back(token(
                            token_type::LITERAL, 
                            new literal(literal_type::STRING, str),
                            line_id, 
                            col));
                        break;
                    }

                    case '\'': {
                        string * str = new string(consumeTil(segment, &i, '\''));
                        tokens.push_back(token(
                            token_type::LITERAL, 
                            new literal(literal_type::SEQUENCE, str),
                            line_id, 
                            col));
                        break;
                    }

                    default: {
                        if(isUnderscore(self) || isChar(self))
                        {
                            size_t start_i = i;

                            string kw = selectKeyword(segment, &i);

                            if(kw == "struct")
                            {
                                tokens.push_back(token(token_type::STRUCT, nullptr, line_id, start_i));
                            }
                            else if(kw == "public")
                            {
                                tokens.push_back(token(token_type::PUBLIC, nullptr, line_id, start_i));
                            }
                            else if(kw == "private")
                            {
                                tokens.push_back(token(token_type::PRIVATE, nullptr, line_id, start_i));
                            }
                            else if(kw == "static")
                            {
                                tokens.push_back(token(token_type::STATIC, nullptr, line_id, start_i));
                            }
                            else if(kw == "final")
                            {
                                tokens.push_back(token(token_type::FINAL, nullptr, line_id, start_i));
                            }
                            else if(kw == "match")
                            {
                                tokens.push_back(token(token_type::MATCH, nullptr, line_id, start_i));
                            }
                            else if(kw == "switch")
                            {
                                tokens.push_back(token(token_type::SWITCH, nullptr, line_id, start_i));
                            }
                            else if(kw == "case")
                            {
                                tokens.push_back(token(token_type::CASE, nullptr, line_id, start_i));
                            }
                            else if(kw == "next")
                            {
                                tokens.push_back(token(token_type::NEXT, nullptr, line_id, start_i));
                            }
                            else if(kw == "break")
                            {
                                tokens.push_back(token(token_type::BREAK, nullptr, line_id, start_i));
                            }
                            else if(kw == "throw")
                            {
                                tokens.push_back(token(token_type::THROW, nullptr, line_id, start_i));
                            }
                            else if(kw == "if")
                            {
                                tokens.push_back(token(token_type::IF, nullptr, line_id, start_i));
                            }
                            else if(kw == "while")
                            {
                                tokens.push_back(token(token_type::WHILE, nullptr, line_id, start_i));
                            }
                            else if(kw == "for")
                            {
                                tokens.push_back(token(token_type::FOR, nullptr, line_id, start_i));
                            }
                            else if(kw == "try")
                            {
                                tokens.push_back(token(token_type::TRY, nullptr, line_id, start_i));
                            }
                            else if(kw == "catch")
                            {
                                tokens.push_back(token(token_type::CATCH, nullptr, line_id, start_i));
                            }
                            else if(kw == "finally")
                            {
                                tokens.push_back(token(token_type::FINALLY, nullptr, line_id, start_i));
                            }
                            else if(kw == "throws")
                            {
                                tokens.push_back(token(token_type::THROWS, nullptr, line_id, start_i));
                            }
                            else if(kw == "return")
                            {
                                tokens.push_back(token(token_type::RETURN, nullptr, line_id, start_i));
                            }
                            else if(kw == "annotation")
                            {
                                tokens.push_back(token(token_type::ANNOTATION, nullptr, line_id, start_i));
                            }
                            else if(kw == "new")
                            {
                                tokens.push_back(token(token_type::NEW, nullptr, line_id, start_i));
                            }
                            else if(kw == "class")
                            {
                                tokens.push_back(token(token_type::CLASS, nullptr, line_id, start_i));
                            }
                            else if(kw == "def")
                            {
                                tokens.push_back(token(token_type::DEF, nullptr, line_id, start_i));
                            }
                            else if(kw == "enum")
                            {
                                tokens.push_back(token(token_type::ENUM, nullptr, line_id, start_i));
                            }
                            else if(kw == "super")
                            {
                                tokens.push_back(token(token_type::SUPER, nullptr, line_id, start_i));
                            }
                            else if(kw == "this")
                            {
                                tokens.push_back(token(token_type::THIS, nullptr, line_id, start_i));
                            }
                            else if(kw == "import")
                            {
                                tokens.push_back(token(token_type::IMPORT, nullptr, line_id, start_i));
                            }
                            else
                            {
                                tokens.push_back(token(token_type::NAME, 
                                    new literal(literal_type::STRING, new string(kw)), 
                                    line_id, 
                                    start_i));
                            }
                        }
                        else if(isDigit(self))
                        {
                            size_t start_i = i;
                            string num = selectNumber(segment, &i);
                            tokens.push_back(token(token_type::LITERAL, 
                                    new literal(literal_type::STRING, new string(num)), 
                                    line_id, 
                                    start_i));
                        }
                        else
                        {
                            std::cout << "Reached word matcher" << std::endl;
                        }
                        break;
                    }
                }
            }
			tokens.push_back(token(tokenizer::token_type::FILEEND, nullptr, line_id, 0));
            return tokens;
        }

        string sequencetostring(vector<token> seq)
        {
            for(token t : seq)
            {
                std::cout << "Line " << t.get_line() << " ~> " << (int) t.get_type() << std::endl;
                if(t.get_type() == token_type::LITERAL || t.get_type() == token_type::LINE_COMMENT)
                {
                    std::cout << t.get_lit()->to_string() << std::endl;
                }
            }
            return "";
        }
    }
}