#include "token.h"
#include <iostream>

namespace skiff
{
    namespace tokenizer
    {

        literal::literal(literal_type type, string value)
        {
            this->type = type;
            this->literal_value = value;
        }

        token::token(token_type type, literal lit, size_t line, size_t pos)
        {
            this->lit = lit;
            this->type = type;
            this->line = line;
            this->pos = pos;
        }

        bool token::operator==(const token &other) const
        {
            bool same_type = this->type == other.type;
            bool same_lit = true;
            if (this->lit.get_type() == literal_type::EMPTY)
            {
                if (other.lit.get_type() == literal_type::EMPTY)
                {
                    same_lit = false;
                }
                else
                {
                    same_lit = other.lit.get_value() == this->lit.get_value();
                }
            }
            return same_lit && same_type;
        }

        string consumeTil(string seq, size_t *i, char del)
        {
            size_t start_i = *i;
            (*i)++;
            for (; !(seq.at(*i) == del && seq.at(*i - 1) != '\\') && *i < seq.length(); (*i)++) { }
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

        string selectKeyword(string seq, size_t *i)
        {
            size_t start_i = *i;
            while (*i < seq.length() &&
                   (isChar(seq.at(*i)) || isDigit(seq.at(*i)) || isUnderscore(seq.at(*i))))
            {
                (*i)++;
            }
            (*i)--;
            return seq.substr(start_i, (*i + 1) - start_i);
        }

        string selectNumber(string seq, size_t *i)
        {
            size_t start_i = *i;
            while (*i < seq.length() &&
                   (isDigit(seq.at(*i)) || isDot(seq.at(*i))))
            {
                (*i)++;
            }
            (*i)--;
            return seq.substr(start_i, (*i + 1) - start_i);
        }

        vector<token> tokenize(string segment)
        {
            vector<token> tokens;
            size_t line_id = 1;
            size_t col = 0;
            size_t total_col = 0;
            for (size_t i = 0; i < segment.length(); i++)
            {
                col = i - total_col;
                char self = segment.at(i);
                char next = '\0';
                if (i + 1 < segment.length())
                {
                    next = segment.at(i + 1);
                }

                switch (self)
                {
                    case ':':tokens.push_back(token(token_type::COLON, literal(), line_id, col));
                        break;
                    case ';':tokens.push_back(token(token_type::SEMICOLON, literal(), line_id, col));
                        break;
                    case ',':tokens.push_back(token(token_type::COMMA, literal(), line_id, col));
                        break;
                    case '(':tokens.push_back(token(token_type::LEFT_PAREN, literal(), line_id, col));
                        break;
                    case ')':tokens.push_back(token(token_type::RIGHT_PAREN, literal(), line_id, col));
                        break;
                    case '{':tokens.push_back(token(token_type::LEFT_BRACE, literal(), line_id, col));
                        break;
                    case '}':tokens.push_back(token(token_type::RIGHT_BRACE, literal(), line_id, col));
                        break;
                    case '[':tokens.push_back(token(token_type::LEFT_BRACKET, literal(), line_id, col));
                        break;
                    case ']':tokens.push_back(token(token_type::RIGHT_BRACKET, literal(), line_id, col));
                        break;
                    case '~':tokens.push_back(token(token_type::BIT_NOT, literal(), line_id, col));
                        break;
                    case '.':tokens.push_back(token(token_type::DOT, literal(), line_id, col));
                        break;

                    case ' ':
                    case '\r':
                    case '\t':break;

                    case '\n':
                    {
                        line_id++;
                        total_col = i;
                        break;
                    }

                    case '=':
                    {
                        switch (next)
                        {
                            case '=':
                            {
                                tokens.push_back(token(token_type::DOUBLE_EQUAL, literal(), line_id, col));
                                i++;
                                break;
                            }
                            case '>':
                            {
                                tokens.push_back(token(token_type::ARROW, literal(), line_id, col));
                                i++;
                                break;
                            }
                            default:
                            {
                                tokens.push_back(token(token_type::EQUAL, literal(), line_id, col));
                                break;
                            }
                        }
                        break;
                    }

                    case '+':
                    {
                        switch (next)
                        {
                            case '=':
                            {
                                tokens.push_back(token(token_type::PLUS_EQUAL, literal(), line_id, col));
                                i++;
                                break;
                            }
                            case '+':
                            {
                                tokens.push_back(token(token_type::INC, literal(), line_id, col));
                                i++;
                                break;
                            }
                            default:
                            {
                                tokens.push_back(token(token_type::PLUS, literal(), line_id, col));
                                break;
                            }
                        }
                        break;
                    }

                    case '-':
                    {
                        switch (next)
                        {
                            case '=':
                            {
                                tokens.push_back(token(token_type::MINUS_EQUAL, literal(), line_id, col));
                                i++;
                                break;
                            }
                            case '-':
                            {
                                tokens.push_back(token(token_type::DEC, literal(), line_id, col));
                                i++;
                                break;
                            }
                            default:
                            {
                                tokens.push_back(token(token_type::MINUS, literal(), line_id, col));
                                break;
                            }
                        }
                        break;
                    }

                    case '*':
                    {
                        switch (next)
                        {
                            case '=':
                            {
                                tokens.push_back(token(token_type::STAR_EQUAL, literal(), line_id, col));
                                i++;
                                break;
                            }
                            case '*':
                            {
                                tokens.push_back(token(token_type::EXP, literal(), line_id, col));
                                i++;
                                break;
                            }
                            case '/':
                            {
                                tokens.push_back(token(token_type::COMMENT_END, literal(), line_id, col));
                                i++;
                                break;
                            }
                            default:
                            {
                                tokens.push_back(token(token_type::STAR, literal(), line_id, col));
                                break;
                            }
                        }
                        break;
                    }

                    case '/':
                    {
                        switch (next)
                        {
                            case '=':
                            {
                                tokens.push_back(token(token_type::DIV_EQUAL, literal(), line_id, col));
                                i++;
                                break;
                            }
                            case '/':
                            {
                                i++;
                                string comment = consumeTil(segment, &i, '\n');
                                i--;
                                tokens.push_back(token(
                                        token_type::LINE_COMMENT,
                                        literal(literal_type::STRING, comment),
                                        line_id,
                                        col));
                                break;
                            }
                            case '*':
                            {
                                tokens.push_back(token(token_type::COMMENT_START, literal(), line_id, col));
                                i++;
                                break;
                            }
                            default:
                            {
                                tokens.push_back(token(token_type::DIV, literal(), line_id, col));
                                break;
                            }
                        }
                        break;
                    }

                    case '%':
                    {
                        switch (next)
                        {
                            case '=':
                            {
                                tokens.push_back(token(token_type::MOD_EQUAL, literal(), line_id, col));
                                i++;
                                break;
                            }
                            default:
                            {
                                tokens.push_back(token(token_type::MOD, literal(), line_id, col));
                                break;
                            }
                        }
                        break;
                    }

                    case '&':
                    {
                        switch (next)
                        {
                            case '&':
                            {
                                tokens.push_back(token(token_type::AND, literal(), line_id, col));
                                i++;
                                break;
                            }
                            default:
                            {
                                tokens.push_back(token(token_type::BIT_AND, literal(), line_id, col));
                                break;
                            }
                        }
                        break;
                    }

                    case '|':
                    {
                        switch (next)
                        {
                            case '|':
                            {
                                tokens.push_back(token(token_type::OR, literal(), line_id, col));
                                i++;
                                break;
                            }
                            default:
                            {
                                tokens.push_back(token(token_type::BIT_OR, literal(), line_id, col));
                                break;
                            }
                        }
                        break;
                    }

                    case '^':
                    {
                        tokens.push_back(token(token_type::BIT_XOR, literal(), line_id, col));
                        break;
                    }

                    case '>':
                    {
                        switch (next)
                        {
//                            case '>': {
//                                tokens.push_back(token(token_type::BIT_RIGHT, literal(), line_id, col));
//                                i++;
//                                break;
//                            }
                            case '=':
                            {
                                tokens.push_back(token(token_type::GREATER_THAN_EQUAL, literal(), line_id, col));
                                i++;
                                break;
                            }
                            default:
                            {
                                tokens.push_back(token(token_type::RIGHT_ANGLE_BRACE, literal(), line_id, col));
                                break;
                            }
                        }
                        break;
                    }

                    case '<':
                    {
                        switch (next)
                        {
//                            case '<': {
//                                tokens.push_back(token(token_type::BIT_LEFT, literal(), line_id, col));
//                                i++;
//                                break;
//                            }
                            case '=':
                            {
                                tokens.push_back(token(token_type::LESS_THAN_EQUAL, literal(), line_id, col));
                                i++;
                                break;
                            }
                            default:
                            {
                                tokens.push_back(token(token_type::LEFT_ANGLE_BRACE, literal(), line_id, col));
                                break;
                            }
                        }
                        break;
                    }

                    case '!':
                    {
                        switch (next)
                        {
                            case '=':
                            {
                                tokens.push_back(token(token_type::BANG_EQUAL, literal(), line_id, col));
                                i++;
                                break;
                            }
                            default:
                            {
                                tokens.push_back(token(token_type::BANG, literal(), line_id, col));
                                break;
                            }
                        }
                        break;
                    }

                    case '@':
                    {
                        tokens.push_back(token(token_type::AT, literal(), line_id, col));
                        break;
                    }

                    case '"':
                    {
                        string str = consumeTil(segment, &i, '"');
                        tokens.push_back(token(
                                token_type::LITERAL,
                                literal(literal_type::STRING, str),
                                line_id,
                                col));
                        break;
                    }

                    case '\'':
                    {
                        string str = consumeTil(segment, &i, '\'');
                        tokens.push_back(token(
                                token_type::LITERAL,
                                literal(literal_type::SEQUENCE, str),
                                line_id,
                                col));
                        break;
                    }

                    default:
                    {
                        if (isUnderscore(self) || isChar(self))
                        {
                            size_t start_i = i;

                            string kw = selectKeyword(segment, &i);

                            if (kw == "struct")
                            {
                                tokens.push_back(token(token_type::STRUCT, literal(), line_id, start_i));
                            }
                            else if (kw == "public")
                            {
                                tokens.push_back(token(token_type::PUBLIC, literal(), line_id, start_i));
                            }
                            else if (kw == "private")
                            {
                                tokens.push_back(token(token_type::PRIVATE, literal(), line_id, start_i));
                            }
                            else if (kw == "static")
                            {
                                tokens.push_back(token(token_type::STATIC, literal(), line_id, start_i));
                            }
                            else if (kw == "final")
                            {
                                tokens.push_back(token(token_type::FINAL, literal(), line_id, start_i));
                            }
                            else if (kw == "match")
                            {
                                tokens.push_back(token(token_type::MATCH, literal(), line_id, start_i));
                            }
                            else if (kw == "switch")
                            {
                                tokens.push_back(token(token_type::SWITCH, literal(), line_id, start_i));
                            }
                            else if (kw == "case")
                            {
                                tokens.push_back(token(token_type::CASE, literal(), line_id, start_i));
                            }
                            else if (kw == "next")
                            {
                                tokens.push_back(token(token_type::NEXT, literal(), line_id, start_i));
                            }
                            else if (kw == "break")
                            {
                                tokens.push_back(token(token_type::BREAK, literal(), line_id, start_i));
                            }
                            else if (kw == "throw")
                            {
                                tokens.push_back(token(token_type::THROW, literal(), line_id, start_i));
                            }
                            else if (kw == "if")
                            {
                                tokens.push_back(token(token_type::IF, literal(), line_id, start_i));
                            }
                            else if (kw == "else")
                            {
                                tokens.push_back(token(token_type::ELSE, literal(), line_id, start_i));
                            }
                            else if (kw == "while")
                            {
                                tokens.push_back(token(token_type::WHILE, literal(), line_id, start_i));
                            }
                            else if (kw == "for")
                            {
                                tokens.push_back(token(token_type::FOR, literal(), line_id, start_i));
                            }
                            else if (kw == "try")
                            {
                                tokens.push_back(token(token_type::TRY, literal(), line_id, start_i));
                            }
                            else if (kw == "catch")
                            {
                                tokens.push_back(token(token_type::CATCH, literal(), line_id, start_i));
                            }
                            else if (kw == "finally")
                            {
                                tokens.push_back(token(token_type::FINALLY, literal(), line_id, start_i));
                            }
                            else if (kw == "throws")
                            {
                                tokens.push_back(token(token_type::THROWS, literal(), line_id, start_i));
                            }
                            else if (kw == "return")
                            {
                                tokens.push_back(token(token_type::RETURN, literal(), line_id, start_i));
                            }
                            else if (kw == "annotation")
                            {
                                tokens.push_back(token(token_type::ANNOTATION, literal(), line_id, start_i));
                            }
                            else if (kw == "new")
                            {
                                tokens.push_back(token(token_type::NEW, literal(), line_id, start_i));
                            }
                            else if (kw == "class")
                            {
                                tokens.push_back(token(token_type::CLASS, literal(), line_id, start_i));
                            }
                            else if (kw == "def")
                            {
                                tokens.push_back(token(token_type::DEF, literal(), line_id, start_i));
                            }
                            else if (kw == "enum")
                            {
                                tokens.push_back(token(token_type::ENUM, literal(), line_id, start_i));
                            }
                            else if (kw == "super")
                            {
                                tokens.push_back(token(token_type::SUPER, literal(), line_id, start_i));
                            }
                            else if (kw == "this")
                            {
                                tokens.push_back(token(token_type::THIS, literal(), line_id, start_i));
                            }
                            else if (kw == "import")
                            {
                                tokens.push_back(token(token_type::IMPORT, literal(), line_id, start_i));
                            }
                            else if (kw == "true")
                            {
                                tokens.push_back(token(token_type::TRU, literal(), line_id, start_i));
                            }
                            else if (kw == "false")
                            {
                                tokens.push_back(token(token_type::FALS, literal(), line_id, start_i));
                            }
                            else if (kw == "none")
                            {
                                tokens.push_back(token(token_type::NONE, literal(), line_id, start_i));
                            }
                            else if (kw == "extern")
                            {
                                tokens.push_back(token(token_type::EXTERN, literal(), line_id, start_i));
                            }
                            else if (kw == "r")
                            {
                                tokens.push_back(token(token_type::R, literal(), line_id, start_i));
                            }
                            else
                            {
                                tokens.push_back(token(token_type::NAME,
                                                       literal(literal_type::STRING, kw),
                                                       line_id,
                                                       start_i));
                            }
                        }
                        else if (isDigit(self))
                        {
                            size_t start_i = i;
                            string num = selectNumber(segment, &i);
                            tokens.push_back(token(token_type::LITERAL,
                                                   literal(literal_type::NUMBER, num),
                                                   line_id,
                                                   start_i));
                        }
                        else
                        {
//                            std::cout << "Reached word matcher" << std::endl;
                        }
                        break;
                    }
                }
            }
            tokens.push_back(token(tokenizer::token_type::FILEEND, literal(), line_id, 0));
            return tokens;
        }

        string sequencetostring(vector<token> seq)
        {
            string rtn = "";
            for (token t : seq)
            {
                rtn += std::to_string((int) t.get_type()) + "(";
                if (t.get_lit().get_type() == literal_type::EMPTY)
                {
                    rtn += ")";
                }
                else
                {
                    rtn += t.get_lit().get_value() + ")";
                }
            }
            return rtn;
        }
    }
}