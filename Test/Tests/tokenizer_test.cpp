#include "../../Core/token.h"
#include "../test_util.h"
#include <string>
#include <vector>
#include <queue>
#include <iostream>

using std::string;
using std::vector;
using std::queue;

using skiff::tokenizer::tokenize;
using skiff::tokenizer::sequencetostring;
using skiff::tokenizer::token;
using skiff::tokenizer::literal;
using skiff::tokenizer::token_type;
using skiff::tokenizer::literal_type ;

namespace Test
{
    TEST_CLASS(Tokenizing, 2)
    {
        void run_basic_token_test(string str, token_type typ, string * lit)
        {
            vector<token> tkns = vector<token>();
            if(lit == nullptr)
            {
                tkns.push_back(token(typ, nullptr, 0, 0));
            }
            else
            {
                tkns.push_back(token(typ, new literal(literal_type::STRING, lit), 0, 0));
            }
            tkns.push_back(token(token_type::FILEEND, nullptr, 0, 0));
            Assert::AreEqual(sequencetostring(tokenize(str)), sequencetostring(tkns));
        }

        TEST_METHOD(Colon)
        {
            run_basic_token_test(":", token_type::COLON, nullptr);
        }

        TEST_METHOD(Semicolon)
        {
            run_basic_token_test(";", token_type::SEMICOLON, nullptr);
        }

        TEST_METHOD(Comma)
        {
            run_basic_token_test(",", token_type::COMMA, nullptr);
        }

        TEST_METHOD(LeftParen)
        {
            run_basic_token_test("(", token_type::LEFT_PAREN, nullptr);
        }

        TEST_METHOD(RightParen)
        {
            run_basic_token_test(")", token_type::RIGHT_PAREN, nullptr);
        }

        TEST_METHOD(LeftBrace)
        {
            run_basic_token_test("{", token_type::LEFT_BRACE, nullptr);
        }

        TEST_METHOD(RightBrace)
        {
            run_basic_token_test("}", token_type::RIGHT_BRACE, nullptr);
        }

        TEST_METHOD(LeftSquareBracket)
        {
            run_basic_token_test("[", token_type::LEFT_BRACKET, nullptr);
        }

        TEST_METHOD(RightSquareBracket)
        {
            run_basic_token_test("]", token_type::RIGHT_BRACKET, nullptr);
        }

        TEST_METHOD(Dot)
        {
            run_basic_token_test(".", token_type::DOT, nullptr);
        }

        TEST_METHOD(Tilde)
        {
            run_basic_token_test("~", token_type::BIT_NOT, nullptr);
        }

        TEST_METHOD(Equal)
        {
            run_basic_token_test("=", token_type::EQUAL, nullptr);
        }

        TEST_METHOD(DoubleEqual)
        {
            run_basic_token_test("==", token_type::DOUBLE_EQUAL, nullptr);
        }

        TEST_METHOD(EqualRightAngleBrace)
        {
            run_basic_token_test("=>", token_type::ARROW, nullptr);
        }

        TEST_METHOD(Plus)
        {
            run_basic_token_test("+", token_type::PLUS, nullptr);
        }

        TEST_METHOD(PlusEqual)
        {
            run_basic_token_test("+=", token_type::PLUS_EQUAL, nullptr);
        }

        TEST_METHOD(PlusPlus)
        {
            run_basic_token_test("++", token_type::INC, nullptr);
        }

        TEST_METHOD(Dash)
        {
            run_basic_token_test("-", token_type::MINUS, nullptr);
        }

        TEST_METHOD(DashEqual)
        {
            run_basic_token_test("-=", token_type::MINUS_EQUAL, nullptr);
        }

        TEST_METHOD(DashDash)
        {
            run_basic_token_test("--", token_type::DEC, nullptr);
        }

        TEST_METHOD(Star)
        {
            run_basic_token_test("*", token_type::STAR, nullptr);
        }

        TEST_METHOD(StarEqual)
        {
            run_basic_token_test("*=", token_type::STAR_EQUAL, nullptr);
        }

        TEST_METHOD(StarStar)
        {
            run_basic_token_test("**", token_type::EXP, nullptr);
        }

        TEST_METHOD(StarForwardSlash)
        {
            run_basic_token_test("*/", token_type::COMMENT_END, nullptr);
        }

        TEST_METHOD(ForwardSlash)
        {
            run_basic_token_test("/", token_type::DIV, nullptr);
        }

        TEST_METHOD(ForwardSlashEqual)
        {
            run_basic_token_test("/=", token_type::DIV_EQUAL, nullptr);
        }

        TEST_METHOD(ForwardSlashForwardSlash)
        {
            run_basic_token_test("//\n", token_type::LINE_COMMENT, new string(""));
        }

        TEST_METHOD(LineComment)
        {
            run_basic_token_test("// This is a full line comment\n", token_type::LINE_COMMENT,
                                 new string(" This is a full line comment"));
        }

        TEST_METHOD(ForwardSlashStar)
        {
            run_basic_token_test("/*", token_type::COMMENT_START, nullptr);
        }

        TEST_METHOD(Percent)
        {
            run_basic_token_test("%", token_type::MOD, nullptr);
        }

        TEST_METHOD(PercentEqual)
        {
            run_basic_token_test("%=", token_type::MOD_EQUAL, nullptr);
        }

        TEST_METHOD(And)
        {
            run_basic_token_test("&", token_type::BIT_AND, nullptr);
        }

        TEST_METHOD(AndAnd)
        {
            run_basic_token_test("&&", token_type::AND, nullptr);
        }

        TEST_METHOD(Pipe)
        {
            run_basic_token_test("|", token_type::BIT_OR, nullptr);
        }

        TEST_METHOD(PipePipe)
        {
            run_basic_token_test("||", token_type::OR, nullptr);
        }

        TEST_METHOD(Carrot)
        {
            run_basic_token_test("^", token_type::BIT_XOR, nullptr);
        }

        TEST_METHOD(RightAngleBrace)
        {
            run_basic_token_test(">", token_type::RIGHT_ANGLE_BRACE, nullptr);
        }

        TEST_METHOD(RightAngleBraceRightAngleBrace)
        {
            run_basic_token_test(">>", token_type::BIT_RIGHT, nullptr);
        }

        TEST_METHOD(RightAngleBraceEqual)
        {
            run_basic_token_test(">=", token_type::GREATER_THAN_EQUAL, nullptr);
        }

        TEST_METHOD(LeftAngleBrace)
        {
            run_basic_token_test("<", token_type::LEFT_ANGLE_BRACE, nullptr);
        }

        TEST_METHOD(LeftAngleBraceLeftAngleBrace)
        {
            run_basic_token_test("<<", token_type::BIT_LEFT, nullptr);
        }

        TEST_METHOD(LeftAngleBraceEqual)
        {
            run_basic_token_test("<=", token_type::LESS_THAN_EQUAL, nullptr);
        }

        TEST_METHOD(Bang)
        {
            run_basic_token_test("!", token_type::BANG, nullptr);
        }

        TEST_METHOD(BangEqual)
        {
            run_basic_token_test("!=", token_type::BANG_EQUAL, nullptr);
        }

        TEST_METHOD(At)
        {
            run_basic_token_test("@", token_type::AT, nullptr);
        }

        TEST_METHOD(StructKeyword)
        {
            run_basic_token_test("struct", token_type::STRUCT, nullptr);
        }

        TEST_METHOD(PublicKeyword)
        {
            run_basic_token_test("public", token_type::PUBLIC, nullptr);
        }

        TEST_METHOD(PrivateKeyword)
        {
            run_basic_token_test("private", token_type::PRIVATE, nullptr);
        }

        TEST_METHOD(StaticKeyword)
        {
            run_basic_token_test("static", token_type::STATIC, nullptr);
        }

        TEST_METHOD(FinalKeyword)
        {
            run_basic_token_test("final", token_type::FINAL, nullptr);
        }

        TEST_METHOD(MatchKeyword)
        {
            run_basic_token_test("match", token_type::MATCH, nullptr);
        }

        TEST_METHOD(SwitchKeyword)
        {
            run_basic_token_test("switch", token_type::SWITCH, nullptr);
        }

        TEST_METHOD(CaseKeyword)
        {
            run_basic_token_test("case", token_type::CASE, nullptr);
        }

        TEST_METHOD(NextKeyword)
        {
            run_basic_token_test("next", token_type::NEXT, nullptr);
        }

        TEST_METHOD(BreakKeyword)
        {
            run_basic_token_test("break", token_type::BREAK, nullptr);
        }

        TEST_METHOD(ThrowKeyword)
        {
            run_basic_token_test("throw", token_type::THROW, nullptr);
        }

        TEST_METHOD(IfKeyword)
        {
            run_basic_token_test("if", token_type::IF, nullptr);
        }

        TEST_METHOD(ElseKeyword)
        {
            run_basic_token_test("else", token_type::ELSE, nullptr);
        }

        TEST_METHOD(WhileKeyword)
        {
            run_basic_token_test("while", token_type::WHILE, nullptr);
        }

        TEST_METHOD(ForKeyword)
        {
            run_basic_token_test("for", token_type::FOR, nullptr);
        }

        TEST_METHOD(TryKeyword)
        {
            run_basic_token_test("try", token_type::TRY, nullptr);
        }

        TEST_METHOD(CatchKeyword)
        {
            run_basic_token_test("catch", token_type::CATCH, nullptr);
        }

        TEST_METHOD(FinallyKeyword)
        {
            run_basic_token_test("finally", token_type::FINALLY, nullptr);
        }

        TEST_METHOD(ThrowsKeyword)
        {
            run_basic_token_test("throws", token_type::THROWS, nullptr);
        }

        TEST_METHOD(ReturnKeyword)
        {
            run_basic_token_test("return", token_type::RETURN, nullptr);
        }

        TEST_METHOD(AnnotationKeyword)
        {
            run_basic_token_test("annotation", token_type::ANNOTATION, nullptr);
        }

        TEST_METHOD(NewKeyword)
        {
            run_basic_token_test("new", token_type::NEW, nullptr);
        }

        TEST_METHOD(ClassKeyword)
        {
            run_basic_token_test("class", token_type::CLASS, nullptr);
        }

        TEST_METHOD(DefKeyword)
        {
            run_basic_token_test("def", token_type::DEF, nullptr);
        }

        TEST_METHOD(EnumKeyword)
        {
            run_basic_token_test("enum", token_type::ENUM, nullptr);
        }

        TEST_METHOD(SuperKeyword)
        {
            run_basic_token_test("super", token_type::SUPER, nullptr);
        }

        TEST_METHOD(ThisKeyword)
        {
            run_basic_token_test("this", token_type::THIS, nullptr);
        }

        TEST_METHOD(ImportKeyword)
        {
            run_basic_token_test("import", token_type::IMPORT, nullptr);
        }

        TEST_METHOD(TrueKeyword)
        {
            run_basic_token_test("true", token_type::TRU, nullptr);
        }

        TEST_METHOD(FalseKeyword)
        {
            run_basic_token_test("false", token_type::FALS, nullptr);
        }

        TEST_METHOD(NoneKeyword)
        {
            run_basic_token_test("none", token_type::NONE, nullptr);
        }

        TEST_METHOD(Regex)
        {
            run_basic_token_test("r", token_type::R, nullptr);
        }

        TEST_METHOD(Name)
        {
            run_basic_token_test("someName", token_type::NAME, new string("someName"));
        }

        TEST_METHOD(UnderscoreName)
        {
            run_basic_token_test("some_name", token_type::NAME, new string("some_name"));
        }

        TEST_METHOD(NumberName)
        {
            run_basic_token_test("some_name1", token_type::NAME, new string("some_name1"));
        }

        TEST_METHOD(LiteralString)
        {
            run_basic_token_test("\"A String\"", token_type::LITERAL, new string("A String"));
        }

        TEST_METHOD(LiteralSequence)
        {
            run_basic_token_test("\'A String\'", token_type::LITERAL, new string("A String"));
        }

        TEST_METHOD(LiteralNumber)
        {
            run_basic_token_test("\'4\'", token_type::LITERAL, new string("4"));
        }

        TEST_METHOD(LiteralFloatNumber)
        {
            run_basic_token_test("\'4.5\'", token_type::LITERAL, new string("4.5"));
        }
    }
}