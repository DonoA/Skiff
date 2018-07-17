#pragma once
#include <string>
#include <vector>

using std::string;
using std::vector;

namespace skiff {
    namespace tokenizer {
        enum literal_type {
            STRING, SEQUENCE, NUMBER, DECIMAL, 
        };

        enum token_type {

            // SMALL
            COLON, SEMICOLON, COMMA, EQUAL, PLUS, MINUS, STAR, DIV, MOD, EXP, 
            LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, LEFT_BRACKET, RIGHT_BRACKET,
            LEFT_ANGLE_BRACE, RIGHT_ANGLE_BRACE, DOT, ARROW, BACK_SLASH, R, COMMENT_START,
            COMMENT_END, LINE_COMMENT, BIT_AND, BIT_OR, BIT_NOT, BIT_XOR, BIT_LEFT, BIT_RIGHT,
            UNDERSCORE, INC, DEC, PLUS_EQUAL, MINUS_EQUAL, STAR_EQUAL, DIV_EQUAL, MOD_EQUAL,

            // LOGIC
            AND, OR, 
            BANG, BANG_EQUAL, DOUBLE_EQUAL, // less than greater than is the generic syntax
            LESS_THAN_EQUAL, GREATER_THAN_EQUAL, TRU, FALS, NILL,

            LITERAL,

            // KEYWORDS
            STRUCT, PUBLIC, PRIVATE, STATIC, FINAL, MATCH, SWITCH, CASE, NEXT, 
            BREAK, THROW, IF, ELSE, WHILE, FOR, TRY, CATCH, FINALLY, THROWS, RETURN, ANNOTATION,
            NEW, CLASS, DEF, AT, ENUM, SUPER, THIS, IMPORT, NAME,

            FILEEND,
        };

        class literal {
        public:
            literal(literal_type type, void * value);
            string to_string() { return *((string *) literal_value); }
        private:
            literal_type type;
            void * literal_value;
        };
        
        class token {
        public:
            token(token_type type, literal * literal, size_t line, size_t pos);
            token_type get_type() { return type; };
            size_t get_line() { return line; };
            size_t get_col() { return pos; };
            literal * get_lit() { return lit; };
        private:
            token_type type;
            literal * lit;
            size_t line;
            size_t pos;
        };

        vector<token> tokenize(string segment);

        string sequencetostring(vector<token> seq);
    }
}
